import { describe, expect, test } from 'vitest';
import {
  findOrganisaatiotWithOrganisaatiotyyppi,
  getKoulutustoimijatToShow,
  getSortedKoulutuksenAlkamisKaudet,
  hasOvaraRole,
  hasOvaraToinenAsteRole,
  getOppilaitoksetToShow,
  getToimipisteetToShow,
  getHarkinnanvaraisuusTranslationKey,
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
        alkamiskausinimi: 'yleinen.ei_alkamiskautta',
        value: 'ei_alkamiskautta',
      },
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
        alkamiskausinimi: 'yleinen.ei_alkamiskautta',
        value: 'ei_alkamiskautta',
      },
      {
        alkamiskausinimi: 'yleinen.henkilokohtainen-suunnitelma',
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
  test('should return koulutustoimija from koulutustoimija1 ', () => {
    const koulutustoimija1 = {
      organisaatio_oid: '1.2.246.562.10.10063814452',
      organisaatio_nimi: {
        en: 'Iin kunta',
        fi: 'Iin kunta',
        sv: 'Iin kunta',
      },
      organisaatiotyypit: ['01', '07', '09'],
      oppilaitostyyppi: null,
      tila: 'AKTIIVINEN',
      parent_oids: ['1.2.246.562.10.00000000001', '1.2.246.562.10.10063814452'],
      children: [],
    };

    expect(
      findOrganisaatiotWithOrganisaatiotyyppi(
        koulutustoimija1,
        KOULUTUSTOIMIJAORGANISAATIOTYYPPI,
      ),
    ).toEqual([koulutustoimija1]);
  });

  test('should return empty array as there are no toimipiste in koulutustoimija1 ', () => {
    const koulutustoimija1 = {
      organisaatio_oid: '1.2.246.562.10.10063814452',
      organisaatio_nimi: {
        en: 'Iin kunta',
        fi: 'Iin kunta',
        sv: 'Iin kunta',
      },
      organisaatiotyypit: ['01', '07', '09'],
      oppilaitostyyppi: null,
      tila: 'AKTIIVINEN',
      parent_oids: ['1.2.246.562.10.00000000001', '1.2.246.562.10.10063814452'],
      children: [],
    };

    expect(
      findOrganisaatiotWithOrganisaatiotyyppi(
        koulutustoimija1,
        TOIMIPISTEORGANISAATIOTYYPPI,
      ),
    ).toEqual([]);
  });

  test('should return all toimipisteet from koulutustoimija1 ', () => {
    const toimipiste1_1 = {
      organisaatio_oid: '1.2.246.562.10.19461923609',
      organisaatio_nimi: {
        en: 'Pohjois-Iin koulu',
        fi: 'Pohjois-Iin koulu',
        sv: 'Pohjois-Iin koulu',
      },
      organisaatiotyypit: ['03'],
      oppilaitostyyppi: null,
      tila: 'AKTIIVINEN',
      parent_oids: [
        '1.2.246.562.10.10063814452',
        '1.2.246.562.10.27440356239',
        '1.2.246.562.10.00000000001',
        '1.2.246.562.10.19461923609',
      ],
      children: [],
    };

    const oppilaitos1_1 = {
      organisaatio_oid: '1.2.246.562.10.27440356239',
      organisaatio_nimi: {
        en: 'Pohjois-Iin koulu',
        fi: 'Pohjois-Iin koulu',
        sv: 'Pohjois-Iin koulu',
      },
      organisaatiotyypit: ['02'],
      oppilaitostyyppi: 'oppilaitostyyppi_11#1',
      tila: 'AKTIIVINEN',
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
      oppilaitostyyppi: null,
      tila: 'AKTIIVINEN',
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
      oppilaitostyyppi: null,
      tila: 'AKTIIVINEN',
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
      oppilaitostyyppi: null,
      tila: 'AKTIIVINEN',
      parent_oids: [
        '1.2.246.562.10.44529610774',
        '1.2.246.562.10.41383012972',
        '1.2.246.562.10.10063814452',
        '1.2.246.562.10.00000000001',
      ],
      children: [toimipiste2_1_1, toimipiste2_1_2],
    };
    const oppilaitos1_2 = {
      organisaatio_oid: '1.2.246.562.10.44529610774',
      organisaatio_nimi: {
        en: 'Iin lukio',
        fi: 'Iin lukio',
        sv: 'Iin lukio',
      },
      organisaatiotyypit: ['02'],
      oppilaitostyyppi: 'oppilaitostyyppi_15#1',
      tila: 'AKTIIVINEN',
      parent_oids: [
        '1.2.246.562.10.10063814452',
        '1.2.246.562.10.44529610774',
        '1.2.246.562.10.00000000001',
      ],
      children: [toimipiste2_1],
    };

    const koulutustoimija1 = {
      organisaatio_oid: '1.2.246.562.10.10063814452',
      organisaatio_nimi: {
        en: 'Iin kunta',
        fi: 'Iin kunta',
        sv: 'Iin kunta',
      },
      organisaatiotyypit: ['01', '07', '09'],
      oppilaitostyyppi: null,
      tila: 'AKTIIVINEN',
      parent_oids: ['1.2.246.562.10.00000000001', '1.2.246.562.10.10063814452'],
      children: [oppilaitos1_1, oppilaitos1_2],
    };

    expect(
      findOrganisaatiotWithOrganisaatiotyyppi(
        koulutustoimija1,
        TOIMIPISTEORGANISAATIOTYYPPI,
      ),
    ).toEqual([toimipiste1_1, toimipiste2_1, toimipiste2_1_1, toimipiste2_1_2]);
  });
});

const toimipiste1_1 = {
  organisaatio_oid: '1.2.246.562.10.19461923609',
  organisaatio_nimi: {
    en: 'Pohjois-Iin koulu',
    fi: 'Pohjois-Iin koulu',
    sv: 'Pohjois-Iin koulu',
  },
  organisaatiotyypit: ['03'],
  oppilaitostyyppi: null,
  tila: 'AKTIIVINEN',
  parent_oids: [
    '1.2.246.562.10.10063814452',
    '1.2.246.562.10.27440356239',
    '1.2.246.562.10.00000000001',
    '1.2.246.562.10.19461923609',
  ],
  children: [],
};

const oppilaitos1_1 = {
  organisaatio_oid: '1.2.246.562.10.27440356239',
  organisaatio_nimi: {
    en: 'Pohjois-Iin koulu',
    fi: 'Pohjois-Iin koulu',
    sv: 'Pohjois-Iin koulu',
  },
  organisaatiotyypit: ['02'],
  oppilaitostyyppi: 'oppilaitostyyppi_11#1',
  tila: 'AKTIIVINEN',
  parent_oids: [
    '1.2.246.562.10.27440356239',
    '1.2.246.562.10.10063814452',
    '1.2.246.562.10.00000000001',
  ],
  children: [toimipiste1_1],
};

const oppilaitos1_2 = {
  organisaatio_oid: '1.2.246.562.10.44529610774',
  organisaatio_nimi: {
    en: 'Iin lukio',
    fi: 'Iin lukio',
    sv: 'Iin lukio',
  },
  organisaatiotyypit: ['02'],
  oppilaitostyyppi: 'oppilaitostyyppi_15#1',
  tila: 'AKTIIVINEN',
  parent_oids: [
    '1.2.246.562.10.10063814452',
    '1.2.246.562.10.44529610774',
    '1.2.246.562.10.00000000001',
  ],
  children: [],
};

const koulutustoimija1 = {
  organisaatio_oid: '1.2.246.562.10.10063814452',
  organisaatio_nimi: {
    en: 'Iin kunta',
    fi: 'Iin kunta',
    sv: 'Iin kunta',
  },
  organisaatiotyypit: ['01', '07', '09'],
  oppilaitostyyppi: null,
  tila: 'AKTIIVINEN',
  parent_oids: ['1.2.246.562.10.00000000001', '1.2.246.562.10.10063814452'],
  children: [oppilaitos1_1, oppilaitos1_2],
};

const toimipiste2_1_1 = {
  organisaatio_oid: '1.2.246.562.10.15270964875',
  organisaatio_nimi: {
    en: 'Nylands hotell- och restaurangskola',
    fi: 'Nylands hotell- och restaurangskola',
    sv: 'Nylands hotell- och restaurangskola',
  },
  organisaatiotyypit: ['03'],
  oppilaitostyyppi: null,
  tila: 'AKTIIVINEN',
  parent_oids: [
    '1.2.246.562.10.10281960954',
    '1.2.246.562.10.00000000001',
    '1.2.246.562.10.221157551210',
    '1.2.246.562.10.15270964875',
  ],
  children: [],
};

const oppilaitos2_1 = {
  organisaatio_oid: '1.2.246.562.10.10281960954',
  organisaatio_nimi: {
    en: 'Nylands hotell- och restaurangskola',
    fi: 'Nylands hotell- och restaurangskola',
    sv: 'Nylands hotell- och restaurangskola',
  },
  organisaatiotyypit: ['02'],
  oppilaitostyyppi: 'oppilaitostyyppi_11#1',
  tila: 'AKTIIVINEN',
  parent_oids: [
    '1.2.246.562.10.10281960954',
    '1.2.246.562.10.00000000001',
    '1.2.246.562.10.221157551210',
  ],
  children: [toimipiste2_1_1],
};

const toimipiste2_2_1 = {
  organisaatio_oid: '1.2.246.562.10.61864390655',
  organisaatio_nimi: {
    en: 'Överby trädgårds- och lantbruksskolor',
    fi: 'Överby trädgårds- och lantbruksskolor',
    sv: 'Överby trädgårds- och lantbruksskolor',
  },
  organisaatiotyypit: ['03'],
  oppilaitostyyppi: null,
  tila: 'AKTIIVINEN',
  parent_oids: [
    '1.2.246.562.10.208433283510',
    '1.2.246.562.10.221157551210',
    '1.2.246.562.10.00000000001',
    '1.2.246.562.10.61864390655',
  ],
  children: [],
};

const toimipiste2_2_2 = {
  organisaatio_oid: '1.2.246.562.10.61864390666',
  organisaatio_nimi: {
    en: 'Överby trädgårds- och lantbruksskolor 2',
    fi: 'Överby trädgårds- och lantbruksskolor 2',
    sv: 'Överby trädgårds- och lantbruksskolor 2',
  },
  organisaatiotyypit: ['03'],
  oppilaitostyyppi: null,
  tila: 'AKTIIVINEN',
  parent_oids: [
    '1.2.246.562.10.208433283510',
    '1.2.246.562.10.221157551210',
    '1.2.246.562.10.00000000001',
    '1.2.246.562.10.61864390666',
  ],
  children: [],
};

const toimipiste2_2_3 = {
  organisaatio_oid: '1.2.246.562.10.618643906665',
  organisaatio_nimi: {
    en: 'Överby trädgårds- och lantbruksskolor 3',
    fi: 'Överby trädgårds- och lantbruksskolor 3',
    sv: 'Överby trädgårds- och lantbruksskolor 3',
  },
  organisaatiotyypit: ['03'],
  oppilaitostyyppi: null,
  tila: 'AKTIIVINEN',
  parent_oids: [
    '1.2.246.562.10.208433283510',
    '1.2.246.562.10.221157551210',
    '1.2.246.562.10.00000000001',
    '1.2.246.562.10.618643906665',
  ],
  children: [],
};

const oppilaitos2_2 = {
  organisaatio_oid: '1.2.246.562.10.208433283510',
  organisaatio_nimi: {
    en: 'Överby trädgårds- och lantbruksskolor',
    fi: 'Överby trädgårds- och lantbruksskolor',
    sv: 'Överby trädgårds- och lantbruksskolor',
  },
  organisaatiotyypit: ['02'],
  oppilaitostyyppi: 'oppilaitostyyppi_11#1',
  tila: 'AKTIIVINEN',
  parent_oids: [
    '1.2.246.562.10.208433283510',
    '1.2.246.562.10.221157551210',
    '1.2.246.562.10.00000000001',
  ],
  children: [toimipiste2_2_1, toimipiste2_2_2, toimipiste2_2_3],
};

const toimipiste2_3_1 = {
  organisaatio_oid: '1.2.246.562.10.61864390667',
  organisaatio_nimi: {
    en: 'Överby trädgårds- och lantbruksskolor 3',
    fi: 'Överby trädgårds- och lantbruksskolor 3',
    sv: 'Överby trädgårds- och lantbruksskolor 3',
  },
  organisaatiotyypit: ['03'],
  oppilaitostyyppi: null,
  tila: 'AKTIIVINEN',
  parent_oids: [
    '1.2.246.562.10.2084332835113',
    '1.2.246.562.10.221157551210',
    '1.2.246.562.10.00000000001',
    '1.2.246.562.10.61864390667',
  ],
  children: [],
};

const oppilaitos2_3 = {
  organisaatio_oid: '1.2.246.562.10.2084332835113',
  organisaatio_nimi: {
    en: 'Överby trädgårds- och lantbruksskolor oppilaitos 3',
    fi: 'Överby trädgårds- och lantbruksskolor oppilaitos 3',
    sv: 'Överby trädgårds- och lantbruksskolor oppilaitos 3',
  },
  organisaatiotyypit: ['02'],
  oppilaitostyyppi: 'oppilaitostyyppi_11#1',
  tila: 'AKTIIVINEN',
  parent_oids: [
    '1.2.246.562.10.2084332835113',
    '1.2.246.562.10.221157551210',
    '1.2.246.562.10.00000000001',
  ],
  children: [toimipiste2_3_1],
};

const koulutustoimija2 = {
  organisaatio_oid: '1.2.246.562.10.221157551210',
  organisaatio_nimi: {
    en: 'Samkommunen för huvudstadsregionens svenskspråkiga yrkesskolor',
    fi: 'Samkommunen för huvudstadsregionens svenskspråkiga yrkesskolor',
    sv: 'Samkommunen för huvudstadsregionens svenskspråkiga yrkesskolor',
  },
  organisaatiotyypit: ['01'],
  oppilaitostyyppi: null,
  tila: 'AKTIIVINEN',
  parent_oids: ['1.2.246.562.10.00000000001', '1.2.246.562.10.221157551210'],
  children: [oppilaitos2_1, oppilaitos2_2, oppilaitos2_3],
};

const hierarkiat = [koulutustoimija1, koulutustoimija2];

describe('getKoulutustoimijatToShow', () => {
  test('should return all koulutustoimijat', () => {
    expect(getKoulutustoimijatToShow(hierarkiat)).toEqual([
      koulutustoimija1,
      koulutustoimija2,
    ]);
  });
});

describe('getOppilaitoksetToShow', () => {
  test('should return all oppilaitokset when koulutustoimija is not selected', () => {
    expect(getOppilaitoksetToShow(hierarkiat, null)).toEqual([
      oppilaitos1_1,
      oppilaitos1_2,
      oppilaitos2_1,
      oppilaitos2_2,
      oppilaitos2_3,
    ]);
  });

  test('should return selected oppilaitos', () => {
    expect(
      getOppilaitoksetToShow(hierarkiat, '1.2.246.562.10.221157551210'),
    ).toEqual([oppilaitos2_1, oppilaitos2_2, oppilaitos2_3]);
  });
});

describe('getToimipisteetToShow', () => {
  test('should return all toimipisteet in hierarkia as no toimipiste has been selected', () => {
    expect(getToimipisteetToShow(hierarkiat, null)).toEqual([
      toimipiste1_1,
      toimipiste2_1_1,
      toimipiste2_2_1,
      toimipiste2_2_2,
      toimipiste2_2_3,
      toimipiste2_3_1,
    ]);
  });

  test('should return only selected toimipiste', () => {
    expect(
      getToimipisteetToShow(hierarkiat, ['1.2.246.562.10.208433283510']),
    ).toEqual([toimipiste2_2_1, toimipiste2_2_2, toimipiste2_2_3]);
  });

  test('should return toimipisteet under oppilaitos2_1 and oppilaitos2_3 when selected', () => {
    expect(
      getToimipisteetToShow(hierarkiat, [
        '1.2.246.562.10.10281960954',
        '1.2.246.562.10.2084332835113',
      ]),
    ).toEqual([toimipiste2_1_1, toimipiste2_3_1]);
  });

  test("should return toimipisteet under selected koulutustoimija1 when oppilaitos hasn't been selected", () => {
    expect(
      getToimipisteetToShow(hierarkiat, [], '1.2.246.562.10.10063814452'),
    ).toEqual([toimipiste1_1]);
  });
});

describe('getHarkinnanvaraisuusTranslationKey', () => {
  test('should remove ATARU_ prefix from key and make it lower case', () => {
    expect(
      getHarkinnanvaraisuusTranslationKey(
        'ATARU_KOULUTODISTUSTEN_VERTAILUVAIKEUDET',
      ),
    ).toEqual('koulutodistusten_vertailuvaikeudet');
  });

  test('should return empty string if no match', () => {
    expect(
      getHarkinnanvaraisuusTranslationKey(
        'SURE_KOULUTODISTUSTEN_VERTAILUVAIKEUDET',
      ),
    ).toEqual('');
  });
});
