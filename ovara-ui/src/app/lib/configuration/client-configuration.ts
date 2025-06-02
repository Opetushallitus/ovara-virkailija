import { Configuration } from './configuration';

declare global {
  interface Window {
    configuration?: Configuration;
  }
}

export function setConfiguration(config: Configuration) {
  window.configuration = config;
}

export function getConfiguration(): Configuration {
  if (!window.configuration) {
    throw new Error('Client configuration has not been set yet.');
  }
  return window.configuration;
}
