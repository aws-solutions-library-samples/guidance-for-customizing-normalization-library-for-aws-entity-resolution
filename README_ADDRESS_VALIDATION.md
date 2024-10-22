## Before Starting
Make sure to follow the instructions in the [top-level README](README.md) before doing address validation.

## Run the solution

Once the stack is successfully created, a Lambda function is created and file is uploaded to S3.
1. Login into the AWS Console
2. Navigate to [AWS Lambda](https://us-east-1.console.aws.amazon.com/lambda/home) appropriate to your AWS region
3. Click on the Lambda Function named ***AddressValidationHandler***
4. Navigate to the **Test** tab
5. Provide the following Json under ***Event Json***
```
{
  "outputFileName": "result.csv",
  "addressFieldName": "",
  "sourceFieldName": ""
}
```
6. Click the ***Test*** button. Test should take about 
7. Check for the resulting output in your s3 bucket that was created (search for ***entityresolutionaddress*** in the S3 console)
   1. When you find the bucket, the results will be output to ***/address_validation_results/result.csv***

## Customize the solution

1. Change the name for the output file in the request Json.
2. Provide your own data for test.
   1. Replace the csv file in the InputBucket with your own csv.
   2. Find InputBucket by searching for ***entityresolutionaddress*** in the S3 console. Look for the bucket with 'input' in the name.
   3. The file ***must*** be named 'entity_resolution_address.csv'
   4. Keep in mind that Lambda has a maximum time out of 15 min. Keep the number of records in your csv under 2500 to prevent timeout.
3. If you provide your own data, you can provide the ***addressFieldName*** and ***sourceFieldName*** in the request Json. Providing an empty value defaults to "Address" and "AddressId" respectively.