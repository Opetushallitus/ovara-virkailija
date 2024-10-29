let _csrfToken: string;

async function csrfToken() {
  if (!_csrfToken) {
    const response = await fetch('/ovara-backend/api/csrf', {
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
  const response = await fetch(`/ovara-backend/api/${resource}`, {
    ...options,
    credentials: 'include',
    headers: {
      ...options?.headers,
      'X-CSRF-TOKEN': await csrfToken(),
    },
  });

  if (response.status === 401) {
    location.assign('/ovara-backend/api/login');
  }
  return response;
}
