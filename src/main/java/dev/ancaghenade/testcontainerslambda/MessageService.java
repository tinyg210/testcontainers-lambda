package dev.ancaghenade.testcontainerslambda;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;


@Component
public class MessageService {

  private final LambdaClient lambdaClient;

  @Autowired
  public MessageService(LambdaClient lambdaClient) {
    this.lambdaClient = lambdaClient;
  }

  public String invokeLambda(String input) throws IOException {
    LambdaRequest lambdaRequest = new LambdaRequest();
    lambdaRequest.setInput(input);

    ObjectMapper objectMapper = new ObjectMapper();
    String requestPayload = objectMapper.writeValueAsString(lambdaRequest);

    SdkBytes payload = SdkBytes.fromUtf8String(requestPayload);

    InvokeRequest invokeRequest = InvokeRequest.builder()
        .functionName("lambda-demo")
        .payload(payload)
        .build();

    try {
      InvokeResponse invokeResponse = lambdaClient.invoke(invokeRequest);
      String responsePayload = invokeResponse.payload().asUtf8String();

      LambdaResponse lambdaResponse = objectMapper.readValue(responsePayload, LambdaResponse.class);

      return lambdaResponse.getOutput();
    } catch (LambdaException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }
}
