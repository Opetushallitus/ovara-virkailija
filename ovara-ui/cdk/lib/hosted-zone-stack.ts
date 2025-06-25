import * as cdk from 'aws-cdk-lib';
import * as route53 from 'aws-cdk-lib/aws-route53';

export class HostedZoneStack extends cdk.Stack {
  public readonly hostedZone: route53.IHostedZone;

  constructor(
    scope: cdk.App,
    id: string,
    props: cdk.StackProps,
    environmentName: string,
    publicHostedZones: { [p: string]: string },
    publicHostedZoneIds: { [p: string]: string },
  ) {
    super(scope, id, props);

    this.hostedZone = route53.HostedZone.fromHostedZoneAttributes(
      this,
      'PublicHostedZone',
      {
        zoneName: `${publicHostedZones[environmentName]}.`,
        hostedZoneId: `${publicHostedZoneIds[environmentName]}`,
      },
    );
  }
}
