import { sort } from 'remeda';

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
      alkamiskausinimi: 'yleinen.henkilokohtainen_suunnitelma',
      value: 'henkilokohtainen_suunnitelma',
    });

    return sortedAlkamiskaudet;
  }

  return [];
};
