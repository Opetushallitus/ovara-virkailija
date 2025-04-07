'use client';
import {
  parseAsArrayOf,
  parseAsBoolean,
  parseAsString,
  useQueryState,
} from 'nuqs';
import { DEFAULT_NUQS_OPTIONS } from '@/app/lib/constants';

export const useCommonSearchParams = () => {
  const [selectedAlkamiskaudet, setSelectedAlkamiskaudet] = useQueryState(
    'alkamiskausi',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedHaut, setSelectedHaut] = useQueryState(
    'haku',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [haunTyyppi, setHauntyyppi] = useQueryState(
    'haun_tyyppi',
    DEFAULT_NUQS_OPTIONS,
  );

  const [selectedKoulutustoimija, setSelectedKoulutustoimija] = useQueryState(
    'koulutustoimija',
    DEFAULT_NUQS_OPTIONS,
  );

  const [selectedOppilaitokset, setSelectedOppilaitokset] = useQueryState(
    'oppilaitos',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedToimipisteet, setSelectedToimipisteet] = useQueryState(
    'toimipiste',
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

  const [selectedHakukohteet, setSelectedHakukohteet] = useQueryState(
    'hakukohde',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedHarkinnanvaraisuus, setSelectedHarkinnanvaraisuus] =
    useQueryState(
      'harkinnanvaraisuus',
      parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
    );

  const [selectedKansalaisuus, setSelectedKansalaisuus] = useQueryState(
    'kansalaisuus',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedHakukohderyhmat, setSelectedHakukohderyhmat] = useQueryState(
    'hakukohderyhmat',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const emptyAllCommonParams = () => {
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
    setSelectedHarkinnanvaraisuus(null);
    setSelectedKansalaisuus(null);
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
    selectedHarkinnanvaraisuus,
    setSelectedHarkinnanvaraisuus,
    selectedKansalaisuus,
    setSelectedKansalaisuus,
    selectedHakukohderyhmat,
    setSelectedHakukohderyhmat,
    emptyAllCommonParams,
    haunTyyppi,
    setHauntyyppi,
  };
};
