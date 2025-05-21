import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { useCommonSearchParams } from '@/app/hooks/searchParams/useCommonSearchParams';
import { isNullishOrEmpty } from '../lib/utils';
import { isNullish } from 'remeda';

const createQueryParamStr = (
  paramName: string,
  selectedValues: Array<string> | null,
) => {
  return isNullishOrEmpty(selectedValues)
    ? ''
    : `&${paramName}=${selectedValues?.toString()}`;
};

const createQueryParam = (paramName: string, selectedValue: string | null) => {
  return isNullish(selectedValue)
    ? ''
    : `&${paramName}=${selectedValue?.toString()}`;
};

const fetchHakukohteet = (
  selectedHaut: Array<string> | null,
  selectedKoulutustoimija: string | null,
  selectedOppilaitokset: Array<string> | null,
  selectedToimipisteet: Array<string> | null,
  selectedHakukohderyhmat: Array<string> | null,
  selectedHakukohteet: Array<string> | null,
) => {
  const koulutustoimijatQueryStr = createQueryParam(
    'koulutustoimija',
    selectedKoulutustoimija,
  );
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

  const paramsStr = `?haut=${selectedHaut?.toString()}${koulutustoimijatQueryStr}${oppilaitoksetQueryStr}${toimipisteetQueryStr}${hakukohderyhmatQueryStr}${hakukohteetQueryStr}`;

  return doApiFetch('hakukohteet', {
    queryParams: paramsStr ?? null,
  });
};

export const useFetchHakukohteet = (
  fetchEnabled: boolean,
  includeKoulutustoimija: boolean = false,
) => {
  const {
    selectedHaut,
    selectedKoulutustoimija,
    selectedOppilaitokset,
    selectedToimipisteet,
    selectedHakukohderyhmat,
    selectedHakukohteet,
  } = useCommonSearchParams();

  return useQuery({
    queryKey: [
      'fetchHakukohteet',
      {
        selectedHaut,
        selectedKoulutustoimija: includeKoulutustoimija
          ? selectedKoulutustoimija
          : null,
        selectedOppilaitokset,
        selectedToimipisteet,
        selectedHakukohderyhmat,
        selectedHakukohteet,
      },
    ],
    queryFn: () =>
      fetchHakukohteet(
        selectedHaut,
        includeKoulutustoimija ? selectedKoulutustoimija : null,
        selectedOppilaitokset,
        selectedToimipisteet,
        selectedHakukohderyhmat,
        selectedHakukohteet,
      ),
    enabled: fetchEnabled,
  });
};
