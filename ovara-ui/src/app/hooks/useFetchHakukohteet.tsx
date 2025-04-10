import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { isNullishOrEmpty } from '../lib/utils';

const createQueryParamStr = (
  paramName: string,
  selectedValues: Array<string> | null,
) => {
  return isNullishOrEmpty(selectedValues)
    ? ''
    : `&${paramName}=${selectedValues?.toString()}`;
};

const fetchHakukohteet = (
  selectedHaut: Array<string> | null,
  selectedOppilaitokset: Array<string> | null,
  selectedToimipisteet: Array<string> | null,
  selectedHakukohderyhmat: Array<string> | null,
  selectedHakukohteet: Array<string> | null,
) => {
  const oppilaitoksetQueryStr = createQueryParamStr(
    'oppilaitokset',
    selectedOppilaitokset,
  );
  const toimipisteetQueryStr = createQueryParamStr(
    'toimipisteet',
    selectedToimipisteet,
  );
  const hakukohderyhmatQueryStr = createQueryParamStr(
    'hakukohderyhmat',
    selectedHakukohderyhmat,
  );
  const hakukohteetQueryStr = createQueryParamStr(
    'hakukohteet',
    selectedHakukohteet,
  );

  const paramsStr = `?haut=${selectedHaut?.toString()}${oppilaitoksetQueryStr}${toimipisteetQueryStr}${hakukohderyhmatQueryStr}${hakukohteetQueryStr}`;

  return doApiFetch('hakukohteet', {
    queryParams: paramsStr ?? null,
  });
};

export const useFetchHakukohteet = () => {
  const {
    selectedHaut,
    selectedOppilaitokset,
    selectedToimipisteet,
    selectedHakukohderyhmat,
    selectedHakukohteet,
  } = useCommonSearchParams();

  const fetchEnabled = !isNullishOrEmpty(selectedHaut);
  return useQuery({
    queryKey: [
      'fetchHakukohteet',
      {
        selectedHaut,
        selectedOppilaitokset,
        selectedToimipisteet,
        selectedHakukohderyhmat,
        selectedHakukohteet,
      },
    ],
    queryFn: () =>
      fetchHakukohteet(
        selectedHaut,
        selectedOppilaitokset,
        selectedToimipisteet,
        selectedHakukohderyhmat,
        selectedHakukohteet,
      ),
    enabled: fetchEnabled,
  });
};
