import { sort, isEmpty, uniqueBy, isNullish } from 'remeda';
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
  selectedOppilaitosOids: Array<string> | null,
  selectedKoulutustoimija: string | null,
) => {
  const toimipisteet = getUniqueOrganisaatiotByOrganisaatiotyyppi(
    hierarkiat,
    TOIMIPISTEORGANISAATIOTYYPPI,
  );

  if (isNullish(selectedOppilaitosOids) || isEmpty(selectedOppilaitosOids)) {
    if (isNullish(selectedKoulutustoimija)) {
      return toimipisteet;
    }

    return toimipisteet.filter((o) => {
      return o.parent_oids.includes(selectedKoulutustoimija);
    });
  } else {
    return toimipisteet.filter((o) => {
      return o.parent_oids.some((oid) => selectedOppilaitosOids?.includes(oid));
    });
  }
};
