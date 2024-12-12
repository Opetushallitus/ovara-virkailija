import { useEffect, useState } from 'react';
import { apiFetch } from '@/app/lib/ovara-backend/api';
import { Organisaatio } from '@/app/lib/types/common';

type OrganisaatiotByTyyppi = {
  '01': Array<Organisaatio>;
  '02': Array<Organisaatio>;
  '03': Array<Organisaatio>;
};

export function useFetchOrganisaatiotByOrganisaatiotyyppi() {
  const [organisaatiot, setOrganisaatiot] =
    useState<OrganisaatiotByTyyppi | null>(null);

  useEffect(() => {
    async function fetchOrganisaatiotByOrganisaatiotyyppi() {
      const response = await apiFetch('organisaatiot');
      if (response.status === 200) {
        const organisaatiot = await response.json();
        setOrganisaatiot(organisaatiot);
      } else {
        setOrganisaatiot(null);
      }
    }

    fetchOrganisaatiotByOrganisaatiotyyppi();
  }, []);

  return organisaatiot;
}
