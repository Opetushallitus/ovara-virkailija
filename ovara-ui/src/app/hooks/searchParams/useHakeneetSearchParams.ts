'use client';
import { parseAsArrayOf, parseAsString } from 'nuqs';
import { useQueryStateWithLocalStorage } from '@/app/hooks/searchParams/useQueryStateWithLocalStorage';
import {
  createBooleanOptions,
  createNullableBooleanOptions,
} from './paramUtil';

export const useHakeneetSearchParams = () => {
  const [selectedTulostustapa, setSelectedTulostustapa] =
    useQueryStateWithLocalStorage<string | null>('tulostustapa', {
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: null,
    });

  const [selectedOpetuskielet, setSelectedOpetuskielet] =
    useQueryStateWithLocalStorage('opetuskielet', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedMaakunnat, setSelectedMaakunnat] =
    useQueryStateWithLocalStorage('maakunnat', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedKunnat, setSelectedKunnat] = useQueryStateWithLocalStorage(
    'kunnat',
    {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    },
  );

  const [selectedKoulutusalat1, setSelectedKoulutusalat1] =
    useQueryStateWithLocalStorage('koulutusalat1', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedKoulutusalat2, setSelectedKoulutusalat2] =
    useQueryStateWithLocalStorage('koulutusalat2', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedKoulutusalat3, setSelectedKoulutusalat3] =
    useQueryStateWithLocalStorage('koulutusalat3', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedNaytaHakutoiveet, setSelectedNaytaHakutoiveet] =
    useQueryStateWithLocalStorage<boolean>(
      'nayta-hakutoiveet',
      createBooleanOptions(true),
    );

  const [selectedSukupuoli, setSelectedSukupuoli] =
    useQueryStateWithLocalStorage<string | null>('sukupuoli', {
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: 'neutral',
    });

  const [selectedTutkinnonTasot, setSelectedTutkinnonTasot] =
    useQueryStateWithLocalStorage('tutkinnon-tasot', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedAidinkielet, setSelectedAidinkielet] =
    useQueryStateWithLocalStorage('aidinkielet', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedEnsikertalainen, setSelectedEnsikertalainen] =
    useQueryStateWithLocalStorage<boolean | null>(
      'ensikertalainen',
      createNullableBooleanOptions(null),
    );

  const [selectedOkmOhjauksenAlat, setSelectedOkmOhjauksenAlat] =
    useQueryStateWithLocalStorage('okm-ohjauksen-alat', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const emptyAllHakeneetParams = () => {
    console.debug('EMPTY ALL HAKENEET-HYVÄKSYTYT-VASTAANOTTANEET PARAMS');
    const keysToClear = [
      'tulostustapa',
      'opetuskielet',
      'maakunnat',
      'kunnat',
      'koulutusalat1',
      'koulutusalat2',
      'koulutusalat3',
      'nayta-hakutoiveet',
      'sukupuoli',
      'tutkinnon-tasot',
      'aidinkielet',
      'ensikertalainen',
      'okm-ohjauksen-alat',
    ];

    keysToClear.forEach((key) => localStorage.removeItem(key));

    setSelectedTulostustapa(null);
    setSelectedOpetuskielet(null);
    setSelectedMaakunnat(null);
    setSelectedKunnat(null);
    setSelectedKoulutusalat1(null);
    setSelectedKoulutusalat2(null);
    setSelectedKoulutusalat3(null);
    setSelectedNaytaHakutoiveet(null);
    setSelectedSukupuoli(null);
    setSelectedTutkinnonTasot(null);
    setSelectedAidinkielet(null);
    setSelectedEnsikertalainen(null);
    setSelectedOkmOhjauksenAlat(null);
  };

  return {
    selectedTulostustapa,
    setSelectedTulostustapa,
    selectedOpetuskielet,
    setSelectedOpetuskielet,
    selectedMaakunnat,
    setSelectedMaakunnat,
    selectedKunnat,
    setSelectedKunnat,
    selectedKoulutusalat1,
    setSelectedKoulutusalat1,
    selectedKoulutusalat2,
    setSelectedKoulutusalat2,
    selectedKoulutusalat3,
    setSelectedKoulutusalat3,
    selectedNaytaHakutoiveet,
    setSelectedNaytaHakutoiveet,
    selectedSukupuoli,
    setSelectedSukupuoli,
    selectedTutkinnonTasot,
    setSelectedTutkinnonTasot,
    selectedAidinkielet,
    setSelectedAidinkielet,
    selectedEnsikertalainen,
    setSelectedEnsikertalainen,
    selectedOkmOhjauksenAlat,
    setSelectedOkmOhjauksenAlat,
    emptyAllHakeneetParams,
  };
};
