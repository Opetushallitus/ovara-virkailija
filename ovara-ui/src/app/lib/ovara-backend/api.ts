import { FetchError, PermissionError } from '@/app/lib/common';
import { redirect } from 'next/navigation';
import { getConfiguration } from '@/app/lib/configuration/serverConfiguration';

let _csrfToken: string;
const isServer = typeof window === 'undefined';

async function csrfToken() {
  const configuration = await getConfiguration();
  if (!_csrfToken) {
    const response = await fetch(`${configuration.ovaraBackendApiUrl}/csrf`, {
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
    const queryParams = options?.queryParams ? options.queryParams : '';
    const configuration = await getConfiguration();
    const response = await fetch(
      `${configuration.ovaraBackendApiUrl}/${resource}${queryParams}`,
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

const isUnauthenticated = (response: Response) => {
  return response?.status === 401;
};

const redirectToLogin = async () => {
  const configuration = await getConfiguration();
  const loginUrl = `${configuration.ovaraBackendApiUrl}/login`;
  if (isServer) {
    redirect(loginUrl);
  } else {
    location.assign(loginUrl);
  }
};

const noContent = (response: Response) => {
  return response.status === 204;
};

const responseToData = async (res: Response | null) => {
  if (!res || !(res instanceof Response)) {
    return {}; // 401 tilanteiden paluuarvo
  }
  if (noContent(res)) {
    return {};
  }
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
      if (isUnauthenticated(error.response)) {
        redirectToLogin();
        return {}; // 401 tilanteita ei käsitellä virheenä
      }
      if (error.response.status === 403) {
        return Promise.reject(new PermissionError());
      }
    }
    return Promise.reject(error);
  }
};
