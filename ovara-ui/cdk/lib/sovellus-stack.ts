import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as route53 from 'aws-cdk-lib/aws-route53';
import * as acm from 'aws-cdk-lib/aws-certificatemanager';
import * as cloudfront from 'aws-cdk-lib/aws-cloudfront';
import * as origins from 'aws-cdk-lib/aws-cloudfront-origins';
import * as targets from 'aws-cdk-lib/aws-route53-targets';
import * as s3 from 'aws-cdk-lib/aws-s3';
import * as s3deploy from 'aws-cdk-lib/aws-s3-deployment';
import { PriceClass } from 'aws-cdk-lib/aws-cloudfront';

interface OvaraUIStackProps extends cdk.StackProps {
  environmentName: string;
  domainName: string;
  hostedZone: route53.IHostedZone;
  certificate: acm.ICertificate;
}

export class OvaraUISovellusStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props: OvaraUIStackProps) {
    super(scope, id, props);

    const siteBucket = new s3.Bucket(this, 'OvaraUiBucket', {
      blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
      encryption: s3.BucketEncryption.S3_MANAGED,
      enforceSSL: true,
    });

    const originAccessIdentity = new cloudfront.OriginAccessIdentity(
      this,
      'OvaraUiOriginAccessIdentity',
    );
    siteBucket.grantRead(originAccessIdentity);

    const redirectToBasePathFunction = new cloudfront.Function(
      this,
      'RedirectToOvaraBasePath',
      {
        code: cloudfront.FunctionCode.fromInline(`
function handler(event) {
  var request = event.request;
  if (request.uri === "/") {
    return {
      statusCode: 302,
      statusDescription: "Found",
      headers: { location: { value: "/ovara/" } }
    };
  }
  if (request.uri === "/ovara") {
    return {
      statusCode: 302,
      statusDescription: "Found",
      headers: { location: { value: "/ovara/" } }
    };
  }
  return request;
}
        `),
      },
    );

    const distribution = new cloudfront.Distribution(
      this,
      `${props.environmentName}-OvaraUI`,
      {
        certificate: props.certificate,
        domainNames: [props.domainName],
        defaultBehavior: {
          origin: origins.S3BucketOrigin.withOriginAccessIdentity(siteBucket, {
            originAccessIdentity,
          }),
          viewerProtocolPolicy:
            cloudfront.ViewerProtocolPolicy.REDIRECT_TO_HTTPS,
          functionAssociations: [
            {
              eventType: cloudfront.FunctionEventType.VIEWER_REQUEST,
              function: redirectToBasePathFunction,
            },
          ],
        },
        errorResponses: [
          {
            httpStatus: 403,
            responseHttpStatus: 200,
            responsePagePath: '/ovara/index.html',
          },
          {
            httpStatus: 404,
            responseHttpStatus: 200,
            responsePagePath: '/ovara/index.html',
          },
        ],
        priceClass: PriceClass.PRICE_CLASS_100,
      },
    );

    new s3deploy.BucketDeployment(this, 'DeployOvaraUi', {
      sources: [s3deploy.Source.asset('../dist')],
      destinationBucket: siteBucket,
      distribution,
      distributionPaths: ['/', '/ovara/*'],
    });

    new route53.ARecord(this, 'OvaraUiAliasRecord', {
      zone: props.hostedZone,
      recordName: props.domainName,
      target: route53.RecordTarget.fromAlias(
        new targets.CloudFrontTarget(distribution),
      ),
    });

    new cdk.CfnOutput(this, 'CloudFrontDistributionDomain', {
      value: distribution.distributionDomainName,
    });
  }
}
