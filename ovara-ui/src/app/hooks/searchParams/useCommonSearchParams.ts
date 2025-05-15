'use client';
import {
  parseAsArrayOf,
  parseAsBoolean,
  parseAsString,
  useQueryState,
} from 'nuqs';
import { DEFAULT_NUQS_OPTIONS } from '@/app/lib/constants';
import { useQueryStateWithLocalStorage } from '@/app/hooks/searchParams/useQueryStateWithLocalStorage';

export const useCommonSearchParams = () => {
  const [selectedAlkamiskaudet, setSelectedAlkamiskaudet] =
    useQueryStateWithLocalStorage('alkamiskaudet', {
      ...parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
      defaultValue: [],
    });

  const [selectedHaut, setSelectedHaut] = useQueryStateWithLocalStorage(
    'haut',
    {
      ...parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
      defaultValue: [],
    },
  );

  const [haunTyyppi, setHauntyyppi] = useQueryState(
    'haun_tyyppi',
    DEFAULT_NUQS_OPTIONS,
  );

  const [selectedKoulutustoimija, setSelectedKoulutustoimija] =
    useQueryStateWithLocalStorage<string | null>('koulutustoimija', {
      ...DEFAULT_NUQS_OPTIONS,
      parse: (value) => (value === null ? null : String(value)), // Handle null and string values
      defaultValue: null,
    });

  const [selectedOppilaitokset, setSelectedOppilaitokset] =
    useQueryStateWithLocalStorage('oppilaitokset', {
      ...parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
      defaultValue: [],
    });

  const [selectedToimipisteet, setSelectedToimipisteet] =
    useQueryStateWithLocalStorage('toimipisteet', {
      ...parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
      defaultValue: [],
    });

  const [selectedKoulutuksenTila, setSelectedKoulutuksenTila] =
    useQueryStateWithLocalStorage<string | null>('koulutuksen-tila', {
      ...DEFAULT_NUQS_OPTIONS,
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: null,
    });

  const [selectedToteutuksenTila, setSelectedToteutuksenTila] =
    useQueryStateWithLocalStorage<string | null>('toteutuksen-tila', {
      ...DEFAULT_NUQS_OPTIONS,
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: null,
    });

  const [selectedHakukohteenTila, setSelectedHakukohteenTila] =
    useQueryStateWithLocalStorage<string | null>('hakukohteen-tila', {
      ...DEFAULT_NUQS_OPTIONS,
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: null,
    });

  // TODO fix boolean parsing
  const [selectedValintakoe, setSelectedValintakoe] = useQueryState(
    'valintakoe',
    parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedHakukohteet, setSelectedHakukohteet] =
    useQueryStateWithLocalStorage('hakukohteet', {
      ...parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
      defaultValue: [],
    });

  const [selectedHarkinnanvaraisuudet, setSelectedHarkinnanvaraisuudet] =
    useQueryStateWithLocalStorage('harkinnanvaraisuudet', {
      ...parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
      defaultValue: [],
    });

  const [selectedKansalaisuusluokat, setSelectedKansalaisuusluokat] =
    useQueryStateWithLocalStorage('kansalaisuusluokat', {
      ...parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
      defaultValue: [],
    });

  const [selectedHakukohderyhmat, setSelectedHakukohderyhmat] =
    useQueryStateWithLocalStorage('hakukohderyhmat', {
      ...parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
      defaultValue: [],
    });

  const emptyAllCommonParams = () => {
    const keysToClear = [
      'alkamiskaudet',
      'haut',
      'koulutustoimija',
      'oppilaitokset',
      'toimipisteet',
      'koulutuksen-tila',
      'toteutuksen-tila',
      'hakukohteen-tila',
      'valintakoe',
      'hakukohteet',
      'harkinnanvaraisuudet',
      'kansalaisuusluokat',
      'hakukohderyhmat',
    ];

    keysToClear.forEach((key) => localStorage.removeItem(key));
    setSelectedAlkamiskaudet(null);
    setSelectedHaut(null);
    setSelectedKoulutustoimija(null);
    setSelectedOppilaitokset(null);
    setSelectedToimipisteet(null);
    setSelectedKoulutuksenTila(null);
    setSelectedToteutuksenTila(null);
    setSelectedHakukohteenTila(null);
    setSelectedValintakoe(null);
    setSelectedHakukohteet(null);
    setSelectedHarkinnanvaraisuudet(null);
    setSelectedKansalaisuusluokat(null);
    setSelectedHakukohderyhmat(null);
  };

  return {
    selectedAlkamiskaudet,
    setSelectedAlkamiskaudet,
    selectedHaut,
    setSelectedHaut,
    selectedKoulutustoimija,
    setSelectedKoulutustoimija,
    selectedOppilaitokset,
    setSelectedOppilaitokset,
    selectedToimipisteet,
    setSelectedToimipisteet,
    selectedKoulutuksenTila,
    setSelectedKoulutuksenTila,
    selectedToteutuksenTila,
    setSelectedToteutuksenTila,
    selectedHakukohteenTila,
    setSelectedHakukohteenTila,
    selectedValintakoe,
    setSelectedValintakoe,
    selectedHakukohteet,
    setSelectedHakukohteet,
    selectedHarkinnanvaraisuudet,
    setSelectedHarkinnanvaraisuudet,
    selectedKansalaisuusluokat,
    setSelectedKansalaisuusluokat,
    selectedHakukohderyhmat,
    setSelectedHakukohderyhmat,
    emptyAllCommonParams,
    haunTyyppi,
    setHauntyyppi,
  };
};
