package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.{Haku, Organisaatio, OrganisaatioParentChild, ammatillisetHakukohdekoodit}
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

  def selectDistinctOrganisaatiotByOrganisaatiotyyppi(organisaatiot: List[String], organisaatiotyyppi: String): SqlStreamingAction[Vector[Organisaatio], Organisaatio, Effect] = {
    val organisaatiotStr = organisaatiot.map(s => s"'$s'").mkString(",")
    sql"""select distinct *
          from (select organisaatio_oid, organisaatio_nimi, jsonb_array_elements_text(organisaatiotyypit) as organisaatiotyyppi
	            from pub.pub_dim_organisaatio o) as org
          where org.organisaatio_oid in (#$organisaatiotStr)
          and org.organisaatiotyyppi = '"#$organisaatiotyyppi"'""".as[Organisaatio]
  }

  def selectDistinctOrganisaatiot(organisaatiot: List[String]): SqlStreamingAction[Vector[Organisaatio], Organisaatio, Effect] = {
    val organisaatiotStr = organisaatiot.map(s => s"'$s'").mkString(",")
    sql"""select distinct *
          from (select organisaatio_oid, organisaatio_nimi, jsonb_array_elements_text(organisaatiotyypit) as organisaatiotyyppi
                from pub.pub_dim_organisaatio o) as org
          where org.organisaatio_oid in (#$organisaatiotStr)""".as[Organisaatio]
  }

  def selectChildOrganisaatiot(organisaatiot: List[String]): SqlStreamingAction[Vector[OrganisaatioParentChild], OrganisaatioParentChild, Effect] = {
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
          ) SELECT * FROM x""".as[OrganisaatioParentChild]
  }
}
