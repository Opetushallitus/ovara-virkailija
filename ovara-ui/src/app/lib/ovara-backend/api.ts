export async function apiFetch(resource: string, options?: object) {
  const response = await fetch(`/ovara-backend/api/${resource}`, {
    ...options,
    credentials: 'include',
  });

  if (response.status === 401) {
    location.assign('/ovara-backend/api/login');
  }
  return response;
}
