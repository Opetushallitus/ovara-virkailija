import { useEffect, useState } from 'react';
import { configuration } from '@/app/lib/configuration';
import { removeDotsFromTranslations } from '@/app/lib/utils';

const REVALIDATE_TIME_SECONDS = 60 * 60 * 2;

type Translation = {
  accesscount: number;
  id: number;
  force: boolean;
  accessed: number;
  category: string;
  created: number;
  createdBy: string;
  modified: number;
  modifiedBy: string;
  key: string;
  locale: string;
  value: string;
};

export function formatTranslations(lokalisoinnit: Array<Translation>) {
  const translations: Record<string, string> = {};
  for (const translation of lokalisoinnit) {
    translations[translation.key] = translation.value;
  }

  return removeDotsFromTranslations(translations);
}

export function useFetchTranslations(lang: string) {
  const [translationsFromLokalisointi, setTranslations] = useState<object>({});
  useEffect(() => {
    async function fetchFromLokalisointi() {
      const url = `${configuration.virkailijaUrl}/lokalisointi/cxf/rest/v1/localisation?category=ovara&locale=`;
      const response = await fetch(`${url}${lang}`, {
        next: { revalidate: REVALIDATE_TIME_SECONDS },
      });
      const translationsFromLokalisointi = await response.json();
      const kaannokset = formatTranslations(translationsFromLokalisointi);
      setTranslations(kaannokset);
    }

    fetchFromLokalisointi();
  }, [lang]);

  return translationsFromLokalisointi;
}
