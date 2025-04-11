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
    'alkamiskaudet',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedHaut, setSelectedHaut] = useQueryState(
    'haut',
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
    'oppilaitokset',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedToimipisteet, setSelectedToimipisteet] = useQueryState(
    'toimipisteet',
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
    'hakukohteet',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedHarkinnanvaraisuudet, setSelectedHarkinnanvaraisuudet] =
    useQueryState(
      'harkinnanvaraisuudet',
      parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
    );

  const [selectedKansalaisuusluokat, setSelectedKansalaisuusluokat] =
    useQueryState(
      'kansalaisuusluokat',
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
