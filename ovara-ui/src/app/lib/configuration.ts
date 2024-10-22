export const DOMAIN =
  process.env.APP_URL ?? process.env.VIRKAILIJA_URL ?? 'https://localhost:3405';

export const OVARA_BACKEND_DOMAIN = process.env.OVARA_BACKEND;

export const isLocalhost = DOMAIN.includes('localhost');

export const isDev = process.env.NODE_ENV === 'development';

export const isProd = process.env.NODE_ENV === 'production';

export const isTesting = process.env.TEST === 'true';

export const configuration = {
  raamitUrl: `${DOMAIN}/virkailija-raamit/apply-raamit.js`,
  asiointiKieliUrl: `${DOMAIN}/oppijanumerorekisteri-service/henkilo/current/asiointiKieli`,
  ovaraBackendApiUrl: `${OVARA_BACKEND_DOMAIN}/ovara-backend/api`,
} as const;
