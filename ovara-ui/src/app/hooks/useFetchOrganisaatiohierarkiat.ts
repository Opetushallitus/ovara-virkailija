import { useEffect, useState } from 'react';
import { apiFetch } from '@/app/lib/ovara-backend/api';
import { OrganisaatioHierarkia } from '@/app/lib/types/common';

export function useFetchOrganisaatiohierarkiat() {
  const [organisaatiot, setOrganisaatiot] =
    useState<Array<OrganisaatioHierarkia> | null>(null);

  useEffect(() => {
    async function fetchOrganisaatiohierarkiat() {
      const response = await apiFetch('organisaatiot');
      if (response.status === 200) {
        const organisaatiot = await response.json();
        setOrganisaatiot(organisaatiot);
      } else {
        setOrganisaatiot(null);
      }
    }

    fetchOrganisaatiohierarkiat();
  }, []);

  return organisaatiot;
}
