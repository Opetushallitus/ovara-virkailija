import { describe, expect, test } from 'vitest';
import {
  findOrganisaatiotWithOrganisaatiotyyppi,
  getSortedKoulutuksenAlkamisKaudet,
  hasOvaraRole,
  hasOvaraToinenAsteRole,
  removeDotsFromTranslations,
} from './utils';
import {
  KOULUTUSTOIMIJAORGANISAATIOTYYPPI,
  TOIMIPISTEORGANISAATIOTYYPPI,
} from './constants';

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
    const userRoles = [] as Array<string>;
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
    const userRoles = [] as Array<string>;
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

describe('findOrganisaatioByOrganisaatiotyyppi', () => {
  test('should return koulutustoimija from hierarkia', () => {
    const hierarkia = {
      organisaatio_oid: '1.2.246.562.10.10063814452',
      organisaatio_nimi: {
        en: 'Iin kunta',
        fi: 'Iin kunta',
        sv: 'Iin kunta',
      },
      organisaatiotyypit: ['01', '07', '09'],
      parent_oids: ['1.2.246.562.10.00000000001', '1.2.246.562.10.10063814452'],
      children: [],
    };

    expect(
      findOrganisaatiotWithOrganisaatiotyyppi(
        hierarkia,
        KOULUTUSTOIMIJAORGANISAATIOTYYPPI,
      ),
    ).toEqual([hierarkia]);
  });

  test('should return empty array as there are no toimipiste in hierarkia', () => {
    const hierarkia = {
      organisaatio_oid: '1.2.246.562.10.10063814452',
      organisaatio_nimi: {
        en: 'Iin kunta',
        fi: 'Iin kunta',
        sv: 'Iin kunta',
      },
      organisaatiotyypit: ['01', '07', '09'],
      parent_oids: ['1.2.246.562.10.00000000001', '1.2.246.562.10.10063814452'],
      children: [],
    };

    expect(
      findOrganisaatiotWithOrganisaatiotyyppi(
        hierarkia,
        TOIMIPISTEORGANISAATIOTYYPPI,
      ),
    ).toEqual([]);
  });

  test('should return all toimipisteet from hierarkia', () => {
    const toimipiste1_1 = {
      organisaatio_oid: '1.2.246.562.10.19461923609',
      organisaatio_nimi: {
        en: 'Pohjois-Iin koulu',
        fi: 'Pohjois-Iin koulu',
        sv: 'Pohjois-Iin koulu',
      },
      organisaatiotyypit: ['03'],
      parent_oids: [
        '1.2.246.562.10.10063814452',
        '1.2.246.562.10.27440356239',
        '1.2.246.562.10.00000000001',
        '1.2.246.562.10.19461923609',
      ],
      children: [],
    };

    const oppilaitos1 = {
      organisaatio_oid: '1.2.246.562.10.27440356239',
      organisaatio_nimi: {
        en: 'Pohjois-Iin koulu',
        fi: 'Pohjois-Iin koulu',
        sv: 'Pohjois-Iin koulu',
      },
      organisaatiotyypit: ['02'],
      parent_oids: [
        '1.2.246.562.10.27440356239',
        '1.2.246.562.10.10063814452',
        '1.2.246.562.10.00000000001',
      ],
      children: [toimipiste1_1],
    };

    const toimipiste2_1_1 = {
      organisaatio_oid: '1.2.246.562.10.413830129721',
      organisaatio_nimi: {
        en: 'Iin alalukio',
        fi: 'Iin alalukio',
        sv: 'Iin alalukio',
      },
      organisaatiotyypit: ['03'],
      parent_oids: [
        '1.2.246.562.10.413830129721',
        '1.2.246.562.10.44529610774',
        '1.2.246.562.10.41383012972',
        '1.2.246.562.10.10063814452',
        '1.2.246.562.10.00000000001',
      ],
      children: [],
    };

    const toimipiste2_1_2 = {
      organisaatio_oid: '1.2.246.562.10.413830129722',
      organisaatio_nimi: {
        en: 'Joku toinen alatoimipiste',
        fi: 'Joku toinen alatoimipiste',
        sv: 'Joku toinen alatoimipiste',
      },
      organisaatiotyypit: ['03'],
      parent_oids: [
        '1.2.246.562.10.413830129721',
        '1.2.246.562.10.44529610774',
        '1.2.246.562.10.41383012972',
        '1.2.246.562.10.10063814452',
        '1.2.246.562.10.00000000001',
      ],
      children: [],
    };

    const toimipiste2_1 = {
      organisaatio_oid: '1.2.246.562.10.41383012972',
      organisaatio_nimi: {
        en: 'Iin lukio',
        fi: 'Iin lukio',
        sv: 'Iin lukio',
      },
      organisaatiotyypit: ['03'],
      parent_oids: [
        '1.2.246.562.10.44529610774',
        '1.2.246.562.10.41383012972',
        '1.2.246.562.10.10063814452',
        '1.2.246.562.10.00000000001',
      ],
      children: [toimipiste2_1_1, toimipiste2_1_2],
    };
    const oppilaitos2 = {
      organisaatio_oid: '1.2.246.562.10.44529610774',
      organisaatio_nimi: {
        en: 'Iin lukio',
        fi: 'Iin lukio',
        sv: 'Iin lukio',
      },
      organisaatiotyypit: ['02'],
      parent_oids: [
        '1.2.246.562.10.10063814452',
        '1.2.246.562.10.44529610774',
        '1.2.246.562.10.00000000001',
      ],
      children: [toimipiste2_1],
    };

    const hierarkia = {
      organisaatio_oid: '1.2.246.562.10.10063814452',
      organisaatio_nimi: {
        en: 'Iin kunta',
        fi: 'Iin kunta',
        sv: 'Iin kunta',
      },
      organisaatiotyypit: ['01', '07', '09'],
      parent_oids: ['1.2.246.562.10.00000000001', '1.2.246.562.10.10063814452'],
      children: [oppilaitos1, oppilaitos2],
    };

    expect(
      findOrganisaatiotWithOrganisaatiotyyppi(
        hierarkia,
        TOIMIPISTEORGANISAATIOTYYPPI,
      ),
    ).toEqual([toimipiste1_1, toimipiste2_1, toimipiste2_1_1, toimipiste2_1_2]);
  });
});
