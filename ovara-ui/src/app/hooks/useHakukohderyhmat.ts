import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';

const fetchHakukohderyhmat = (queryParamsStr: string | null) => {
  return doApiFetch('hakukohderyhmat', {
    queryParams: queryParamsStr ? `?${queryParamsStr}` : null,
  });
};

export const useFetchHakukohderyhmat = (
  hautQueryParams: string | null,
  fetchEnabled: boolean,
) => {
  return useQuery({
    queryKey: ['fetchHakukohderyhmat', hautQueryParams],
    queryFn: () => fetchHakukohderyhmat(hautQueryParams),
    enabled: fetchEnabled,
  });
};
