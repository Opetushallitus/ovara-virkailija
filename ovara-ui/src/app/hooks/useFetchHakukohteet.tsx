import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';

const fetchHakukohteet = (queryParamsStr: string | null) => {
  return doApiFetch('hakukohteet', {
    queryParams: queryParamsStr ? `?${queryParamsStr}` : null,
  });
};

export const useFetchHakukohteet = (queryParamsStr: string | null) => {
  return useQuery({
    queryKey: ['fetchHakukohteet', queryParamsStr],
    queryFn: () => fetchHakukohteet(queryParamsStr),
  });
};
