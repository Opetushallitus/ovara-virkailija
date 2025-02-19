import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';

const fetchHakukohteet = (queryParamsStr: string | null) => {
  return doApiFetch('hakukohteet', {
    queryParams: queryParamsStr ? `?${queryParamsStr}` : null,
  });
};

export const useFetchHakukohteet = (
  queryParamsStr: string | null,
  fetchEnabled: boolean,
) => {
  return useQuery({
    queryKey: ['fetchHakukohteet', queryParamsStr],
    queryFn: () => fetchHakukohteet(queryParamsStr),
    enabled: fetchEnabled,
  });
};
