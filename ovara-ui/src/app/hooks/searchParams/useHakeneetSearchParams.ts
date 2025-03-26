'use client';
import {
  parseAsArrayOf,
  parseAsBoolean,
  parseAsString,
  useQueryState,
} from 'nuqs';
import { DEFAULT_NUQS_OPTIONS } from '@/app/lib/constants';

export const useHakeneetSearchParams = () => {
  const [selectedTulostustapa, setSelectedTulostustapa] = useQueryState(
    'tulostustapa',
    DEFAULT_NUQS_OPTIONS,
  );

  const [selectedOpetuskielet, setSelectedOpetuskielet] = useQueryState(
    'opetuskieli',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedMaakunnat, setSelectedMaakunnat] = useQueryState(
    'maakunta',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedKunnat, setSelectedKunnat] = useQueryState(
    'kunta',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedKoulutusalat1, setSelectedKoulutusalat1] = useQueryState(
    'koulutusala1',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedKoulutusalat2, setSelectedKoulutusalat2] = useQueryState(
    'koulutusala2',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedKoulutusalat3, setSelectedKoulutusalat3] = useQueryState(
    'koulutusala3',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedNaytaHakutoiveet, setSelectedNaytaHakutoiveet] = useQueryState(
    'nayta-hakutoiveet',
    parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS).withDefault(true),
  );

  const [selectedSukupuoli, setSelectedSukupuoli] = useQueryState(
    'sukupuoli',
    parseAsString.withOptions(DEFAULT_NUQS_OPTIONS).withDefault('neutral'),
  );

  const [selectedTutkinnonTaso, setSelectedTutkinnonTaso] = useQueryState(
    'tutkinnon-taso',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedAidinkieli, setSelectedAidinkieli] = useQueryState(
    'aidinkieli',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedEnsikertalainen, setSelectedEnsikertalainen] = useQueryState(
    'ensikertalainen',
    parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedOkmOhjauksenAlat, setSelectedOkmOhjauksenAlat] = useQueryState(
    'okm-ohjauksen-ala',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const emptyAllHakeneetParams = () => {
    console.debug('EMPTY ALL HAKENEET-HYVÄKSYTYT-VASTAANOTTANEET PARAMS');
    setSelectedTulostustapa(null);
    setSelectedOpetuskielet(null);
    setSelectedMaakunnat(null);
    setSelectedKunnat(null);
    setSelectedKoulutusalat1(null);
    setSelectedKoulutusalat2(null);
    setSelectedKoulutusalat3(null);
    setSelectedNaytaHakutoiveet(null);
    setSelectedSukupuoli(null);
    setSelectedTutkinnonTaso(null);
    setSelectedAidinkieli(null);
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
    selectedTutkinnonTaso,
    setSelectedTutkinnonTaso,
    selectedAidinkieli,
    setSelectedAidinkieli,
    selectedEnsikertalainen,
    setSelectedEnsikertalainen,
    selectedOkmOhjauksenAlat,
    setSelectedOkmOhjauksenAlat,
    emptyAllHakeneetParams,
  };
};
