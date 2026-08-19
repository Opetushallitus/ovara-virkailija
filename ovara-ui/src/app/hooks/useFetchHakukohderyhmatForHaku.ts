import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { isNullish } from 'remeda';

/**
 * Fetch hakukohderyhmat for a single hakuOid — decoupled from the shared
 * `useCommonSearchParams` state (which stores selectedHaut as an array). Used by
 * pages that submit a single hakuOid to the backend. Options stay empty until
 * haku is set.
 */
export const useFetchHakukohderyhmatForHaku = (hakuOid: string | null) => {
  return useQuery({
    queryKey: ['fetchHakukohderyhmatForHaku', hakuOid],
    queryFn: () =>
      doApiFetch('hakukohderyhmat', {
        queryParams: `?ovara_haut=${hakuOid}`,
      }),
    enabled: !isNullish(hakuOid),
    staleTime: 5 * 60 * 1000,
    gcTime: 30 * 60 * 1000,
  });
};
