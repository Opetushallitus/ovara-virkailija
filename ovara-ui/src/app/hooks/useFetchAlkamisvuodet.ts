import { useEffect, useState } from 'react';
import { apiFetch } from '@/app/lib/ovara-backend/api';

export function useFetchAlkamisvuodet() {
  const [alkamisvuodet, setAlkamisvuodet] = useState<Array<string> | null>(
    null,
  );

  useEffect(() => {
    async function fetchAlkamisvuodet() {
      const response = await apiFetch('alkamisvuodet');
      const alkamisvuodet = await response.json();
      setAlkamisvuodet(alkamisvuodet);
    }

    fetchAlkamisvuodet();
  }, []);

  return alkamisvuodet;
}
