import { describe, expect, test } from 'vitest';
import { getSortedKoulutuksenAlkamisKaudet } from './utils';

describe('getSortedKoulutuksenAlkamiskaudet', () => {
  test('should return array with henkilokohtainen-suunnitelma if alkamisvuodet is undefined', () => {
    const alkamisvuodet = null;
    expect(getSortedKoulutuksenAlkamisKaudet(alkamisvuodet)).toEqual([
      {
        value: 'henkilokohtainen_suunnitelma',
        alkamiskausinimi: 'yleinen.henkilokohtainen-suunnitelma',
      },
    ]);
  });

  test('should return alkamiskausi objects sorted in descending order by year', () => {
    const alkamisvuodet = ['2024', '2021', '2022'];
    const result = [
      {
        value: 'henkilokohtainen_suunnitelma',
        alkamiskausinimi: 'yleinen.henkilokohtainen-suunnitelma',
      },
      {
        alkamisvuosi: 2024,
        alkamiskausinimi: 'yleinen.kevat',
        value: `2024_kevat`,
      },
      {
        alkamisvuosi: 2024,
        alkamiskausinimi: 'yleinen.syksy',
        value: `2024_syksy`,
      },
      {
        alkamisvuosi: 2022,
        alkamiskausinimi: 'yleinen.kevat',
        value: `2022_kevat`,
      },
      {
        alkamisvuosi: 2022,
        alkamiskausinimi: 'yleinen.syksy',
        value: `2022_syksy`,
      },
      {
        alkamisvuosi: 2021,
        alkamiskausinimi: 'yleinen.kevat',
        value: `2021_kevat`,
      },
      {
        alkamisvuosi: 2021,
        alkamiskausinimi: 'yleinen.syksy',
        value: `2021_syksy`,
      },
    ];

    expect(getSortedKoulutuksenAlkamisKaudet(alkamisvuodet)).toEqual(result);
  });
});
