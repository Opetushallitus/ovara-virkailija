import { describe, expect, test } from 'vitest';
import { getSortedKoulutuksenAlkamisKaudet } from './utils';

describe('getSortedKoulutuksenAlkamiskaudet', () => {
  test('should return empty array if alkamisvuodet is undefined', () => {
    const alkamisvuodet = null;
    expect(getSortedKoulutuksenAlkamisKaudet(alkamisvuodet)).toEqual([]);
  });

  test('should return alkamiskausi objects sorted in descending order by year', () => {
    const alkamisvuodet = ['2024', '2021', '2022'];
    const result = [
      {
        alkamisvuosi: 2024,
        alkamiskausikoodiuri: 'kausi_k',
        alkamiskausinimi: 'yleinen.kevat',
        value: `2024_kevat`,
      },
      {
        alkamisvuosi: 2024,
        alkamiskausikoodiuri: 'kausi_s',
        alkamiskausinimi: 'yleinen.syksy',
        value: `2024_syksy`,
      },
      {
        alkamisvuosi: 2022,
        alkamiskausikoodiuri: 'kausi_k',
        alkamiskausinimi: 'yleinen.kevat',
        value: `2022_kevat`,
      },
      {
        alkamisvuosi: 2022,
        alkamiskausikoodiuri: 'kausi_s',
        alkamiskausinimi: 'yleinen.syksy',
        value: `2022_syksy`,
      },
      {
        alkamisvuosi: 2021,
        alkamiskausikoodiuri: 'kausi_k',
        alkamiskausinimi: 'yleinen.kevat',
        value: `2021_kevat`,
      },
      {
        alkamisvuosi: 2021,
        alkamiskausikoodiuri: 'kausi_s',
        alkamiskausinimi: 'yleinen.syksy',
        value: `2021_syksy`,
      },
    ];

    expect(getSortedKoulutuksenAlkamisKaudet(alkamisvuodet)).toEqual(result);
  });
});
