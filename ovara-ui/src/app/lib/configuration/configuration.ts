import { getConfiguration } from './serverConfiguration';

export const isDev = process.env.NODE_ENV === 'development';

export const isProd = process.env.NODE_ENV === 'production';

export const isTesting = process.env.TEST === 'true';

export type Configuration = Awaited<ReturnType<typeof getConfiguration>>;
