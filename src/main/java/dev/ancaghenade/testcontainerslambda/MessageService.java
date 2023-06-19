package dev.ancaghenade.testcontainerslambda;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.LambdaException;


@Component
public class MessageService {

  private final LambdaClient lambdaClient;

  @Autowired
  public MessageService(LambdaClient lambdaClient) {
    this.lambdaClient = lambdaClient;
  }

  public String invokeLambda(String input) throws IOException {
    var lambdaRequest = new LambdaRequest();
    lambdaRequest.setInput(input);

    var objectMapper = new ObjectMapper();
    var requestPayload = objectMapper.writeValueAsString(lambdaRequest);

    var payload = SdkBytes.fromUtf8String(requestPayload);

    var invokeRequest = InvokeRequest.builder()
        .functionName("lambda-demo")
        .payload(payload)
        .build();

    try {
      var invokeResponse = lambdaClient.invoke(invokeRequest);
      var responsePayload = invokeResponse.payload().asUtf8String();

      var lambdaResponse = objectMapper.readValue(responsePayload, LambdaResponse.class);

      return lambdaResponse.getOutput();
    } catch (LambdaException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }
}
