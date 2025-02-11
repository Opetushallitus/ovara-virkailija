'use client';
import { parseAsBoolean, useQueryState } from 'nuqs';
import { DEFAULT_NUQS_OPTIONS } from '@/app/lib/constants';

export const useHakeneetSearchParams = () => {
  const [selectedTulostustapa, setSelectedTulostustapa] = useQueryState(
    'tulostustapa',
    DEFAULT_NUQS_OPTIONS,
  );

  const [selectedOpetuskielet, setSelectedOpetuskielet] = useQueryState(
    'opetuskieli',
    DEFAULT_NUQS_OPTIONS,
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
    selectedNaytaHakutoiveet,
    setSelectedNaytaHakutoiveet,
    selectedSukupuoli,
    setSelectedSukupuoli,
  };
};
