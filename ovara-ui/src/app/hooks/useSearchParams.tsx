'use client';
import {
  parseAsArrayOf,
  parseAsBoolean,
  parseAsString,
  useQueryState,
} from 'nuqs';

export const DEFAULT_NUQS_OPTIONS = {
  history: 'push',
  clearOnDefault: true,
} as const;

export const useSearchParams = () => {
  const [selectedAlkamiskaudet, setSelectedAlkamiskaudet] = useQueryState(
    'alkamiskausi',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedHaut, setSelectedHaut] = useQueryState(
    'haku',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedKoulutuksenTila, setSelectedKoulutuksenTila] = useQueryState(
    'koulutuksen-tila',
    DEFAULT_NUQS_OPTIONS,
  );

  const [selectedToteutuksenTila, setSelectedToteutuksenTila] = useQueryState(
    'toteutuksen-tila',
    DEFAULT_NUQS_OPTIONS,
  );

  const [selectedHakukohteenTila, setSelectedHakukohteenTila] = useQueryState(
    'hakukohteen-tila',
    DEFAULT_NUQS_OPTIONS,
  );

  const [selectedValintakoe, setSelectedValintakoe] = useQueryState(
    'valintakoe',
    parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS),
  );

  return {
    selectedAlkamiskaudet,
    setSelectedAlkamiskaudet,
    selectedHaut,
    setSelectedHaut,
    selectedKoulutuksenTila,
    setSelectedKoulutuksenTila,
    selectedToteutuksenTila,
    setSelectedToteutuksenTila,
    selectedHakukohteenTila,
    setSelectedHakukohteenTila,
    selectedValintakoe,
    setSelectedValintakoe,
  };
};
