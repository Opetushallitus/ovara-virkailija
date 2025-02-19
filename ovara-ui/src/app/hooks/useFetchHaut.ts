import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';

const fetchHaut = (queryParamsStr: string | null) => {
  return doApiFetch('haut', {
    queryParams: queryParamsStr ? `?${queryParamsStr}` : null,
  });
};

export const useFetchHaut = (
  alkamiskaudetQueryParams: string | null,
  fetchEnabled: boolean,
) => {
  return useQuery({
    queryKey: ['fetchHaut', alkamiskaudetQueryParams],
    queryFn: () => fetchHaut(alkamiskaudetQueryParams),
    enabled: fetchEnabled,
  });
};
