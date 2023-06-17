
awslocal lambda create-function \
    --function-name lambda-demo \
    --runtime java11 \
    --handler lambda.Lambda::handleRequest \
    --memory-size 512 \
    --timeout 60 \
    --role arn:aws:iam::000000000000:role/lambda-role \
    --zip-file fileb://target/lambda.jar


awslocal lambda update-function-code --function-name lambda-demo \
         --zip-file fileb://target/lambda.jar \
         --region us-east-1

awslocal s3 mb s3://test-bucket --region us-east-1
