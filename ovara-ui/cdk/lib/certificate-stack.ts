import * as acm from 'aws-cdk-lib/aws-certificatemanager';
import * as cdk from 'aws-cdk-lib';
import * as route53 from 'aws-cdk-lib/aws-route53';
import { Construct } from 'constructs';

interface OvaraCertificateStackProps extends cdk.StackProps {
  domain: string;
  hostedZone: route53.IHostedZone;
}

export class OvaraCertificateStack extends cdk.Stack {
  readonly certificate: acm.ICertificate;
  constructor(scope: Construct, id: string, props: OvaraCertificateStackProps) {
    super(scope, id, props);

    this.certificate = new acm.Certificate(this, 'SiteCertificate', {
      domainName: props.domain,
      validation: acm.CertificateValidation.fromDns(props.hostedZone),
    });

    new cdk.CfnOutput(this, 'CertificateArnExport', {
      value: this.certificate.certificateArn,
      exportName: `${props.stackName}-CertificateArn`,
    });
  }
}
