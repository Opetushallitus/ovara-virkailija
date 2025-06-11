import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { isNullish } from 'remeda';
import { isNullishOrEmpty } from '../lib/utils';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';

const fetchHaut = (
  selectedAlkamiskaudet: Array<string> | null,
  haunTyyppi: string | null,
) => {
  const paramsStr = `?ovara_haun_tyyppi=${haunTyyppi}&alkamiskaudet=${selectedAlkamiskaudet?.toString()}`;
  return doApiFetch('haut', {
    queryParams: paramsStr ?? null,
  });
};

export const useFetchHaut = () => {
  const { selectedAlkamiskaudet, haunTyyppi } = useCommonSearchParams();

  const fetchEnabled =
    !isNullishOrEmpty(selectedAlkamiskaudet) && !isNullish(haunTyyppi);

  return useQuery({
    queryKey: ['fetchHaut', { selectedAlkamiskaudet, haunTyyppi }],
    queryFn: () => fetchHaut(selectedAlkamiskaudet, haunTyyppi),
    enabled: fetchEnabled,
  });
};
