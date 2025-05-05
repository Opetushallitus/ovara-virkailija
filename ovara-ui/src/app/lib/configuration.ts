export const DOMAIN =
  process.env.APP_URL ?? process.env.VIRKAILIJA_URL ?? 'https://localhost:3405';

export const OVARA_BACKEND =
  process.env.OVARA_BACKEND ?? process.env.VIRKAILIJA_URL;

export const isLocalhost = DOMAIN.includes('localhost');

export const isDev = process.env.NODE_ENV === 'development';

export const isProd = process.env.NODE_ENV === 'production';

export const isTesting = process.env.TEST === 'true';

export const configuration = {
  raamitUrl: `${DOMAIN}/virkailija-raamit/apply-raamit.js`,
  ovaraBackendApiUrl: `${OVARA_BACKEND}/ovara-backend/api`,
  virkailijaUrl: DOMAIN,
  lokalisointiPrefix: `${DOMAIN}/lokalisointi/tolgee`,
} as const;
