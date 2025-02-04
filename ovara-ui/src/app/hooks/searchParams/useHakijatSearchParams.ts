'use client';
import {
  parseAsArrayOf,
  parseAsBoolean,
  parseAsString,
  useQueryState,
} from 'nuqs';
import { DEFAULT_NUQS_OPTIONS } from '@/app/lib/constants';

export const useHakijatSearchParams = () => {
  const [selectedVastaanottotieto, setSelectedVastaanottotieto] = useQueryState(
    'vastaanottotieto',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedMarkkinointilupa, setSelectedMarkkinointilupa] = useQueryState(
    'markkinointilupa',
    parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedJulkaisulupa, setSelectedJulkaisulupa] = useQueryState(
    'julkaisulupa',
    parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS),
  );

  return {
    selectedVastaanottotieto,
    setSelectedVastaanottotieto,
    selectedMarkkinointilupa,
    setSelectedMarkkinointilupa,
    selectedJulkaisulupa,
    setSelectedJulkaisulupa,
  };
};
