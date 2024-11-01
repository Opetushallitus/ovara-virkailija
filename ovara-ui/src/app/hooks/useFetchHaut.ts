import { useEffect, useState } from 'react';
import { apiFetch } from '@/app/lib/ovara-backend/api';
import { Kielistetty } from '@/app/lib/types/common';

type Haku = {
  haku_oid: string;
  haku_nimi: Kielistetty;
};

export function useFetchHaut() {
  const [haut, setHaut] = useState<Array<Haku> | null>(null);

  useEffect(() => {
    async function fetchHaut() {
      const response = await apiFetch('haut');
      if (response.status === 200) {
        const haut = await response.json();
        setHaut(haut);
      } else {
        setHaut(null);
      }
    }

    fetchHaut();
  }, []);

  return haut;
}
