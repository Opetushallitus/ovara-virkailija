import { sort, isEmpty } from 'remeda';
import { set } from 'lodash';
import { OrganisaatioHierarkia } from './types/common';

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
