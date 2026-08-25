import { sort, isEmpty, uniqueBy, isNullish } from 'remeda';
import { LanguageCode, OrganisaatioHierarkia } from './types/common';
import {
  KK_RAPORTIT,
  KK_YOS_RAPORTTI,
  KOULUTUSTOIMIJAORGANISAATIOTYYPPI,
  OPPILAITOSORGANISAATIOTYYPPI,
  TIEDONSIIRTO_KK_RAPORTIT,
  TIEDONSIIRTO_RAPORTIT,
  TOIMIPISTEORGANISAATIOTYYPPI,
  TOISEN_ASTEEN_RAPORTIT,
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

export const hasOvaraKkYosRole = (userRoles?: Array<string>) => {
  return (
    userRoles?.includes('ROLE_APP_OVARA-VIRKAILIJA_KK_YOS') ||
    userRoles?.includes('ROLE_APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA')
  );
};

export const hasOvaraHakeneetRole = (userRoles?: Array<string>) => {
  return (
    userRoles?.includes('ROLE_APP_OVARA-VIRKAILIJA_HAKENEET') ||
    userRoles?.includes('ROLE_APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA')
  );
};

export const hasOvaraKkHakeneetRole = (userRoles?: Array<string>) => {
  return (
    userRoles?.includes('ROLE_APP_OVARA-VIRKAILIJA_KK_HAKENEET') ||
    userRoles?.includes('ROLE_APP_OVARA-VIRKAILIJA_OPH_PAAKAYTTAJA')
  );
};

export const getRaporttiListByUserRights = (userRoles?: Array<string>) => {
  const raportit = [];

  if (hasOvaraToinenAsteRole(userRoles)) {
    raportit.push(...TOISEN_ASTEEN_RAPORTIT);
  }

  if (hasOvaraKkRole(userRoles)) {
    raportit.push(...KK_RAPORTIT);
  }

  if (hasOvaraKkYosRole(userRoles)) {
    raportit.push(...[KK_YOS_RAPORTTI]);
  }

  if (hasOvaraHakeneetRole(userRoles)) {
    raportit.push(...TIEDONSIIRTO_RAPORTIT);
  }

  if (hasOvaraKkHakeneetRole(userRoles)) {
    raportit.push(...TIEDONSIIRTO_KK_RAPORTIT);
  }

  return raportit;
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

/**
 * Rakentaa tiedonsiirtoraporttien (external-rajapinta) kyselyparametrit.
 * Rajapinta ottaa vastaan joko hakukohteen tai organisaation, ei molempia:
 * jos hakukohde on valittu, lähetetään sen oid, muuten organisaation oid.
 */
export const buildTiedonsiirtoParams = ({
  hakuOid,
  hakukohdeOid,
  organisaatioOid,
  valintarajaus,
}: {
  hakuOid: string | null;
  hakukohdeOid: string | null;
  organisaatioOid: string | null;
  valintarajaus: string | null;
}): string => {
  const params = new URLSearchParams();
  if (hakuOid) params.set('hakuOid', hakuOid);
  if (hakukohdeOid) params.set('hakukohdeOid', hakukohdeOid);
  else if (organisaatioOid) params.set('organisaatioOid', organisaatioOid);
  if (valintarajaus) params.set('valintarajaus', valintarajaus);
  return params.toString();
};

/**
 * Rakentaa KK-hakijoiden tiedonsiirtoraportin (external-rajapinta) kyselyparametrit.
 * Toisin kuin `buildTiedonsiirtoParams`, kaikki valitut rajaimet lähetetään ja backend
 * leikkaa ne keskenään: hakukohde JA hakukohderyhmä JA organisaatio. Organisaatio ei ole
 * pakollinen, jos hakukohderyhmä on valittu.
 */
export const buildKkHakijatTiedonsiirtoParams = ({
  hakuOid,
  hakukohdeOid,
  hakukohderyhmaOid,
  organisaatioOid,
  valintarajaus,
}: {
  hakuOid: string | null;
  hakukohdeOid: string | null;
  hakukohderyhmaOid: string | null;
  organisaatioOid: string | null;
  valintarajaus: string | null;
}): string => {
  const params = new URLSearchParams();
  if (hakuOid) params.set('hakuOid', hakuOid);
  if (hakukohdeOid) params.set('hakukohdeOid', hakukohdeOid);
  if (hakukohderyhmaOid) params.set('hakukohderyhmaOid', hakukohderyhmaOid);
  if (organisaatioOid) params.set('organisaatioOid', organisaatioOid);
  if (valintarajaus) params.set('valintarajaus', valintarajaus);
  return params.toString();
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

/**
 * Oppilaitostason organisaatiot valintalistan vaihtoehdoiksi. Järjestys jätetään
 * ComboBoxille (sortOptions oletuksena true), joka lajittelee nimen mukaan.
 */
export const getOppilaitosOptions = (
  hierarkiat: Array<OrganisaatioHierarkia> | null,
  locale: LanguageCode,
): Array<{ value: string; label: string }> =>
  getOppilaitoksetToShow(hierarkiat, null).map((org) => ({
    value: org.organisaatio_oid,
    label: org.organisaatio_nimi[locale]
      ? `${org.organisaatio_nimi[locale]}`
      : '',
  }));

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

/**
 * Toimipistetason organisaatiot valintalistan vaihtoehdoiksi. Vastinpari
 * `getOppilaitosOptions`-metodille; ks. sen kommentti.
 *
 * `selectedOppilaitos` rajaa listan kyseisen oppilaitoksen toimipisteisiin. Ilman
 * sitä palautetaan kaikki toimipisteet joihin käyttäjällä on oikeus -- se on
 * tarkoituksellista, jotta pelkän toimipisteoikeuden saanut käyttäjä voi valita
 * toimipisteen vaikkei näe yhtään oppilaitosta.
 */
export const getToimipisteOptions = (
  hierarkiat: Array<OrganisaatioHierarkia> | null,
  locale: LanguageCode,
  selectedOppilaitos: string | null = null,
): Array<{ value: string; label: string }> =>
  getToimipisteetToShow(
    hierarkiat,
    // Ei valittuja toimipisteitä: yhden valinnan listaa ei haluta laajentaa
    // rajauksen ulkopuolisilla toimipisteillä.
    null,
    isNullish(selectedOppilaitos) ? null : [selectedOppilaitos],
    null,
  ).map((org) => ({
    value: org.organisaatio_oid,
    label: org.organisaatio_nimi[locale]
      ? `${org.organisaatio_nimi[locale]}`
      : '',
  }));

/**
 * Kaikki organisaatiot joihin käyttäjällä on oikeus, tasoittain järjestettynä
 * (koulutustoimijat -> oppilaitokset -> toimipisteet). Toisin kuin
 * `getKoulutustoimijatToShow`, tämä ei jätä pois käyttäjää jonka oikeus on
 * koulutustoimijaa alemmalla tasolla.
 */
export const getKaikkiOrganisaatiotToShow = (
  organisaatiot: Array<OrganisaatioHierarkia> | null,
) => {
  return uniqueBy(
    [
      ...getUniqueOrganisaatiotByOrganisaatiotyyppi(
        organisaatiot,
        KOULUTUSTOIMIJAORGANISAATIOTYYPPI,
      ),
      ...getUniqueOrganisaatiotByOrganisaatiotyyppi(
        organisaatiot,
        OPPILAITOSORGANISAATIOTYYPPI,
      ),
      ...getUniqueOrganisaatiotByOrganisaatiotyyppi(
        organisaatiot,
        TOIMIPISTEORGANISAATIOTYYPPI,
      ),
    ],
    (o) => o.organisaatio_oid,
  );
};

export const findOrganisaatio = (
  organisaatiot: Array<OrganisaatioHierarkia> | null,
  organisaatioOid: string | null,
): OrganisaatioHierarkia | undefined => {
  if (isNullish(organisaatioOid)) {
    return undefined;
  }
  return getKaikkiOrganisaatiotToShow(organisaatiot).find(
    (o) => o.organisaatio_oid === organisaatioOid,
  );
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
