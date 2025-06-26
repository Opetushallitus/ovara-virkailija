#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import { OvaraUISovellusStack } from '../lib/sovellus-stack';
import { OvaraCertificateStack } from '../lib/certificate-stack';
import { HostedZoneStack } from '../lib/hosted-zone-stack';

const app = new cdk.App();
const environmentName = app.node.tryGetContext('environment');
const account = process.env.CDK_DEFAULT_ACCOUNT!;
const envEU = { account, region: 'eu-west-1' };
const envUS = { account, region: 'us-east-1' };

const publicHostedZones: { [p: string]: string } = {
  hahtuva: 'hahtuvaopintopolku.fi',
  pallero: 'testiopintopolku.fi',
  sade: 'opintopolku.fi',
  untuva: 'untuvaopintopolku.fi',
};

const publicHostedZoneIds: { [p: string]: string } = {
  hahtuva: 'Z20VS6J64SGAG9',
  pallero: 'Z175BBXSKVCV3B',
  sade: 'ZNMCY72OCXY4M',
  untuva: 'Z1399RU36FG2N9',
};

const hostedZoneStack = new HostedZoneStack(
  app,
  'HostedZoneStack',
  { env: envEU },
  environmentName,
  publicHostedZones,
  publicHostedZoneIds,
);

const domainName = `ovara-virkailija.${publicHostedZones[environmentName]}`;

const certificateStack = new OvaraCertificateStack(
  app,
  'OvaraCertificateStack',
  {
    env: envUS,
    stackName: `${environmentName}-ovara-certificate`,
    domain: domainName,
    hostedZone: hostedZoneStack.hostedZone,
    crossRegionReferences: true,
  },
);

new OvaraUISovellusStack(app, 'OvaraUISovellusStack', {
  stackName: `${environmentName}-ovara-ui`,
  environmentName,
  env: {
    account: process.env.CDK_DEFAULT_ACCOUNT,
    region: process.env.CDK_DEFAULT_REGION,
  },
  domainName: domainName,
  hostedZone: hostedZoneStack.hostedZone,
  certificate: certificateStack.certificate,
  crossRegionReferences: true,
});
