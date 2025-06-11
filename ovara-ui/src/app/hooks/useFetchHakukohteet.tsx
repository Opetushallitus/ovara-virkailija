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
    'ovara_koulutustoimija',
    selectedKoulutustoimija,
  );
  const oppilaitoksetQueryStr = createQueryParamStr(
    'ovara_oppilaitokset',
    selectedOppilaitokset,
  );
  const toimipisteetQueryStr = createQueryParamStr(
    'ovara_toimipisteet',
    selectedToimipisteet,
  );
  const hakukohderyhmatQueryStr = createQueryParamStr(
    'ovara_hakukohderyhmat',
    selectedHakukohderyhmat,
  );
  const hakukohteetQueryStr = createQueryParamStr(
    'ovara_hakukohteet',
    selectedHakukohteet,
  );

  const paramsStr = `?ovara_haut=${selectedHaut?.toString()}${koulutustoimijatQueryStr}${oppilaitoksetQueryStr}${toimipisteetQueryStr}${hakukohderyhmatQueryStr}${hakukohteetQueryStr}`;

  return doApiFetch('hakukohteet', {
    queryParams: paramsStr ?? null,
  });
};

export const useFetchHakukohteet = (
  fetchEnabled: boolean,
  includeKoulutustoimija: boolean = false,
  includeHakukohderyhma: boolean = false,
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
        selectedHakukohderyhmat: includeHakukohderyhma
          ? selectedHakukohderyhmat
          : null,
        selectedHakukohteet,
      },
    ],
    queryFn: () =>
      fetchHakukohteet(
        selectedHaut,
        includeKoulutustoimija ? selectedKoulutustoimija : null,
        selectedOppilaitokset,
        selectedToimipisteet,
        includeHakukohderyhma ? selectedHakukohderyhmat : null,
        selectedHakukohteet,
      ),
    enabled: fetchEnabled,
  });
};
