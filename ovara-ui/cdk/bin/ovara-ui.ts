#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import { OvaraUISovellusStack } from '../lib/sovellus-stack';

const app = new cdk.App();
const environmentName = app.node.tryGetContext('environment');
const skipBuild = app.node.tryGetContext('skipBuild');

new OvaraUISovellusStack(app, 'OvaraUISovellusStack', {
  stackName: `${environmentName}-ovara-ui`,
  environmentName,
  skipBuild,
  env: {
    account: process.env.CDK_DEFAULT_ACCOUNT,
    region: process.env.CDK_DEFAULT_REGION,
  },
});
