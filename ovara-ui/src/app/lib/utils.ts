import { sort } from 'remeda';
import { set } from 'lodash';

export type KoulutuksenAlkaminen = {
  alkamiskausinimi: string;
  value: string;
  alkamisvuosi?: number | undefined;
};

export const getSortedKoulutuksenAlkamisKaudet = (
  alkamisvuodet: Array<string> | null,
): KoulutuksenAlkaminen[] => {
  const alkamisvuodetInts = alkamisvuodet
    ? alkamisvuodet.map((vuosi) => parseInt(vuosi))
    : [];

  if (alkamisvuodetInts) {
    const sortedAlkamiskaudet = sort(
      alkamisvuodetInts,
      (a, b) => b - a,
    ).flatMap((alkamisvuosi) => {
      return [
        {
          alkamisvuosi: alkamisvuosi,
          alkamiskausinimi: 'yleinen.kevat',
          value: `${alkamisvuosi}_kevat`,
        },
        {
          alkamisvuosi: alkamisvuosi,
          alkamiskausinimi: 'yleinen.syksy',
          value: `${alkamisvuosi}_syksy`,
        },
      ];
    });

    sortedAlkamiskaudet.unshift({
      alkamisvuosi: undefined,
      value: 'henkilokohtainen_suunnitelma',
      alkamiskausinimi: 'yleinen.henkilokohtainen-suunnitelma',
    });

    return sortedAlkamiskaudet;
  }

  return [];
};

// next-intl ei salli pisteitä avaimissa
// ks. https://github.com/amannn/next-intl/discussions/148#discussioncomment-4274218
export function removeDotsFromTranslations(
  translations: { [s: string]: string } | ArrayLike<string>,
) {
  return Object.entries(translations).reduce(
    (acc, [key, value]) => set(acc, key, value),
    {},
  );
}
