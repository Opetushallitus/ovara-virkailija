'use client';
import {
  parseAsArrayOf,
  parseAsBoolean,
  parseAsString,
  useQueryState,
} from 'nuqs';
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

  const [selectedMarkkinointilupa, setSelectedMarkkinointilupa] = useQueryState(
    'markkinointilupa',
    parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedJulkaisulupa, setSelectedJulkaisulupa] = useQueryState(
    'julkaisulupa',
    parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedValintatieto, setSelectedValintatieto] = useQueryState(
    'valintatieto',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedKaksoistutkinto, setSelectedKaksoistutkinto] = useQueryState(
    'kaksoistutkinto',
    parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedSoraTerveys, setSelectedSoraTerveys] = useQueryState(
    'sora_terveys',
    parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedSoraAiempi, setSelectedSoraAiempi] = useQueryState(
    'sora_aiempi',
    parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS),
  );

  return {
    selectedHakukohteet,
    setSelectedHakukohteet,
    selectedVastaanottotieto,
    setSelectedVastaanottotieto,
    selectedMarkkinointilupa,
    setSelectedMarkkinointilupa,
    selectedJulkaisulupa,
    setSelectedJulkaisulupa,
    selectedValintatieto,
    setSelectedValintatieto,
    selectedKaksoistutkinto,
    setSelectedKaksoistutkinto,
    selectedSoraTerveys,
    setSelectedSoraTerveys,
    selectedSoraAiempi,
    setSelectedSoraAiempi,
  };
};
