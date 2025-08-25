package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.utils.{ParametriNimet}
import fi.oph.ovara.backend.domain.OrganisaationKoulutusToteutusHakukohde
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.{Component, Repository}
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
@Repository
class KoulutuksetToteutuksetHakukohteetRepository extends Extractors {
  val LOG: Logger = LoggerFactory.getLogger(classOf[KoulutuksetToteutuksetHakukohteetRepository])

  def selectWithParams(
      selectedKayttooikeusOrganisaatiot: List[String],
      haut: List[String],
      koulutuksenTila: Option[String],
      toteutuksenTila: Option[String],
      hakukohteenTila: Option[String],
      valintakoe: Option[Boolean]
  ): SqlStreamingAction[Vector[
    OrganisaationKoulutusToteutusHakukohde
  ], OrganisaationKoulutusToteutusHakukohde, Effect] = {
    val raportointiorganisaatiotStr = RepositoryUtils.makeListOfValuesQueryStr(selectedKayttooikeusOrganisaatiot)
    val query = sql"""SELECT hk.hakukohde_nimi,
                 hk.hakukohde_oid,
                 k.tila AS koulutuksen_tila,
                 t.tila AS toteutuksen_tila,
                 hk.tila AS hakukohteen_tila,
                 hk.hakukohteen_aloituspaikat,
                 hk.on_valintakoe,
                 hk.toinen_aste_onko_kaksoistutkinto,
                 hk.jarjestaa_urheilijan_ammkoulutusta,
                 o.organisaatio_oid
          FROM pub.pub_dim_haku h
          JOIN pub.pub_dim_hakukohde hk
          ON h.haku_oid = hk.haku_oid
          JOIN pub.pub_dim_toteutus t
          ON hk.toteutus_oid = t.toteutus_oid
          JOIN pub.pub_dim_koulutus k
          ON k.koulutus_oid = t.koulutus_oid
          JOIN pub.pub_dim_organisaatio o
          ON jarjestyspaikka_oid = o.organisaatio_oid
          WHERE h.haku_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(haut)})
          AND (hk.jarjestyspaikka_oid IN (#$raportointiorganisaatiotStr))
          #${RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "k.tila", koulutuksenTila)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "t.tila", toteutuksenTila)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "hk.tila", hakukohteenTila)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "hk.on_valintakoe", valintakoe)}
          """.as[OrganisaationKoulutusToteutusHakukohde]

    LOG.debug(s"selectWithParams: ${query.statements.head}")

    query
  }

  def hakuParamNamesQuery(haut: List[String], koulutustoimija: Option[String], oppilaitokset: List[String], toimipisteet: List[String]):
  SqlStreamingAction[Vector[ParametriNimet], ParametriNimet, Effect] = {
    val koulutustoimijaQuery =
      if(koulutustoimija.isDefined)
       s"""UNION ALL SELECT 'koulutustoimija' AS param, organisaatio_nimi AS nimi FROM pub.pub_dim_organisaatio WHERE organisaatio_oid = '${koulutustoimija.get}'"""
      else
        ""
    val oppilaitosQuery = RepositoryUtils.makeHakuParamOptionalQueryStr("oppilaitos", "organisaatio_oid", "organisaatio_nimi", "pub.pub_dim_organisaatio", oppilaitokset)
    val toimipisteQuery = RepositoryUtils.makeHakuParamOptionalQueryStr("toimipiste", "organisaatio_oid", "organisaatio_nimi", "pub.pub_dim_organisaatio", toimipisteet)

    val query = sql"""
      SELECT param, jsonb_agg(nimi) AS nimet
      FROM (
        SELECT 'haku' AS param, haku_nimi AS nimi from pub.pub_dim_haku
        WHERE haku_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(haut)})
        #$koulutustoimijaQuery
        #$oppilaitosQuery
        #$toimipisteQuery
      ) subquery
      GROUP BY param
    """.as[ParametriNimet]
    LOG.debug(s"hakuParamNamesQuery: ${query.statements.head}")
    query
  }
}
