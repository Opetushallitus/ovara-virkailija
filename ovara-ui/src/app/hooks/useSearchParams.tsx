'use client';
import { parseAsArrayOf, parseAsString, useQueryState } from 'nuqs';

export const DEFAULT_NUQS_OPTIONS = {
  history: 'push',
  clearOnDefault: true,
} as const;

export const useSearchParams = () => {
  const [selectedAlkamiskaudet, setSelectedAlkamiskaudet] = useQueryState(
    'alkamiskausi',
    parseAsArrayOf(parseAsString).withOptions(DEFAULT_NUQS_OPTIONS),
  );

  return {
    selectedAlkamiskaudet,
    setSelectedAlkamiskaudet,
  };
};
