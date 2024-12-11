package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.{Haku, Organisaatio, ammatillisetHakukohdekoodit}
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
          from (select organisaatio_oid, organisaatio_nimi, jsonb_array_elements(organisaatiotyypit) as organisaatiotyyppi
	            from pub.pub_dim_organisaatio o) as org
          where org.organisaatio_oid in (#$organisaatiotStr)
          and org.organisaatiotyyppi = '"#$organisaatiotyyppi"'""".as[Organisaatio]
  }
}
