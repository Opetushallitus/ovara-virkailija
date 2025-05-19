'use client';
import { parseAsArrayOf, parseAsString } from 'nuqs';
import { useQueryStateWithLocalStorage } from './useQueryStateWithLocalStorage';
import {
  createBooleanOptions,
  createNullableBooleanOptions,
} from './paramUtil';

export const useHakijatSearchParams = () => {
  const [selectedPohjakoulutukset, setSelectedPohjakoulutukset] =
    useQueryStateWithLocalStorage('pohjakoulutukset', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedVastaanottotiedot, setSelectedVastaanottotiedot] =
    useQueryStateWithLocalStorage('vastaanottotiedot', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedMarkkinointilupa, setSelectedMarkkinointilupa] =
    useQueryStateWithLocalStorage<boolean | null>(
      'markkinointilupa',
      createNullableBooleanOptions(null),
    );

  const [selectedJulkaisulupa, setSelectedJulkaisulupa] =
    useQueryStateWithLocalStorage<boolean | null>(
      'julkaisulupa',
      createNullableBooleanOptions(null),
    );

  const [selectedValintatiedot, setSelectedValintatiedot] =
    useQueryStateWithLocalStorage('valintatiedot', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedKaksoistutkinto, setSelectedKaksoistutkinto] =
    useQueryStateWithLocalStorage<boolean | null>(
      'kaksoistutkinto',
      createNullableBooleanOptions(null),
    );

  const [selectedUrheilijatutkinto, setSelectedUrheilijatutkinto] =
    useQueryStateWithLocalStorage<boolean | null>(
      'urheilijatutkinto',
      createNullableBooleanOptions(null),
    );

  const [selectedSoraTerveys, setSelectedSoraTerveys] =
    useQueryStateWithLocalStorage<boolean | null>(
      'sora_terveys',
      createNullableBooleanOptions(null),
    );

  const [selectedSoraAiempi, setSelectedSoraAiempi] =
    useQueryStateWithLocalStorage<boolean | null>(
      'sora_aiempi',
      createNullableBooleanOptions(null),
    );

  const [selectedNaytaYoArvosanat, setSelectedNaytaYoArvosanat] =
    useQueryStateWithLocalStorage<boolean>(
      'nayta-yo-arvosanat',
      createBooleanOptions(false),
    );

  const [selectedNaytaHetu, setSelectedNaytaHetu] =
    useQueryStateWithLocalStorage<boolean>(
      'nayta-hetu',
      createBooleanOptions(true),
    );

  const [selectedNaytaPostiosoite, setSelectedNaytaPostiosoite] =
    useQueryStateWithLocalStorage<boolean>(
      'nayta-postiosoite',
      createBooleanOptions(true),
    );

  const emptyAllHakijatParams = () => {
    console.debug('EMPTY ALL HAKIJAT PARAMS');
    const keysToClear = [
      'pohjakoulutukset',
      'vastaanottotiedot',
      'markkinointilupa',
      'julkaisulupa',
      'valintatiedot',
      'kaksoistutkinto',
      'urheilijatutkinto',
      'sora-terveys',
      'sora-aiempi',
      'nayta-yo-arvosanat',
      'nayta-hetu',
      'nayta-postiosoite',
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
