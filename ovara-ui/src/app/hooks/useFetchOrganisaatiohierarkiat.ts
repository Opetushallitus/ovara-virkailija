import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { useQuery } from '@tanstack/react-query';

const fetchOrganisaatiohierarkiat = () => {
  return doApiFetch('organisaatiot');
};

export function useFetchOrganisaatiohierarkiat() {
  return useQuery({
    queryKey: ['fetchOrganisaatiohierarkiat'],
    queryFn: () => fetchOrganisaatiohierarkiat(),
  });
}
