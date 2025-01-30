package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.*
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.springframework.stereotype.Component
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
class CommonRepository extends Extractors {
  def selectDistinctAlkamisvuodet(): SqlStreamingAction[Vector[String], String, Effect] = {
    sql"""SELECT DISTINCT koulutuksen_alkamisvuosi
          FROM pub.pub_dim_toteutus pdt
          WHERE koulutuksen_alkamisvuosi IS NOT NULL""".as[String]
  }

  def selectDistinctExistingHaut(
      alkamiskaudet: List[String] = List()
  ): SqlStreamingAction[Vector[Haku], Haku, Effect] = {
    val alkamiskaudetAndHenkKohtSuunnitelma =
      RepositoryUtils.extractAlkamisvuosiKausiAndHenkkohtSuunnitelma(alkamiskaudet)

    val alkamiskaudetQueryStr = if (alkamiskaudet.isEmpty) {
      ""
    } else {
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(alkamiskaudetAndHenkKohtSuunnitelma)
    }

    sql"""SELECT DISTINCT h.haku_oid, h.haku_nimi
                  FROM pub.pub_dim_haku h
                  LEFT JOIN (
                    SELECT haku_oid, jsonb_array_elements(koulutuksen_alkamiskausi) as alkamiskausi
                    FROM pub.pub_dim_haku h
                  ) alkamiskaudet
                  ON h.haku_oid = alkamiskaudet.haku_oid
                  WHERE h.haun_tyyppi = 'toinen_aste'
                  AND h.tila != 'poistettu'
                  #$alkamiskaudetQueryStr""".as[Haku]
  }

  def selectDistinctExistingHakukohteetWithSelectedOrgsAsJarjestaja(
      orgs: List[String],
      haut: List[String]
  ): SqlStreamingAction[Vector[Hakukohde], Hakukohde, Effect] = {
    val organisaatiotStr = orgs.map(s => s"'$s'").mkString(",")
    val organisaatiotQueryStr = if (organisaatiotStr.isEmpty) {
      ""
    } else { s"AND hk.jarjestyspaikka_oid in ($organisaatiotStr)" }

    val hautStr = RepositoryUtils.makeListOfValuesQueryStr(haut)
    val hautQueryStr = if (hautStr.isEmpty) {
      ""
    } else {
      s"AND h.haku_oid in ($hautStr)"
    }

    sql"""SELECT DISTINCT hk.hakukohde_oid, hk.hakukohde_nimi
          FROM pub.pub_dim_hakukohde hk
          JOIN pub.pub_dim_haku h
          ON hk.haku_oid = h.haku_oid
          WHERE hk.tila != 'poistettu'
          #$organisaatiotQueryStr
          #$hautQueryStr
          """.as[Hakukohde]
  }

  def selectDistinctOrganisaatiot(
      organisaatiot: List[String]
  ): SqlStreamingAction[Vector[Organisaatio], Organisaatio, Effect] = {
    val organisaatiotStr = organisaatiot.map(s => s"'$s'").mkString(",")
    val optionalOrganisaatiotClause =
      if (organisaatiotStr.isEmpty) "" else s"where org.organisaatio_oid in ($organisaatiotStr)"
    sql"""SELECT DISTINCT *
          FROM (SELECT organisaatio_oid, organisaatio_nimi, organisaatiotyypit
                FROM pub.pub_dim_organisaatio o) AS org
                #$optionalOrganisaatiotClause""".as[Organisaatio]
  }

  def selectDistinctKoulutustoimijat(
      organisaatiot: List[String]
  ): SqlStreamingAction[Vector[Organisaatio], Organisaatio, Effect] = {
    val organisaatiotStr = organisaatiot.map(s => s"'$s'").mkString(",")
    val optionalOrganisaatiotClause =
      if (organisaatiotStr.isEmpty) "" else s"where org.organisaatio_oid in ($organisaatiotStr)"
    sql"""SELECT DISTINCT *
          FROM (SELECT organisaatio_oid, organisaatio_nimi, organisaatiotyypit
                FROM pub.pub_dim_organisaatio o
            WHERE organisaatiotyypit ?? '01') AS org
                #$optionalOrganisaatiotClause""".as[Organisaatio]
  }

  def selectChildOrganisaatiot(
      organisaatiot: List[String]
  ): SqlStreamingAction[Vector[OrganisaatioParentChild], OrganisaatioParentChild, Effect] = {
    val organisaatiotStr = organisaatiot.map(s => s"'$s'").mkString(",")

    sql"""WITH RECURSIVE x AS (
            SELECT parent_oid, child_oid
            FROM pub.pub_dim_organisaatio_rakenne or1
            WHERE parent_oid in (#$organisaatiotStr)
            UNION
            SELECT or2.parent_oid, or2.child_oid
            FROM pub.pub_dim_organisaatio_rakenne or2
            INNER JOIN x x1
            ON or2.parent_oid = x1.child_oid
          ) SELECT parent_oid, child_oid, organisaatio_nimi, organisaatiotyypit
            FROM x
            inner join pub.pub_dim_organisaatio org
            on org.organisaatio_oid = x.child_oid""".as[OrganisaatioParentChild]
  }

  val selectOrganisaatioHierarkiaSql =
    """SELECT organisaatio_oid,
              organisaatio_nimi,
              organisaatiotyypit,
              oppilaitostyyppi,
              tila,
              parent_oids,
              children"""

  def selectKoulutustoimijaDescendants(
      koulutustoimijaOids: List[String]
  ): SqlStreamingAction[Vector[OrganisaatioHierarkia], OrganisaatioHierarkia, Effect] = {
    val organisaatiotStr = koulutustoimijaOids.map(s => s"'$s'").mkString(",")
    sql"""#$selectOrganisaatioHierarkiaSql
          FROM pub.pub_dim_koulutustoimija_ja_toimipisteet,
          LATERAL jsonb_array_elements_text(parent_oids) AS parent_oid
          WHERE parent_oid IN (#$organisaatiotStr)
          """.as[OrganisaatioHierarkia]
  }

  def selectOppilaitosDescendants(
      oids: List[String]
  ): SqlStreamingAction[Vector[OrganisaatioHierarkia], OrganisaatioHierarkia, Effect] = {
    val organisaatiotStr = oids.map(s => s"'$s'").mkString(",")
    sql"""#$selectOrganisaatioHierarkiaSql
          FROM pub.pub_dim_oppilaitos_ja_toimipisteet,
          LATERAL jsonb_array_elements_text(parent_oids) AS parent_oid
          WHERE parent_oid IN (#$organisaatiotStr)
          """.as[OrganisaatioHierarkia]
  }

  def selectToimipisteDescendants(
      oids: List[String]
  ): SqlStreamingAction[Vector[OrganisaatioHierarkia], OrganisaatioHierarkia, Effect] = {
    val organisaatiotStr = oids.map(s => s"'$s'").mkString(",")
    sql"""#$selectOrganisaatioHierarkiaSql
          FROM pub.pub_dim_toimipiste_ja_toimipisteet,
          LATERAL jsonb_array_elements_text(parent_oids) AS parent_oid
          WHERE parent_oid IN (#$organisaatiotStr)
          """.as[OrganisaatioHierarkia]
  }
}
