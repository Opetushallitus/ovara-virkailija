'use client';
import { parseAsArrayOf, parseAsString } from 'nuqs';
import {
  useBooleanQueryStateWithOptions,
  useQueryStateWithLocalStorage,
} from './useQueryStateWithLocalStorage';
import { createNullableBooleanOptions } from './paramUtil';

export const useHakijatSearchParams = () => {
  const [selectedPohjakoulutukset, setSelectedPohjakoulutukset] =
    useQueryStateWithLocalStorage('ovara_pohjakoulutukset', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedVastaanottotiedot, setSelectedVastaanottotiedot] =
    useQueryStateWithLocalStorage('ovara_vastaanottotiedot', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedMarkkinointilupa, setSelectedMarkkinointilupa] =
    useQueryStateWithLocalStorage<boolean | null>(
      'ovara_markkinointilupa',
      createNullableBooleanOptions(null),
    );

  const [selectedJulkaisulupa, setSelectedJulkaisulupa] =
    useQueryStateWithLocalStorage<boolean | null>(
      'ovara_julkaisulupa',
      createNullableBooleanOptions(null),
    );

  const [selectedValintatiedot, setSelectedValintatiedot] =
    useQueryStateWithLocalStorage('ovara_valintatiedot', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedKaksoistutkinto, setSelectedKaksoistutkinto] =
    useQueryStateWithLocalStorage<boolean | null>(
      'ovara_kaksoistutkinto',
      createNullableBooleanOptions(null),
    );

  const [selectedUrheilijatutkinto, setSelectedUrheilijatutkinto] =
    useQueryStateWithLocalStorage<boolean | null>(
      'ovara_urheilijatutkinto',
      createNullableBooleanOptions(null),
    );

  const [selectedSoraTerveys, setSelectedSoraTerveys] =
    useQueryStateWithLocalStorage<boolean | null>(
      'ovara_sora_terveys',
      createNullableBooleanOptions(null),
    );

  const [selectedSoraAiempi, setSelectedSoraAiempi] =
    useQueryStateWithLocalStorage<boolean | null>(
      'ovara_sora_aiempi',
      createNullableBooleanOptions(null),
    );

  const [selectedNaytaYoArvosanat, setSelectedNaytaYoArvosanat] =
    useBooleanQueryStateWithOptions('ovara_nayta-yo-arvosanat', false);

  const [selectedNaytaHetu, setSelectedNaytaHetu] =
    useBooleanQueryStateWithOptions('ovara_nayta-hetu', true);

  const [selectedNaytaPostiosoite, setSelectedNaytaPostiosoite] =
    useBooleanQueryStateWithOptions('ovara_nayta-postiosoite', true);

  const emptyAllHakijatParams = () => {
    console.debug('EMPTY ALL HAKIJAT PARAMS');
    const keysToClear = [
      'ovara_pohjakoulutukset',
      'ovara_vastaanottotiedot',
      'ovara_markkinointilupa',
      'ovara_julkaisulupa',
      'ovara_valintatiedot',
      'ovara_kaksoistutkinto',
      'ovara_urheilijatutkinto',
      'ovara_sora-terveys',
      'ovara_sora-aiempi',
      'ovara_nayta-yo-arvosanat',
      'ovara_nayta-hetu',
      'ovara_nayta-postiosoite',
    ];

    keysToClear.forEach((key) => localStorage.removeItem(key));

    setSelectedPohjakoulutukset(null);
    setSelectedVastaanottotiedot(null);
    setSelectedMarkkinointilupa(null);
    setSelectedJulkaisulupa(null);
    setSelectedValintatiedot(null);
    setSelectedKaksoistutkinto(null);
    setSelectedUrheilijatutkinto(null);
    setSelectedSoraTerveys(null);
    setSelectedSoraAiempi(null);
    setSelectedNaytaYoArvosanat(null);
    setSelectedNaytaHetu(null);
    setSelectedNaytaPostiosoite(null);
  };

  return {
    selectedPohjakoulutukset,
    setSelectedPohjakoulutukset,
    selectedVastaanottotiedot,
    setSelectedVastaanottotiedot,
    selectedMarkkinointilupa,
    setSelectedMarkkinointilupa,
    selectedJulkaisulupa,
    setSelectedJulkaisulupa,
    selectedValintatiedot,
    setSelectedValintatiedot,
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
    emptyAllHakijatParams,
  };
};
