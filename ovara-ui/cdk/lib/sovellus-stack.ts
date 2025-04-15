import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as route53 from 'aws-cdk-lib/aws-route53';
import * as acm from 'aws-cdk-lib/aws-certificatemanager';
import { Nextjs } from 'cdk-nextjs-standalone';
import * as logs from 'aws-cdk-lib/aws-logs';
import { PriceClass } from 'aws-cdk-lib/aws-cloudfront';

interface OvaraUIStackProps extends cdk.StackProps {
  environmentName: string;
  skipBuild: boolean;
}

export class OvaraUISovellusStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props: OvaraUIStackProps) {
    super(scope, id, props);

    const publicHostedZones: { [p: string]: string } = {
      hahtuva: 'hahtuvaopintopolku.fi',
      pallero: 'testiopintopolku.fi',
      untuva: 'untuvaopintopolku.fi',
    };

    const publicHostedZoneIds: { [p: string]: string } = {
      hahtuva: 'Z20VS6J64SGAG9',
      pallero: 'Z175BBXSKVCV3B',
      untuva: 'Z1399RU36FG2N9',
    };

    const hostedZone = route53.HostedZone.fromHostedZoneAttributes(
      this,
      'PublicHostedZone',
      {
        zoneName: `${publicHostedZones[props.environmentName]}.`,
        hostedZoneId: `${publicHostedZoneIds[props.environmentName]}`,
      },
    );

    const domainName = `ovara-virkailija.${publicHostedZones[props.environmentName]}`;

    const certificate = new acm.DnsValidatedCertificate(
      this,
      'SiteCertificate',
      {
        domainName,
        hostedZone,
        region: 'us-east-1', // Cloudfront only checks this region for certificates.
      },
    );

    const nextjs = new Nextjs(this, `${props.environmentName}-OvaraUI`, {
      nextjsPath: '..', // relative path from project root to NextJS
      ...(props.skipBuild
        ? {
            buildCommand:
              'npx --yes open-next@^2 build -- --build-command "npm run noop"',
          }
        : {}),
      basePath: '/ovara',
      environment: {
        SKIP_TYPECHECK: 'true',
        STANDALONE: 'true',
        VIRKAILIJA_URL: `https://virkailija.${publicHostedZones[props.environmentName]}`,
      },
      domainProps: {
        domainName,
        certificate,
        hostedZone,
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
