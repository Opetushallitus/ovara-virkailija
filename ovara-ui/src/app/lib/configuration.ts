export const DOMAIN =
  process.env.APP_URL ?? process.env.VIRKAILIJA_URL ?? 'https://localhost:3405';

export const isLocalhost = DOMAIN.includes('localhost');

export const isDev = process.env.NODE_ENV === 'development';

export const isProd = process.env.NODE_ENV === 'production';

export const isTesting = process.env.TEST === 'true';

export const configuration = {
  raamitUrl: `${DOMAIN}/virkailija-raamit/apply-raamit.js`,
  loginUrl: `${DOMAIN}/cas/login`,
  sessionCookie: process.env.SESSION_COOKIE || 'JSESSIONID',
  kayttoikeusUrl: `${DOMAIN}/kayttooikeus-service/henkilo/current/omattiedot`,
  kooditUrl: `${DOMAIN}/koodisto-service/rest/codeelement/codes/`,
  asiointiKieliUrl: `${DOMAIN}/oppijanumerorekisteri-service/henkilo/current/asiointiKieli`,
  ohjausparametritUrl: `${DOMAIN}/ohjausparametrit-service/api/v1/rest/parametri`,
  ovaraBackendPing: `${DOMAIN}/ovara-backend/api/ping`,
} as const;
