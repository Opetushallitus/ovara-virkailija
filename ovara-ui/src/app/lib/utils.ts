import { sort, isEmpty, uniqueBy, isNullish } from 'remeda';
import { set } from 'lodash';
import { OrganisaatioHierarkia } from './types/common';
import {
  KOULUTUSTOIMIJAORGANISAATIOTYYPPI,
  OPPILAITOSORGANISAATIOTYYPPI,
  TOIMIPISTEORGANISAATIOTYYPPI,
} from './constants';

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
    }) as KoulutuksenAlkaminen[];

    sortedAlkamiskaudet.unshift({
      value: 'henkilokohtainen_suunnitelma',
      alkamiskausinimi: 'yleinen.henkilokohtainen-suunnitelma',
    });

    return sortedAlkamiskaudet;
  }

  return [];
};

// next-intl ei salli pisteitä avaimissa
// ks. https://github.com/amannn/next-intl/discussions/148#discussioncomment-4274218
export function removeDotsFromTranslations(
  translations: { [s: string]: string } | ArrayLike<string>,
) {
  return Object.entries(translations).reduce(
    (acc, [key, value]) => set(acc, key, value),
    {},
  );
}

export const hasOvaraRole = (userRoles?: Array<string>) => {
  return userRoles?.includes('ROLE_APP_OVARA-VIRKAILIJA');
};

export const hasOvaraToinenAsteRole = (userRoles?: Array<string>) => {
  return (
    userRoles?.includes('ROLE_APP_OVARA-VIRKAILIJA_2ASTE') ||
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
  organisaatiot: Array<OrganisaatioHierarkia>,
  organisaatiotyyppi: string,
) => {
  return uniqueBy(
    organisaatiot?.flatMap((o) =>
      findOrganisaatiotWithOrganisaatiotyyppi(o, organisaatiotyyppi),
    ) || [],
    (o) => o.organisaatio_oid,
  );
};

export const getOrganisaatiotToShow = (
  organisaatiot: Array<OrganisaatioHierarkia>,
  organisaatiotyyppi: string,
  oids?: Array<string>,
) => {
  const orgsByOrganisaatiotyyppi = uniqueBy(
    organisaatiot?.flatMap((o) =>
      findOrganisaatiotWithOrganisaatiotyyppi(o, organisaatiotyyppi),
    ) || [],
    (o) => o.organisaatio_oid,
  );

  if (isNullish(oids) || isEmpty(oids)) {
    return orgsByOrganisaatiotyyppi;
  } else {
    return orgsByOrganisaatiotyyppi.filter((o) => {
      return o.parent_oids.some((oid) => oids?.includes(oid));
    });
  }
};

export const getKoulutustoimijatToShow = (
  organisaatiot: Array<OrganisaatioHierarkia>,
  koulutustoimijaOid?: string,
) => {
  const koulutustoimijaOrgs = uniqueBy(
    organisaatiot?.flatMap((o) =>
      findOrganisaatiotWithOrganisaatiotyyppi(
        o,
        KOULUTUSTOIMIJAORGANISAATIOTYYPPI,
      ),
    ) || [],
    (o) => o.organisaatio_oid,
  );

  if (isNullish(koulutustoimijaOid)) {
    return koulutustoimijaOrgs;
  } else {
    return koulutustoimijaOrgs.filter((o) =>
      o.parent_oids.includes(koulutustoimijaOid),
    );
  }
};

export const getOppilaitoksetToShow = (
  selectedKoulutustoimijat: Array<OrganisaatioHierarkia>,
  oppilaitosOids: Array<string>,
) => {
  const oppilaitokset = getUniqueOrganisaatiotByOrganisaatiotyyppi(
    selectedKoulutustoimijat,
    OPPILAITOSORGANISAATIOTYYPPI,
  );

  if (isNullish(oppilaitosOids) || isEmpty(oppilaitosOids)) {
    return oppilaitokset;
  } else {
    return oppilaitokset.filter((o) => {
      return o.parent_oids.some((oid) => oppilaitosOids?.includes(oid));
    });
  }
};

export const getToimipisteetToShow = (
  selectedOppilaitokset: Array<OrganisaatioHierarkia>,
  toimipisteOids: Array<string>,
) => {
  const toimipisteet = getUniqueOrganisaatiotByOrganisaatiotyyppi(
    selectedOppilaitokset,
    TOIMIPISTEORGANISAATIOTYYPPI,
  );

  if (isNullish(toimipisteOids) || isEmpty(toimipisteOids)) {
    return toimipisteet;
  } else {
    return toimipisteet.filter((o) => {
      return o.parent_oids.some((oid) => toimipisteOids?.includes(oid));
    });
  }
};
