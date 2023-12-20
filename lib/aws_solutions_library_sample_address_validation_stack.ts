import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import { aws_s3 as s3, Duration } from 'aws-cdk-lib';
import { Architecture, Code, Function, Runtime } from "aws-cdk-lib/aws-lambda";
import * as s3deploy from "aws-cdk-lib/aws-s3-deployment"
import * as path from "path"
import { CfnPlaceIndex } from "aws-cdk-lib/aws-location";
import { Effect, PolicyStatement } from "aws-cdk-lib/aws-iam";


export class AwsSolutionsLibrarySampleAddressValidationStack extends cdk.Stack {

    public static readonly INPUT_FILE_NAME: string = "entity_resolution_address.csv";
    public static readonly PLACE_INDEX_NAME: string = "SampleAddressValidationIndex";

    constructor(scope: Construct, id: string, props?: cdk.StackProps) {
        super(scope, id, props);

        const inputs3Bucket = new s3.Bucket(this, "address-validation-input-bucket", {
            enforceSSL: true,
            serverAccessLogsPrefix: "address-validation-input-bucket"
        });

        const outputS3Bucket = new s3.Bucket(this, "address-validation-output-bucket", {
            enforceSSL: true,
            serverAccessLogsPrefix: "address-validation-output-bucket"
        });

        new s3deploy.BucketDeployment(this, "normalization-library-deployment", {
            sources:[s3deploy.Source.asset('sample/address_validation/')],
            destinationBucket: inputs3Bucket
        });

        const placeIndex = new CfnPlaceIndex(this, 'SamplePlaceIndex', {
            dataSource: 'Esri',
            indexName: AwsSolutionsLibrarySampleAddressValidationStack.PLACE_INDEX_NAME
        });
        const addressValidationLambda = new Function(this, "address-validation-handler", {
            functionName: "AddressValidationHandler",
            code: Code.fromAsset(path.join("target", "NormalizationLibrary-1.0-SNAPSHOT-jar-with-dependencies.jar")),
            handler: "com.amazon.lambda.AddressValidationHandler::handleRequest",
            runtime: Runtime.JAVA_11,
            memorySize: 2048,
            timeout: Duration.minutes(15),
            architecture: Architecture.X86_64,
            environment: {
                INPUT_SOURCE_ARN: inputs3Bucket.bucketArn,
                INPUT_SOURCE_BUCKET_NAME: inputs3Bucket.bucketName,
                OUTPUT_SOURCE_BUCKET_NAME: outputS3Bucket.bucketName,
                INPUT_SOURCE_FILE_NAME: AwsSolutionsLibrarySampleAddressValidationStack.INPUT_FILE_NAME,
                PLACE_INDEX_NAME: placeIndex.indexName
            }
        });
        addressValidationLambda.addToRolePolicy(new PolicyStatement({
            effect: Effect.ALLOW,
            actions: ["geo:SearchPlaceIndexForText"],
            resources: ["*"]
        }));

        inputs3Bucket.grantRead(addressValidationLambda);
        outputS3Bucket.grantReadWrite(addressValidationLambda);

        new cdk.CfnOutput(this, "input data path", {value: `s3://${inputs3Bucket.bucketName}/${AwsSolutionsLibrarySampleAddressValidationStack.INPUT_FILE_NAME}`})
        new cdk.CfnOutput(this, "Address validation output path", {value: `s3://${outputS3Bucket.bucketName}/address_validation/output/`})

    }
}