import { sort } from 'remeda';

export type KoulutuksenAlkaminen = {
  alkamisvuosi: number;
  alkamiskausinimi: string;
  value: string;
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
      alkamiskausinimi: 'yleinen.henkilokohtainen_suunnitelma',
      value: 'henkilokohtainen_suunnitelma',
    });

    return sortedAlkamiskaudet;
  }

  return [];
};
