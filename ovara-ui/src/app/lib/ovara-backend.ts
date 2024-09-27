import { client } from '../lib/http-client';
import { configuration } from '../lib/configuration';

export const getPing = () => {
  return client.get(configuration.ovaraBackendPing);
};
