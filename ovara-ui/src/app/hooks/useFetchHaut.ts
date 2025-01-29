import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';

const fetchHaut = (alkamiskaudetQueryParams: string | null) => {
  return doApiFetch('haut', {
    queryParams: alkamiskaudetQueryParams
      ? `?alkamiskausi=${alkamiskaudetQueryParams}`
      : null,
  });
};

export const useFetchHaut = (alkamiskaudetQueryParams: string | null) => {
  return useQuery({
    queryKey: ['fetchHaut', alkamiskaudetQueryParams],
    queryFn: () => fetchHaut(alkamiskaudetQueryParams),
  });
};
