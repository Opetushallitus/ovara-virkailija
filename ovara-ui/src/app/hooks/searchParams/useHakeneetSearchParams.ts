'use client';
import {
  parseAsArrayOf,
  parseAsBoolean,
  parseAsString,
  useQueryState,
} from 'nuqs';
import { DEFAULT_NUQS_OPTIONS } from '@/app/lib/constants';
import { useQueryStateWithLocalStorage } from '@/app/hooks/searchParams/useQueryStateWithLocalStorage';

export const useHakeneetSearchParams = () => {
  const [selectedTulostustapa, setSelectedTulostustapa] =
    useQueryStateWithLocalStorage<string | null>('tulostustapa', {
      ...DEFAULT_NUQS_OPTIONS,
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: null,
    });

  const [selectedOpetuskielet, setSelectedOpetuskielet] =
    useQueryStateWithLocalStorage('opetuskielet', {
      ...parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
      defaultValue: [],
    });

  const [selectedMaakunnat, setSelectedMaakunnat] =
    useQueryStateWithLocalStorage('maakunnat', {
      ...parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
      defaultValue: [],
    });

  const [selectedKunnat, setSelectedKunnat] = useQueryStateWithLocalStorage(
    'kunnat',
    {
      ...parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
      defaultValue: [],
    },
  );

  const [selectedKoulutusalat1, setSelectedKoulutusalat1] =
    useQueryStateWithLocalStorage('koulutusalat1', {
      ...parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
      defaultValue: [],
    });

  const [selectedKoulutusalat2, setSelectedKoulutusalat2] =
    useQueryStateWithLocalStorage('koulutusalat2', {
      ...parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
      defaultValue: [],
    });

  const [selectedKoulutusalat3, setSelectedKoulutusalat3] =
    useQueryStateWithLocalStorage('koulutusalat3', {
      ...parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
      defaultValue: [],
    });

  // TODO fix boolean parsing
  const [selectedNaytaHakutoiveet, setSelectedNaytaHakutoiveet] = useQueryState(
    'nayta-hakutoiveet',
    parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS).withDefault(true),
  );

  const [selectedSukupuoli, setSelectedSukupuoli] =
    useQueryStateWithLocalStorage<string | null>('sukupuoli', {
      ...DEFAULT_NUQS_OPTIONS,
      parse: (value) => (value === null ? null : String(value)),
      defaultValue: 'neutral',
    });

  const [selectedTutkinnonTasot, setSelectedTutkinnonTasot] =
    useQueryStateWithLocalStorage('tutkinnon-tasot', {
      ...parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
      defaultValue: [],
    });

  const [selectedAidinkielet, setSelectedAidinkielet] =
    useQueryStateWithLocalStorage('aidinkielet', {
      ...parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
      defaultValue: [],
    });

  // TODO fix boolean parsing
  const [selectedEnsikertalainen, setSelectedEnsikertalainen] = useQueryState(
    'ensikertalainen',
    parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedOkmOhjauksenAlat, setSelectedOkmOhjauksenAlat] =
    useQueryStateWithLocalStorage('okm-ohjauksen-alat', {
      ...parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
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
      'sukupuoli',
      'tutkinnon-tasot',
      'aidinkielet',
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
