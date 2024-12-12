import { describe, expect, test } from 'vitest';
import {
  getSortedKoulutuksenAlkamisKaudet,
  hasOvaraRole,
  hasOvaraToinenAsteRole,
  removeDotsFromTranslations,
} from './utils';

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

describe('hasOvaraRole', () => {
  test('should return true if user has ovara-virkailija role in their authorities', () => {
    const userRoles = [
      'ROLE_APP_OVARA-VIRKAILIJA',
      'ROLE_APP_OVARA-VIRKAILIJA_2ASTE',
      'ROLE_APP_OVARA-VIRKAILIJA_2ASTE_1.2.246.562.10.81934895871',
    ];
    expect(hasOvaraRole(userRoles)).toBeTruthy();
  });

  test("should return false if user doesn't ovara-virkailija role in their authorities", () => {
    const userRoles = [
      'ROLE_APP_RAPORTOINTI',
      'ROLE_APP_RAPORTOINTI_2ASTE',
      'ROLE_APP_RAPORTOINTI_2ASTE_1.2.246.562.10.81934895871',
    ];
    expect(hasOvaraRole(userRoles)).toBeFalsy();
  });

  test('should return false if authorities is empty', () => {
    const userRoles = [];
    expect(hasOvaraRole(userRoles)).toBeFalsy();
  });

  test('should return falsy if authorities is undefined', () => {
    const userRoles = undefined;
    expect(hasOvaraRole(userRoles)).toBeFalsy();
  });
});

describe('hasOvaraToinenAsteRole', () => {
  test('should return true if user has ovara-virkailija_2aste role in their authorities', () => {
    const userRoles = [
      'ROLE_APP_OVARA-VIRKAILIJA',
      'ROLE_APP_OVARA-VIRKAILIJA_2ASTE',
      'ROLE_APP_OVARA-VIRKAILIJA_2ASTE_1.2.246.562.10.81934895871',
    ];
    expect(hasOvaraToinenAsteRole(userRoles)).toBeTruthy();
  });

  test("should return false if user doesn't ovara-virkailija_2aste role in their authorities", () => {
    const userRoles = [
      'ROLE_APP_RAPORTOINTI',
      'ROLE_APP_RAPORTOINTI_2ASTE',
      'ROLE_APP_RAPORTOINTI_2ASTE_1.2.246.562.10.81934895871',
    ];
    expect(hasOvaraToinenAsteRole(userRoles)).toBeFalsy();
  });

  test('should return false if authorities is empty', () => {
    const userRoles = [];
    expect(hasOvaraToinenAsteRole(userRoles)).toBeFalsy();
  });

  test('should return falsy if authorities is undefined', () => {
    const userRoles = undefined;
    expect(hasOvaraToinenAsteRole(userRoles)).toBeFalsy();
  });

  test('should return true when user has OPH_PAAKAYTTAJA user role', () => {
    const userRoles = [
      'ROLE_APP_OVARA-VIRKAILIJA',
      'ROLE_APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA',
      'ROLE_APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA_1.2.246.562.10.00000000001',
    ];
    expect(hasOvaraToinenAsteRole(userRoles)).toBeTruthy();
  });
});
