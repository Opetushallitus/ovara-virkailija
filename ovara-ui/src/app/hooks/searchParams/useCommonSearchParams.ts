'use client';
import { parseAsArrayOf, parseAsString, useQueryState } from 'nuqs';
import { useQueryStateWithLocalStorage } from '@/app/hooks/searchParams/useQueryStateWithLocalStorage';
import { createNullableBooleanOptions } from './paramUtil';

export const useCommonSearchParams = () => {
  const [selectedAlkamiskaudet, setSelectedAlkamiskaudet] =
    useQueryStateWithLocalStorage('ovara_alkamiskaudet', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedHaut, setSelectedHaut] = useQueryStateWithLocalStorage(
    'ovara_haut',
    {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    },
  );

  const [haunTyyppi, setHauntyyppi] = useQueryState('ovara_haun_tyyppi');

  const [selectedKoulutustoimija, setSelectedKoulutustoimija] =
    useQueryStateWithLocalStorage<string | null>('ovara_koulutustoimija', {
      parse: (value) => (value === null ? null : String(value)), // Handle null and string values
      defaultValue: null,
    });

  const [selectedOppilaitokset, setSelectedOppilaitokset] =
    useQueryStateWithLocalStorage('ovara_oppilaitokset', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedToimipisteet, setSelectedToimipisteet] =
    useQueryStateWithLocalStorage('ovara_toimipisteet', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedKoulutuksenTila, setSelectedKoulutuksenTila] =
    useQueryStateWithLocalStorage<string | null>('ovara_koulutuksen-tila', {
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: null,
    });

  const [selectedToteutuksenTila, setSelectedToteutuksenTila] =
    useQueryStateWithLocalStorage<string | null>('ovara_toteutuksen-tila', {
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: null,
    });

  const [selectedHakukohteenTila, setSelectedHakukohteenTila] =
    useQueryStateWithLocalStorage<string | null>('ovara_hakukohteen-tila', {
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: null,
    });

  const [selectedValintakoe, setSelectedValintakoe] =
    useQueryStateWithLocalStorage<boolean | null>(
      'ovara_valintakoe',
      createNullableBooleanOptions(null),
    );

  const [selectedHakukohteet, setSelectedHakukohteet] =
    useQueryStateWithLocalStorage('ovara_hakukohteet', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedHarkinnanvaraisuudet, setSelectedHarkinnanvaraisuudet] =
    useQueryStateWithLocalStorage('ovara_harkinnanvaraisuudet', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedKansalaisuusluokat, setSelectedKansalaisuusluokat] =
    useQueryStateWithLocalStorage('ovara_kansalaisuusluokat', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedHakukohderyhmat, setSelectedHakukohderyhmat] =
    useQueryStateWithLocalStorage('ovara_hakukohderyhmat', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const emptyAllCommonParams = () => {
    const keysToClear = [
      'ovara_alkamiskaudet',
      'ovara_haut',
      'ovara_koulutustoimija',
      'ovara_oppilaitokset',
      'ovara_toimipisteet',
      'ovara_koulutuksen-tila',
      'ovara_toteutuksen-tila',
      'ovara_hakukohteen-tila',
      'ovara_valintakoe',
      'ovara_hakukohteet',
      'ovara_harkinnanvaraisuudet',
      'ovara_kansalaisuusluokat',
      'ovara_hakukohderyhmat',
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
