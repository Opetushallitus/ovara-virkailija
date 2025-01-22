'use client';
import { parseAsArrayOf, parseAsString, useQueryState } from 'nuqs';
import { DEFAULT_NUQS_OPTIONS } from '@/app/lib/constants';

export const useHakijatSearchParams = () => {
  const [selectedHakukohteet, setSelectedHakukohteet] = useQueryState(
    'hakukohde',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedVastaanottotieto, setSelectedVastaanottotieto] = useQueryState(
    'vastaanottotieto',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  return {
    selectedHakukohteet,
    setSelectedHakukohteet,
    selectedVastaanottotieto,
    setSelectedVastaanottotieto,
  };
};
