package dev.ancaghenade.testcontainerslambda;


import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Component
@Configuration
public class LambdaConfig {

  @Value("${aws.lambda.endpoint}")
  private String lambdaEndpoint;

  @Value("${aws.region}")
  private String region;

  @Value("${aws.credentials.secret-key}")
  private String secretKey;

  @Value("${aws.credentials.access-key}")
  private String accessLey;


  @Bean
  public LambdaClient lambdaClient() {
    return LambdaClient.builder()
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider
                .create(AwsBasicCredentials
                    .create(accessLey, secretKey)))
        .endpointOverride(URI.create(lambdaEndpoint))
        .build();
  }
}
