import { configuration } from '../configuration';

let _csrfToken: string;

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

  if (response.status === 401) {
    location.assign(`${configuration.ovaraBackendApiUrl}/login`);
  }

  return response;
}
