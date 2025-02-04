'use client';
import { useQueryState } from 'nuqs';
import { DEFAULT_NUQS_OPTIONS } from '@/app/lib/constants';

export const useHakeneetSearchParams = () => {
  const [selectedTulostustapa, setSelectedTulostustapa] = useQueryState(
    'tulostustapa',
    DEFAULT_NUQS_OPTIONS,
  );

  const [selectedOpetuskieli, setSelectedOpetuskieli] = useQueryState(
    'opetuskieli',
    DEFAULT_NUQS_OPTIONS,
  );

  return {
    selectedTulostustapa,
    setSelectedTulostustapa,
    selectedOpetuskieli,
    setSelectedOpetuskieli,
  };
};
