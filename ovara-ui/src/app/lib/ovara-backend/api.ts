import { configuration } from '../configuration';
import { FetchError, PermissionError } from '@/app/lib/common';

let _csrfToken: string;
const loginUrl = `${configuration.ovaraBackendApiUrl}/login`;

async function csrfToken() {
  if (!_csrfToken) {
    const response = await fetch(`${configuration.ovaraBackendApiUrl}/csrf`, {
      credentials: 'include',
    });

    _csrfToken = await response?.json();
  }

  return _csrfToken;
}

export async function apiFetch(
  resource: string,
  options?: { headers?: object },
) {
  try {
    const response = await fetch(
      `${configuration.ovaraBackendApiUrl}/${resource}`,
      {
        ...options,
        credentials: 'include',
        headers: {
          ...options?.headers,
          'X-CSRF-TOKEN': await csrfToken(),
          cache: 'force-cache',
        },
      },
    );
    if (isUnauthenticated(response)) {
      redirectToLogin();
    }
    if (response.status === 403) {
      return Promise.reject(new PermissionError());
    }
    return response.status >= 400
      ? Promise.reject(new FetchError(response, (await response.text()) ?? ''))
      : Promise.resolve(response);
  } catch (e) {
    console.error('Fetching data failed!');
    return Promise.reject(e);
  }
}

const isUnauthenticated = (response: Response) => {
  return response?.status === 401;
};

const isRedirected = (response: Response) => {
  return response.redirected;
};

const redirectToLogin = () => {
  location.assign(loginUrl);
};

const noContent = (response: Response) => {
  return response.status === 204;
};

const responseToData = async (res: Response) => {
  if (noContent(res)) {
    return {};
  }
  try {
    const result = await res.json();
    return result;
  } catch (e) {
    console.error('Parsing fetch response body as JSON failed!');
    return Promise.reject(e);
  }
};

export const doApiFetch = async (
  resource: string,
  options?: { headers?: object },
) => {
  try {
    const response = await apiFetch(resource, options);
    const responseUrl = new URL(response.url);
    if (
      isRedirected(response) &&
      responseUrl.pathname.startsWith('/cas/login')
    ) {
      redirectToLogin();
    }
    return responseToData(response);
  } catch (error: unknown) {
    return Promise.reject(error);
  }
};
