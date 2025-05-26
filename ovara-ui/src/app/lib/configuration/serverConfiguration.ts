'use server';

export async function getConfiguration() {
  const OVARA_BACKEND = process.env.OVARA_BACKEND ?? process.env.VIRKAILIJA_URL;

  const DOMAIN =
    process.env.APP_URL ??
    process.env.VIRKAILIJA_URL ??
    'https://localhost:3405';

  return {
    domain: DOMAIN,
    raamitUrl: `${DOMAIN}/virkailija-raamit/apply-raamit.js`,
    ovaraBackendApiUrl: `${OVARA_BACKEND}/ovara-backend/api`,
    virkailijaUrl: DOMAIN,
    lokalisointiPrefix: `${DOMAIN}/lokalisointi/tolgee`,
  } as const;
}
