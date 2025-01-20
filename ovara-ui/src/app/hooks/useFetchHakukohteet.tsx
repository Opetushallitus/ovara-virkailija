import { useEffect, useState } from 'react';
import { apiFetch } from '@/app/lib/ovara-backend/api';
import { Kielistetty } from '@/app/lib/types/common';
import { useSearchParams } from 'next/navigation';

type Hakukohde = {
  hakukohde_oid: string;
  hakukohde_nimi: Kielistetty;
};

export function useFetchHakukohteet() {
  const [hakukohteet, setHakukohteet] = useState<Array<Hakukohde> | null>(null);
  const queryParams = useSearchParams();

  useEffect(() => {
    async function fetchHakukohteet() {
      const response = await apiFetch('hakukohteet?' + queryParams);
      if (response.status === 200) {
        const hakukohteet = await response.json();
        setHakukohteet(hakukohteet);
      } else {
        setHakukohteet(null);
      }
    }

    fetchHakukohteet();
  }, [queryParams]);

  return hakukohteet;
}
