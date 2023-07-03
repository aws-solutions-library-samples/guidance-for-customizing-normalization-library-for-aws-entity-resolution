#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import { App, Aspects } from 'aws-cdk-lib';
import {AwsSolutionsChecks, NagSuppressions} from 'cdk-nag';

import { AwsSolutionsFirstPartyDataNormalizationLibraryStack } from '../lib/aws_solutions_first_party_data_normalization_library-stack';

const app = new cdk.App();
const stack = new AwsSolutionsFirstPartyDataNormalizationLibraryStack(app, 'AwsSolutionsFirstPartyDataNormalizationLibraryStack', {
  /* If you don't specify 'env', this stack will be environment-agnostic.
   * Account/Region-dependent features and context lookups will not work,
   * but a single synthesized template can be deployed anywhere. */

  /* Uncomment the next line to specialize this stack for the AWS Account
   * and Region that are implied by the current CLI configuration. */
  // env: { account: process.env.CDK_DEFAULT_ACCOUNT, region: process.env.CDK_DEFAULT_REGION },

  /* Uncomment the next line if you know exactly what Account and Region you
   * want to deploy the stack to. */
  // env: { account: '123456789012', region: 'us-east-1' },

  /* For more information, see https://docs.aws.amazon.com/cdk/latest/guide/environments.html */
});

NagSuppressions.addStackSuppressions(
    stack,
    [
        {
            "id": "AwsSolutions-IAM5",
            "reason": "S3Bucket Deployment contains a wildcard permissions have apply to bucket",
        },
        {
            "id": "AwsSolutions-IAM4",
            "reason": "AWS Managed IAM policies are used for AWSGlueServiceRole and AmazonEC2ContainerRegistryReadOnly been allowed to maintain secured access with the ease of operational maintenance - however for more granular control the custom IAM policies can be used instead of AWS managed policies",
        },
        {
            "id": "AwsSolutions-S1",
            "reason": "S3 Access Logs are disabled for demo purposes.",
        },
        {
            "id":"AwsSolutions-L1",
            "reason":"The non-container Lambda function is created as part of the CDKBucketDeployment that uploads the sample data to Amazon S3 bucket"
        }
    ],
)

Aspects.of(app).add(new AwsSolutionsChecks());
