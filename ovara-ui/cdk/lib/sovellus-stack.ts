import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as route53 from 'aws-cdk-lib/aws-route53';
import * as acm from 'aws-cdk-lib/aws-certificatemanager';
import { Nextjs } from 'cdk-nextjs-standalone';
import * as logs from 'aws-cdk-lib/aws-logs';
import { PriceClass } from 'aws-cdk-lib/aws-cloudfront';

interface OvaraUIStackProps extends cdk.StackProps {
  environmentName: string;
  domainName: string;
  hostedZone: route53.IHostedZone;
}

export class OvaraUISovellusStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props: OvaraUIStackProps) {
    super(scope, id, props);

    const certificateArn = cdk.Fn.importValue(
      `${props.environmentName}-ovara-certificate-CertificateArn`,
    );
    const certificate = acm.Certificate.fromCertificateArn(
      this,
      'ImportedCertificate',
      certificateArn,
    );

    const nextjs = new Nextjs(this, `${props.environmentName}-OvaraUI`, {
      nextjsPath: '..', // relative path from project root to NextJS
      basePath: '/ovara',
      environment: {
        SKIP_TYPECHECK: 'true',
        STANDALONE: 'true',
        VIRKAILIJA_URL: `https://virkailija.${props.hostedZone.zoneName}`,
      },
      domainProps: {
        domainName: props.domainName,
        certificate: certificate,
        hostedZone: props.hostedZone,
      },
      overrides: {
        nextjsDistribution: {
          distributionProps: {
            priceClass: PriceClass.PRICE_CLASS_100,
          },
        },
        nextjsServer: {
          functionProps: {
            logGroup: new logs.LogGroup(this, 'Ovara-ui NextJs Server', {
              logGroupName: `/aws/lambda/${props.environmentName}-ovara-ui`,
            }),
          },
        },
      },
    });
    new cdk.CfnOutput(this, 'CloudFrontDistributionDomain', {
      value: nextjs.distribution.distributionDomain,
    });
  }
}
