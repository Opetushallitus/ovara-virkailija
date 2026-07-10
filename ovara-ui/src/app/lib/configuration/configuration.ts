export const DOMAIN =
  import.meta.env.VITE_APP_URL ?? import.meta.env.VITE_VIRKAILIJA_URL ?? '';

export const OVARA_BACKEND =
  import.meta.env.VITE_OVARA_BACKEND ??
  import.meta.env.VITE_VIRKAILIJA_URL ??
  '';

export const isLocalhost =
  typeof window !== 'undefined' &&
  window.location.hostname.includes('localhost');

export const isDev = import.meta.env.DEV;

export const isProd = import.meta.env.PROD;

export const isTesting = import.meta.env.VITE_TEST === 'true';

export const configuration = {
  raamitUrl: `${DOMAIN}/virkailija-raamit/apply-raamit.js`,
  ovaraBackendApiUrl: `${OVARA_BACKEND}/ovara-backend/api`,
  virkailijaUrl: DOMAIN,
  lokalisointiPrefix: `${DOMAIN}/lokalisointi/tolgee`,
} as const;

export type Configuration = {
  raamitUrl: string;
  ovaraBackendApiUrl: string;
  virkailijaUrl: string;
  lokalisointiPrefix: string;
};
