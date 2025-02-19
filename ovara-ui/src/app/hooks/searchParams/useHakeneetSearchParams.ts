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
    parseAsBoolean.withOptions(DEFAULT_NUQS_OPTIONS),
  );

  const [selectedSukupuoli, setSelectedSukupuoli] = useQueryState(
    'sukupuoli',
    DEFAULT_NUQS_OPTIONS,
  );

  return {
    selectedTulostustapa,
    setSelectedTulostustapa,
    selectedOpetuskielet,
    setSelectedOpetuskielet,
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
  };
};
