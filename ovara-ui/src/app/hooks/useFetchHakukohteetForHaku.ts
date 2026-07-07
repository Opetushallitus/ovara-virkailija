import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { isNullish } from 'remeda';

/**
 * Fetch hakukohteet scoped to a single hakuOid — decoupled from the
 * shared `useCommonSearchParams` state (which stores selectedHaut as an
 * array). Used by pages that submit a single hakuOid to the backend.
 */
export const useFetchHakukohteetForHaku = (hakuOid: string | null) => {
  return useQuery({
    queryKey: ['fetchHakukohteetForHaku', { hakuOid }],
    queryFn: () =>
      doApiFetch('hakukohteet', {
        queryParams: `?ovara_haut=${hakuOid}`,
      }),
    enabled: !isNullish(hakuOid),
    staleTime: 5 * 60 * 1000,
    gcTime: 30 * 60 * 1000,
  });
};
