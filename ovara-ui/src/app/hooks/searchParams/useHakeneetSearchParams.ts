'use client';
import { parseAsArrayOf, parseAsString } from 'nuqs';
import {
  useBooleanQueryStateWithOptions,
  useQueryStateWithLocalStorage,
} from '@/app/hooks/searchParams/useQueryStateWithLocalStorage';
import { createNullableBooleanOptions } from './paramUtil';

export const useHakeneetSearchParams = () => {
  const [selectedTulostustapa, setSelectedTulostustapa] =
    useQueryStateWithLocalStorage<string | null>('ovara_tulostustapa', {
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: null,
    });

  const [selectedOpetuskielet, setSelectedOpetuskielet] =
    useQueryStateWithLocalStorage('ovara_opetuskielet', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedMaakunnat, setSelectedMaakunnat] =
    useQueryStateWithLocalStorage('ovara_maakunnat', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedKunnat, setSelectedKunnat] = useQueryStateWithLocalStorage(
    'ovara_kunnat',
    {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    },
  );

  const [selectedKoulutusalat1, setSelectedKoulutusalat1] =
    useQueryStateWithLocalStorage('ovara_koulutusalat1', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedKoulutusalat2, setSelectedKoulutusalat2] =
    useQueryStateWithLocalStorage('ovara_koulutusalat2', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedKoulutusalat3, setSelectedKoulutusalat3] =
    useQueryStateWithLocalStorage('ovara_koulutusalat3', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedNaytaHakutoiveet, setSelectedNaytaHakutoiveet] =
    useBooleanQueryStateWithOptions('ovara_nayta-hakutoiveet', true);

  const [selectedSukupuoli, setSelectedSukupuoli] =
    useQueryStateWithLocalStorage<string | null>('ovara_sukupuoli', {
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: 'neutral',
    });

  const [selectedTutkinnonTasot, setSelectedTutkinnonTasot] =
    useQueryStateWithLocalStorage('ovara_tutkinnon-tasot', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedAidinkielet, setSelectedAidinkielet] =
    useQueryStateWithLocalStorage('ovara_aidinkielet', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const [selectedEnsikertalainen, setSelectedEnsikertalainen] =
    useQueryStateWithLocalStorage<boolean | null>(
      'ovara_ensikertalainen',
      createNullableBooleanOptions(null),
    );

  const [selectedOkmOhjauksenAlat, setSelectedOkmOhjauksenAlat] =
    useQueryStateWithLocalStorage('ovara_okm-ohjauksen-alat', {
      ...parseAsArrayOf(parseAsString),
      defaultValue: [],
    });

  const emptyAllHakeneetParams = () => {
    console.debug('EMPTY ALL HAKENEET-HYVÄKSYTYT-VASTAANOTTANEET PARAMS');
    const keysToClear = [
      'ovara_tulostustapa',
      'ovara_opetuskielet',
      'ovara_maakunnat',
      'ovara_kunnat',
      'ovara_koulutusalat1',
      'ovara_koulutusalat2',
      'ovara_koulutusalat3',
      'ovara_nayta-hakutoiveet',
      'ovara_sukupuoli',
      'ovara_tutkinnon-tasot',
      'ovara_aidinkielet',
      'ovara_ensikertalainen',
      'ovara_okm-ohjauksen-alat',
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
