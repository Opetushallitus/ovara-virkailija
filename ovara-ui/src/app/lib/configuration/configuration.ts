export const DOMAIN =
  import.meta.env.VITE_APP_URL ?? import.meta.env.VITE_VIRKAILIJA_URL ?? '';

export const isLocalhost =
  typeof window !== 'undefined' &&
  window.location.hostname.includes('localhost');

export const isDev = import.meta.env.DEV;

export const isProd = import.meta.env.PROD;

export const isTesting = import.meta.env.VITE_TEST === 'true';

export const configuration = {
  // Backend API is always same-origin.
  // Dev: Vite proxies it to localhost:8443.
  // Prod: Spring Boot serves it on the same origin.
  ovaraBackendApiUrl: '/ovara-backend/api',

  // External services
  raamitUrl: `${DOMAIN}/virkailija-raamit/apply-raamit.js`,
  virkailijaUrl: DOMAIN,
  lokalisointiPrefix: `${DOMAIN}/lokalisointi/tolgee`,
} as const;

export type Configuration = {
  raamitUrl: string;
  ovaraBackendApiUrl: string;
  virkailijaUrl: string;
  lokalisointiPrefix: string;
};
