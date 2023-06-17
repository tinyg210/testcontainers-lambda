package dev.ancaghenade.testcontainerslambda;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.localstack.LocalStackContainer.Service;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.CreateFunctionRequest;
import software.amazon.awssdk.services.lambda.model.FunctionCode;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class LambdaTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(LambdaTest.class);

  private static Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOGGER);
  protected TestRestTemplate restTemplate = new TestRestTemplate();

  protected static final String BASE_URL = "http://localhost:8080";

  @Container
  protected static LocalStackContainer localStack =
      new LocalStackContainer(DockerImageName.parse("localstack/localstack:2.1.0"))
          .withExposedPorts(4566)
          //  .withEnv("ENFORCE_IAM", "0")
          .withEnv("DEBUG", "1");
  private static Region region = Region.of(localStack.getRegion());

  private static LambdaClient lambdaClient;

  @DynamicPropertySource
  static void overrideConfigs(DynamicPropertyRegistry registry) {

    registry.add("aws.lambda.endpoint",
        () -> localStack.getEndpoint());
    registry.add("aws.region", () -> localStack.getRegion());
    registry.add("aws.credentials.secret-key", () -> localStack.getSecretKey());
    registry.add("aws.credentials.access-key", () -> localStack.getAccessKey());

  }

  @BeforeAll
  static void setup() {
    localStack.followOutput(logConsumer);

    lambdaClient = LambdaClient.builder()
        .endpointOverride(localStack.getEndpoint())
        .region(Region.of(localStack.getRegion())).build();

    createLambdaResources();
  }

  private static void createLambdaResources() {
    var functionName = "lambda-demo";
    var runtime = "java11";
    var handler = "lambda.Lambda::handleRequest";
    var zipFilePath = "lambda/target/lambda.jar";

    try {
      var zipFileBytes = Files.readAllBytes(Paths.get(zipFilePath));
      var zipFileBuffer = ByteBuffer.wrap(zipFileBytes);

      var createFunctionRequest = CreateFunctionRequest.builder()
          .functionName(functionName)
          .runtime(runtime)
          .handler(handler)
          .role("arn:aws:iam::000000000000:role/lambda-role")
          .code(FunctionCode.builder().zipFile(SdkBytes.fromByteBuffer(zipFileBuffer)).build())
          .timeout(60)
          .memorySize(512)
          .build();

      lambdaClient.createFunction(
          createFunctionRequest);

    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (AwsServiceException e) {
      throw new RuntimeException(e);
    } catch (SdkClientException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testLambdaRequest() throws JSONException, IOException, InterruptedException {

    waitForLambdaAvailability();

    String input = "hello";
    ResponseEntity<String> postResponse = restTemplate.exchange(BASE_URL + "/" + input,
        HttpMethod.GET, null, String.class);

    assertEquals(HttpStatus.OK, postResponse.getStatusCode());

    // give the Lambda time to start up and process the image
    try {
      Thread.sleep(10000);

    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    assertEquals("This is your message: hello", "This is your message: " + input);
  }


  protected static ExecResult executeInContainer(String command) throws Exception {

    final var execResult = localStack.execInContainer(formatCommand(command));
    // assertEquals(0, execResult.getExitCode());

    final var logs = execResult.getStdout() + execResult.getStderr();
    LOGGER.info(logs);
    LOGGER.error(execResult.getExitCode() != 0 ? execResult + " - DOES NOT WORK" : "");
    return execResult;
  }

  private static String[] formatCommand(String command) {
    return command.split(" ");
  }

  private void waitForLambdaAvailability() throws JSONException, IOException, InterruptedException {
    var result = localStack.execInContainer(formatCommand(
        "awslocal lambda get-function --function-name lambda-demo"));
    var obj = new JSONObject(result.getStdout()).getJSONObject("Configuration");
    var state = obj.getString("State");
    while (!state.equals("Active")) {
      result = localStack.execInContainer(formatCommand(
          "awslocal lambda get-function --function-name lambda-demo"));
      obj = new JSONObject(result.getStdout()).getJSONObject("Configuration");
      state = obj.getString("State");
    }
  }

}