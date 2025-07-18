import { sort, isEmpty, uniqueBy, isNullish } from 'remeda';
import { OrganisaatioHierarkia } from './types/common';
import {
  KOULUTUSTOIMIJAORGANISAATIOTYYPPI,
  OPPILAITOSORGANISAATIOTYYPPI,
  TOIMIPISTEORGANISAATIOTYYPPI,
} from './constants';
import { match } from 'ts-pattern';

export type KoulutuksenAlkaminen = {
  alkamiskausinimi: string;
  value: string;
  alkamisvuosi?: number;
};

export const getSortedKoulutuksenAlkamisKaudet = (
  alkamisvuodet: Array<string> | null,
): KoulutuksenAlkaminen[] => {
  const alkamisvuodetInts = alkamisvuodet
    ? alkamisvuodet.map((vuosi) => parseInt(vuosi))
    : [];

  if (alkamisvuodetInts) {
    const sortedAlkamiskaudet: KoulutuksenAlkaminen[] = sort(
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
    }) as KoulutuksenAlkaminen[];

    sortedAlkamiskaudet.unshift({
      value: 'henkilokohtainen_suunnitelma',
      alkamiskausinimi: 'yleinen.henkilokohtainen-suunnitelma',
    });

    sortedAlkamiskaudet.unshift({
      value: 'ei_alkamiskautta',
      alkamiskausinimi: 'yleinen.ei_alkamiskautta',
    });

    return sortedAlkamiskaudet;
  }

  return [];
};

export const hasOphPaaKayttajaRole = (userRoles?: Array<string>) => {
  return userRoles?.includes('ROLE_APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA');
};

export const hasOvaraRole = (userRoles?: Array<string>) => {
  return userRoles?.includes('ROLE_APP_OVARA-VIRKAILIJA');
};

export const hasOvaraToinenAsteRole = (userRoles?: Array<string>) => {
  return (
    userRoles?.includes('ROLE_APP_OVARA-VIRKAILIJA_2ASTE') ||
    userRoles?.includes('ROLE_APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA')
  );
};

export const hasOvaraKkRole = (userRoles?: Array<string>) => {
  return (
    userRoles?.includes('ROLE_APP_OVARA-VIRKAILIJA_KK') ||
    userRoles?.includes('ROLE_APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA')
  );
};

export const findOrganisaatiotWithOrganisaatiotyyppi = (
  hierarkia: OrganisaatioHierarkia,
  organisaatiotyyppi: string,
): Array<OrganisaatioHierarkia> => {
  if (isEmpty(hierarkia.children)) {
    if (hierarkia.organisaatiotyypit.includes(organisaatiotyyppi)) {
      return [hierarkia];
    } else {
      return [];
    }
  }

  if (hierarkia.organisaatiotyypit.includes(organisaatiotyyppi)) {
    return [
      hierarkia,
      ...hierarkia.children.flatMap((child) =>
        findOrganisaatiotWithOrganisaatiotyyppi(child, organisaatiotyyppi),
      ),
    ];
  }

  return hierarkia.children.flatMap((child) =>
    findOrganisaatiotWithOrganisaatiotyyppi(child, organisaatiotyyppi),
  );
};

const getUniqueOrganisaatiotByOrganisaatiotyyppi = (
  organisaatiot: Array<OrganisaatioHierarkia> | null,
  organisaatiotyyppi: string,
) => {
  return uniqueBy(
    organisaatiot?.flatMap((o) =>
      findOrganisaatiotWithOrganisaatiotyyppi(o, organisaatiotyyppi),
    ) || [],
    (o) => o.organisaatio_oid,
  );
};

export const isNullishOrEmpty = <T>(
  list: Array<T> | null | undefined,
): boolean => {
  return isNullish(list) || isEmpty(list);
};

export const getKoulutustoimijatToShow = (
  organisaatiot: Array<OrganisaatioHierarkia> | null,
) => {
  return getUniqueOrganisaatiotByOrganisaatiotyyppi(
    organisaatiot,
    KOULUTUSTOIMIJAORGANISAATIOTYYPPI,
  );
};

export const getOppilaitoksetToShow = (
  hierarkiat: Array<OrganisaatioHierarkia> | null,
  selectedKoulutustoimija: string | null,
) => {
  const oppilaitokset = getUniqueOrganisaatiotByOrganisaatiotyyppi(
    hierarkiat,
    OPPILAITOSORGANISAATIOTYYPPI,
  );

  if (isNullish(selectedKoulutustoimija)) {
    return oppilaitokset;
  } else {
    return oppilaitokset.filter((o) => {
      return o.parent_oids.includes(selectedKoulutustoimija);
    });
  }
};

export const getToimipisteetToShow = (
  hierarkiat: Array<OrganisaatioHierarkia> | null,
  selectedToimipisteOids: Array<string> | null,
  selectedOppilaitosOids: Array<string> | null,
  selectedKoulutustoimija: string | null,
) => {
  const toimipisteet = getUniqueOrganisaatiotByOrganisaatiotyyppi(
    hierarkiat,
    TOIMIPISTEORGANISAATIOTYYPPI,
  );

  if (isNullishOrEmpty(selectedOppilaitosOids)) {
    if (isNullish(selectedKoulutustoimija)) {
      return toimipisteet;
    }

    return toimipisteet.filter((o) => {
      return o.parent_oids.includes(selectedKoulutustoimija);
    });
  } else {
    return toimipisteet.filter((o) => {
      return (
        o.parent_oids.some((oid) => selectedOppilaitosOids?.includes(oid)) ||
        (!isNullishOrEmpty(selectedToimipisteOids) &&
          selectedToimipisteOids?.includes(o.organisaatio_oid))
      );
    });
  }
};

export const getHarkinnanvaraisuusTranslation = (
  harkinnanvaraisuuden_syy: string,
  t: (s: string) => string,
) => {
  const match = harkinnanvaraisuuden_syy.match(/(ATARU)_(\w*)/);
  const lowerCaseMatch = match?.[2].toLowerCase();
  return lowerCaseMatch ? t(`raportti.${lowerCaseMatch}`) : t('');
};

export const getKansalaisuusTranslation = (
  kansalaisuus: string,
  t: (s: string) => string,
) => {
  return match(kansalaisuus)
    .with('1', () => t('raportti.kansalaisuus.suomi'))
    .with('2', () => t('raportti.kansalaisuus.eu-eta'))
    .otherwise(() => t('raportti.kansalaisuus.muu'));
};
