package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.*
import org.springframework.stereotype.Component
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
class CommonRepository extends Extractors {
  def selectDistinctAlkamisvuodet(): SqlStreamingAction[Vector[String], String, Effect] = {
    sql"""select distinct koulutuksen_alkamisvuosi
          from pub.pub_dim_toteutus pdt
          where koulutuksen_alkamisvuosi is not null""".as[String]
  }

  def selectDistinctExistingHaut(): SqlStreamingAction[Vector[Haku], Haku, Effect] = {
    val hakukohdekooditStr = ammatillisetHakukohdekoodit.map(s => s"'$s'").mkString(",")
    sql"""select distinct haku_oid, haku_nimi
          from pub.pub_dim_haku h
          where kohdejoukko_koodi in (#$hakukohdekooditStr)
          and h.tila != 'poistettu'""".as[Haku]
  }

  def selectDistinctOrganisaatiot(
      organisaatiot: List[String]
  ): SqlStreamingAction[Vector[Organisaatio], Organisaatio, Effect] = {
    val organisaatiotStr = organisaatiot.map(s => s"'$s'").mkString(",")
    val optionalOrganisaatiotClause =
      if (organisaatiotStr.isEmpty) "" else s"where org.organisaatio_oid in ($organisaatiotStr)"
    sql"""select distinct *
          from (select organisaatio_oid, organisaatio_nimi, organisaatiotyypit
                from pub.pub_dim_organisaatio o) as org
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
