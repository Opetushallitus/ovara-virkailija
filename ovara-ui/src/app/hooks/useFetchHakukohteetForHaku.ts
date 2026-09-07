import { useQuery } from '@tanstack/react-query';
import { doApiFetch } from '@/app/lib/ovara-backend/api';
import { isNullish } from 'remeda';
import { useFetchOrganisaatiohierarkiat } from '@/app/hooks/useFetchOrganisaatiohierarkiat';
import { findOrganisaatio } from '@/app/lib/utils';
import {
  KOULUTUSTOIMIJAORGANISAATIOTYYPPI,
  OPPILAITOSORGANISAATIOTYYPPI,
  TOIMIPISTEORGANISAATIOTYYPPI,
} from '@/app/lib/constants';
import { OrganisaatioHierarkia } from '@/app/lib/types/common';

/**
 * `hakukohteet`-rajapinta rajaa organisaation mukaan eri parametrilla kullakin
 * organisaatiotasolla, ja backendin `getAllowedOrgOidsFromOrgSelection`
 * tarkistaa tasot järjestyksessä toimipiste -> oppilaitos -> koulutustoimija.
 * Siksi oid on lähetettävä nimenomaan omaa tasoaan vastaavana parametrina.
 */
const getOrganisaatioQueryParam = (
  organisaatio: OrganisaatioHierarkia | undefined,
) => {
  if (isNullish(organisaatio)) {
    return null;
  }
  const tyypit = organisaatio.organisaatiotyypit;
  if (tyypit.includes(TOIMIPISTEORGANISAATIOTYYPPI)) {
    return `&ovara_toimipisteet=${organisaatio.organisaatio_oid}`;
  }
  if (tyypit.includes(OPPILAITOSORGANISAATIOTYYPPI)) {
    return `&ovara_oppilaitokset=${organisaatio.organisaatio_oid}`;
  }
  if (tyypit.includes(KOULUTUSTOIMIJAORGANISAATIOTYYPPI)) {
    return `&ovara_koulutustoimija=${organisaatio.organisaatio_oid}`;
  }
  return null;
};

/**
 * Fetch hakukohteet scoped to a single hakuOid, and further narrowed by a single
 * organisaatioOid and/or a single hakukohderyhmaOid — decoupled from the shared
 * `useCommonSearchParams` state (which stores selectedHaut as an array). Used by
 * pages that submit a single hakuOid to the backend. Options stay empty until haku
 * plus at least one of organisaatio / hakukohderyhmä is set.
 *
 * Kun organisaatiota ei ole valittu, backendin `getAllowedOrgOidsFromOrgSelection`
 * rajaa hakukohteet käyttäjän omiin oikeuksiin, joten ryhmävalinta yksinään riittää.
 */
export const useFetchHakukohteetForHaku = (
  hakuOid: string | null,
  organisaatioOid: string | null,
  hakukohderyhmaOid: string | null = null,
) => {
  const { data: organisaatiot } = useFetchOrganisaatiohierarkiat();

  const organisaatioQueryStr = getOrganisaatioQueryParam(
    findOrganisaatio(organisaatiot ?? null, organisaatioOid),
  );
  const hakukohderyhmaQueryStr = isNullish(hakukohderyhmaOid)
    ? ''
    : `&ovara_hakukohderyhmat=${hakukohderyhmaOid}`;

  // Organisaatiohierarkia ratkeaa vasta oman kyselynsä jälkeen. Jos organisaatio on
  // valittu mutta sen taso ei ole vielä selvillä, odotetaan -- muuten haettaisiin
  // hetkellisesti organisaatiorajauksetta ja saataisiin liian laaja lista.
  const organisaatioPending =
    !isNullish(organisaatioOid) && isNullish(organisaatioQueryStr);

  return useQuery({
    queryKey: [
      'fetchHakukohteetForHaku',
      { hakuOid, organisaatioOid, hakukohderyhmaOid },
    ],
    queryFn: () =>
      doApiFetch('hakukohteet', {
        queryParams: `?ovara_haut=${hakuOid}${organisaatioQueryStr ?? ''}${hakukohderyhmaQueryStr}`,
      }),
    enabled:
      !isNullish(hakuOid) &&
      !organisaatioPending &&
      (!isNullish(organisaatioQueryStr) || !isNullish(hakukohderyhmaOid)),
    staleTime: 5 * 60 * 1000,
    gcTime: 30 * 60 * 1000,
  });
};
