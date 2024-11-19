import { describe, expect, test } from 'vitest';
import {
  getSortedKoulutuksenAlkamisKaudet,
  removeDotsFromTranslations,
} from './utils';

describe('getSortedKoulutuksenAlkamiskaudet', () => {
  test('should return array with henkilokohtainen_suunnitelma if alkamisvuodet is undefined', () => {
    const alkamisvuodet = null;
    expect(getSortedKoulutuksenAlkamisKaudet(alkamisvuodet)).toEqual([
      {
        alkamiskausinimi: 'yleinen.henkilokohtainen_suunnitelma',
        value: 'henkilokohtainen_suunnitelma',
      },
    ]);
  });

  test('should return alkamiskausi objects sorted in descending order by year', () => {
    const alkamisvuodet = ['2024', '2021', '2022'];
    const result = [
      {
        alkamiskausinimi: 'yleinen.henkilokohtainen_suunnitelma',
        value: 'henkilokohtainen_suunnitelma',
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

describe('removeDotsFromTranslations', () => {
  test('it should return lokalisoinnit in a next-intl approved format', () => {
    const translations = {
      'header.home': 'Opiskelijavalinnan raportointi',
      'raporttilista.title': 'Raportti',
      'raportti.radio-group.neutral': 'Ei merkitystä',
      'raportti.radio-group.yes': 'Kyllä',
      'raportti.radio-group.no': 'Ei',
    };

    const result = {
      header: {
        home: 'Opiskelijavalinnan raportointi',
      },
      raporttilista: {
        title: 'Raportti',
      },
      raportti: {
        'radio-group': {
          neutral: 'Ei merkitystä',
          yes: 'Kyllä',
          no: 'Ei',
        },
      },
    };
    expect(removeDotsFromTranslations(translations)).toEqual(result);
  });
});
