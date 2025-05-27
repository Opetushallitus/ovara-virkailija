import { FetchError, PermissionError } from '@/app/lib/common';
import { redirect } from 'next/navigation';
import { getConfiguration } from '@/app/lib/configuration/client-configuration';
import { configuration } from '@/app/lib/configuration/configuration';

let _csrfToken: string;
const isServer = typeof window === 'undefined';
console.log('isServer', isServer);

function getRuntimeConfiguration() {
  return isServer ? configuration : getConfiguration();
}

async function csrfToken() {
  if (!_csrfToken) {
    const config = getRuntimeConfiguration();
    const { ovaraBackendApiUrl } = config;
    const response = await fetch(`${ovaraBackendApiUrl}/csrf`, {
      credentials: 'include',
    });
    const data = await response.json();
    _csrfToken = data.token;
  }
  return _csrfToken;
}

type Options = {
  headers?: object;
  queryParams?: string | null;
};

export async function apiFetch(
  resource: string,
  options?: Options,
  cache?: string,
) {
  try {
    const config = getRuntimeConfiguration();
    const { ovaraBackendApiUrl } = config;
    const queryParams = options?.queryParams ? options.queryParams : '';
    const response = await fetch(
      `${ovaraBackendApiUrl}/${resource}${queryParams}`,
      {
        ...options,
        credentials: 'include',
        headers: {
          ...options?.headers,
          'X-CSRF-TOKEN': await csrfToken(),
          cache: cache ?? 'force-cache',
        },
      },
    );
    if (response.status === 401) {
      redirectToLogin();
      return null; // Palautetaan null login-flowta odotellessa
    }
    if (response.status >= 400) {
      let body: unknown;
      try {
        const clone = response.clone();
        const contentType = clone.headers.get('content-type') || '';

        if (contentType.includes('application/json')) {
          body = await clone.json();
        } else {
          // varmistetaan ettei springin error page vuoda käliin
          const text = await clone.text();
          body =
            text.startsWith('<!DOCTYPE html') || text.includes('<html')
              ? 'virhe.palvelin'
              : text;
        }
      } catch {
        body = 'virhe.tuntematon';
      }
      throw new FetchError(response, body);
    }

    return response;
  } catch (e) {
    console.error('Fetching data failed!');
    return Promise.reject(e);
  }
}

const redirectToLogin = () => {
  const { ovaraBackendApiUrl } = getRuntimeConfiguration();
  const loginUrl = `${ovaraBackendApiUrl}/login`;
  if (isServer) {
    redirect(loginUrl);
  } else {
    location.assign(loginUrl);
  }
};

const noContent = (response: Response) => response.status === 204;

const responseToData = async (res: Response | null) => {
  if (!res || !(res instanceof Response)) return {};
  if (noContent(res)) return {};
  try {
    return await res.json();
  } catch (e) {
    console.error('Parsing fetch response body as JSON failed!');
    return Promise.reject(e);
  }
};

export const doApiFetch = async (
  resource: string,
  options?: Options,
  cache?: string,
) => {
  try {
    const response = await apiFetch(resource, options, cache);
    return responseToData(response);
  } catch (error: unknown) {
    if (error instanceof FetchError) {
      if (error.response?.status === 401) {
        redirectToLogin();
        return {};
      }
      if (error.response?.status === 403) {
        return Promise.reject(new PermissionError());
      }
    }
    return Promise.reject(error);
  }
};
