'use client';
import {
  parseAsArrayOf,
  parseAsBoolean,
  parseAsString,
  useQueryState,
} from 'nuqs';
import { DEFAULT_NUQS_OPTIONS } from '@/app/lib/constants';

export const useHakijatSearchParams = () => {
  const [selectedPohjakoulutukset, setSelectedPohjakoulutukset] = useQueryState(
    'pohjakoulutus',
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

  const [selectedUrheilijatutkinto, setSelectedUrheilijatutkinto] =
    useQueryState(
      'urheilijatutkinto',
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

  const [selectedNaytaYoArvosanat, setSelectedNaytaYoArvosanat] = useQueryState(
    'nayta-yo-arvosanat',
    parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS).withDefault(true),
  );

  const [selectedNaytaHetu, setSelectedNaytaHetu] = useQueryState(
    'nayta-hetu',
    parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS).withDefault(true),
  );

  const [selectedNaytaPostiosoite, setSelectedNaytaPostiosoite] = useQueryState(
    'nayta-postiosoite',
    parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS).withDefault(true),
  );

  return {
    selectedPohjakoulutukset,
    setSelectedPohjakoulutukset,
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
    selectedUrheilijatutkinto,
    setSelectedUrheilijatutkinto,
    selectedSoraTerveys,
    setSelectedSoraTerveys,
    selectedSoraAiempi,
    setSelectedSoraAiempi,
    selectedNaytaYoArvosanat,
    setSelectedNaytaYoArvosanat,
    selectedNaytaHetu,
    setSelectedNaytaHetu,
    selectedNaytaPostiosoite,
    setSelectedNaytaPostiosoite,
  };
};
