package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;


public class Lambda implements RequestHandler<LambdaRequest, LambdaResponse> {

  @Override
  public LambdaResponse handleRequest(LambdaRequest request, Context context) {
    String input = request.getInput();

    // Process the input string
    String processedOutput = processInput(input);

    LambdaResponse response = new LambdaResponse();
    response.setOutput(processedOutput);
    context.getLogger().log("Processing complete.Message: " + processedOutput);
    return response;
  }

  private String processInput(String input) {
    return String.format("This is your message in upper case: %s .",input.toUpperCase());
  }
}





