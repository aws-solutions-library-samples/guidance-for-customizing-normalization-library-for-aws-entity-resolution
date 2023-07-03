import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import { aws_s3 as s3 } from 'aws-cdk-lib';
import { Asset } from 'aws-cdk-lib/aws-s3-assets';
import * as s3deploy from "aws-cdk-lib/aws-s3-deployment"
import * as iam from "aws-cdk-lib/aws-iam"
import * as path from "path"
import * as glue from '@aws-cdk/aws-glue-alpha'

//import * as glue from 'aws-cdk-lib/aws-glue-alpha'



export class AwsSolutionsFirstPartyDataNormalizationLibraryStack extends cdk.Stack {

  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const s3Bucket = new s3.Bucket(this, "normalization-input-bucket", {
        versioned: true,
        enforceSSL: true,
        serverAccessLogsPrefix: "normalization-input-bucket"
    })

    const outputS3Bucket = new s3.Bucket(this, "normalization-output-bucket", {
        versioned: true,
        enforceSSL: true,
        serverAccessLogsPrefix: "normalization-output-bucket"
    })


    new s3deploy.BucketDeployment(this, "normalization-library-deployment", {
        sources:[s3deploy.Source.asset('sample/')],
        destinationBucket: s3Bucket
    })

/*
    const jarAsset = new Asset(this, 'JAR', {
        path: path.join(__dirname, '..', 'target', 'AWSSolutionsFirstPartyDataNormalizationLibrary-1.0-SNAPSHOT-jar-with-dependencies.jar'),
    });

    // Upload job script as S3 asset
    const scriptAsset = new Asset(this, 'Script', {
        path: path.join(__dirname, '..', 'glue', 'script.scala'),
    });
*/
    const inputBucket = s3.Bucket.fromBucketName(this, 'InputBucketByName', 'id-resoultion-jan272023');
    const outputBucket = s3.Bucket.fromBucketName(this, 'OutputBucketByName', 'id-resoultion-jan272023');

    // Create Glue Job role
    const jobRole = new iam.Role(this, 'JobRole', {
        roleName: 'normalization-glue-role',
        assumedBy: new iam.ServicePrincipal('glue.amazonaws.com'),
        managedPolicies: [
            iam.ManagedPolicy.fromAwsManagedPolicyName('service-role/AWSGlueServiceRole'),
        ],
    });

    inputBucket.grantRead(jobRole)
    outputBucket.grantReadWrite(jobRole)
    s3Bucket.grantRead(jobRole)
    outputS3Bucket.grantReadWrite(jobRole)
    //jarAsset.grantRead(jobRole)
    //scriptAsset.grantRead(jobRole)


// Create Glue Job
    const job = new glue.Job(this, 'ScalaSparkEtlJob', {
        executable: glue.JobExecutable.scalaEtl({
            glueVersion: glue.GlueVersion.V3_0,
            script: glue.Code.fromAsset(path.join(__dirname, '../glue/script.scala')),
            className: 'GlueApp',
            extraJars: [glue.Code.fromAsset(path.join(__dirname, '../target/AWSSolutionsFirstPartyDataNormalizationLibrary-1.0-SNAPSHOT-jar-with-dependencies.jar'))]
        }),
        jobName: 'AwsSolutionsFirstPartyDataNormalizationETLJob',
        role: jobRole,
        defaultArguments: {
            '--input_path':`s3://${s3Bucket.bucketName}/synthetic_testData10k.csv`,
            '--output_path':`s3://${outputS3Bucket.bucketName}/normalized_output/`
        }
    });

    new cdk.CfnOutput(this, "input data path", {value: `s3://${s3Bucket.bucketName}/`})
    new cdk.CfnOutput(this, "glue normalized output path", {value: `s3://${outputS3Bucket.bucketName}/normalized_output/`})

  }
}