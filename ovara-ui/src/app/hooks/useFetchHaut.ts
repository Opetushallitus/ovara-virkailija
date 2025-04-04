import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { isNullish } from 'remeda';

const fetchHaut = (queryParamsStr: string | null) => {
  return doApiFetch('haut', {
    queryParams: queryParamsStr ? `?${queryParamsStr}` : null,
  });
};

export const useFetchHaut = (
  alkamiskaudetQueryParams: string | null,
  fetchEnabled: boolean,
) => {
  const { selectedAlkamiskaudet } = useCommonSearchParams();
  const hasSelectedAlkamiskaudet =
    !isNullish(selectedAlkamiskaudet) && selectedAlkamiskaudet.length > 0;
  // lisävarmistus koska muuten hakee pelkällä haun tyyppi -parametrilla
  // ennenkuin valittu alkamiskausi on ehtinyt päivittyä urliin
  const hasAlkamiskaudetInQuery = alkamiskaudetQueryParams
    ?.split('&')
    .some((param) => param.startsWith('alkamiskausi='));
  const shouldFetch =
    fetchEnabled && hasSelectedAlkamiskaudet && hasAlkamiskaudetInQuery;
  return useQuery({
    queryKey: ['fetchHaut', alkamiskaudetQueryParams],
    queryFn: () => fetchHaut(alkamiskaudetQueryParams),
    enabled: shouldFetch,
  });
};
