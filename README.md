# testcontainers-lambda

This repo is a small sample on how to use Testcontainers to test a Spring Boot app using a Lambda
function on LocalStack.

## How to use

Start LocalStack by using the `docker-compose` file: `docker compose up`.
Change and build your Lambda function:
* `cd lambda`
* `mvn clean package shade:shade`

You can find the AWS CLI command to create the Lambda function in the `src/main/resources/awslocal-commands.sh`  file:
From the `lambda` folder:
```
awslocal lambda create-function \
--function-name lambda-demo \
--runtime java11 \
--handler lambda.Lambda::handleRequest \
--memory-size 512 \
--timeout 60 \
--role arn:aws:iam::000000000000:role/lambda-role \
--zip-file fileb://target/lambda.jar
```
Start the Spring Boot app: `mvn spring-boot:run`.

You can also find the update command in the same file.

Use curl: `curl -X GET http://localhost:8080/hello%20world` and the output will be:
```
This is your message in upper case: HELLO WORLD .
```

To run the test using [Testcontainrs](https://java.testcontainers.org/) just run:
`mvn test` in the root folder, or by using your IDE provided interface.
