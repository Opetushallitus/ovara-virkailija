import { sort } from 'remeda';

export type KoulutuksenAlkaminen = {
  alkamisvuosi: number;
  alkamiskausikoodiuri: string;
  alkamiskausinimi: string;
  value: string;
};

export const getSortedKoulutuksenAlkamisKaudet = (
  alkamisvuodet: Array<string> | null,
): KoulutuksenAlkaminen[] => {
  const alkamisvuodetInts = alkamisvuodet?.map((vuosi) => parseInt(vuosi));

  if (alkamisvuodetInts) {
    return sort(alkamisvuodetInts, (a, b) => b - a).flatMap((alkamisvuosi) => {
      return [
        {
          alkamisvuosi: alkamisvuosi,
          alkamiskausikoodiuri: 'kausi_k',
          alkamiskausinimi: 'yleinen.kevat',
          value: `${alkamisvuosi}_kevat`,
        },
        {
          alkamisvuosi: alkamisvuosi,
          alkamiskausikoodiuri: 'kausi_s',
          alkamiskausinimi: 'yleinen.syksy',
          value: `${alkamisvuosi}_syksy`,
        },
      ];
    });
  }

  return [];
};
