'use client';
import { parseAsArrayOf, parseAsString, useQueryState } from 'nuqs';
import { useQueryStateWithLocalStorage } from '@/app/hooks/searchParams/useQueryStateWithLocalStorage';
import { createNullableBooleanOptions } from './paramUtil';

export const useCommonSearchParams = () => {
  const [selectedAlkamiskaudet, setSelectedAlkamiskaudet] =
    useQueryStateWithLocalStorage('alkamiskaudet', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedHaut, setSelectedHaut] = useQueryStateWithLocalStorage(
    'haut',
    {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    },
  );

  const [haunTyyppi, setHauntyyppi] = useQueryState('haun_tyyppi');

  const [selectedKoulutustoimija, setSelectedKoulutustoimija] =
    useQueryStateWithLocalStorage<string | null>('koulutustoimija', {
      parse: (value) => (value === null ? null : String(value)), // Handle null and string values
      defaultValue: null,
    });

  const [selectedOppilaitokset, setSelectedOppilaitokset] =
    useQueryStateWithLocalStorage('oppilaitokset', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedToimipisteet, setSelectedToimipisteet] =
    useQueryStateWithLocalStorage('toimipisteet', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedKoulutuksenTila, setSelectedKoulutuksenTila] =
    useQueryStateWithLocalStorage<string | null>('koulutuksen-tila', {
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: null,
    });

  const [selectedToteutuksenTila, setSelectedToteutuksenTila] =
    useQueryStateWithLocalStorage<string | null>('toteutuksen-tila', {
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: null,
    });

  const [selectedHakukohteenTila, setSelectedHakukohteenTila] =
    useQueryStateWithLocalStorage<string | null>('hakukohteen-tila', {
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: null,
    });

  const [selectedValintakoe, setSelectedValintakoe] =
    useQueryStateWithLocalStorage<boolean | null>(
      'valintakoe',
      createNullableBooleanOptions(null),
    );

  const [selectedHakukohteet, setSelectedHakukohteet] =
    useQueryStateWithLocalStorage('hakukohteet', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedHarkinnanvaraisuudet, setSelectedHarkinnanvaraisuudet] =
    useQueryStateWithLocalStorage('harkinnanvaraisuudet', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedKansalaisuusluokat, setSelectedKansalaisuusluokat] =
    useQueryStateWithLocalStorage('kansalaisuusluokat', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedHakukohderyhmat, setSelectedHakukohderyhmat] =
    useQueryStateWithLocalStorage('hakukohderyhmat', {
      ...parseAsArrayOf(parseAsString),
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
