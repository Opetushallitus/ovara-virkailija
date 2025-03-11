package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.KkHakeneetHyvaksytytVastaanottaneetHakukohteittain
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.springframework.stereotype.Component
import slick.dbio.{DBIO, Effect}
import slick.sql.SqlStreamingAction
import slick.jdbc.PostgresProfile.api.*

@Component
class KkHakeneetHyvaksytytVastaanottaneetRepository extends Extractors {

  private def buildFilters(
                            haut: List[String],
                            selectedKayttooikeusOrganisaatiot: List[String],
                            hakukohteet: List[String],
                            okmOhjauksenAlat: List[String],
                            aidinkielet: List[String],
                            kansalaisuudet: List[String],
                            sukupuoli: Option[String],
                            ensikertalainen: Option[Boolean],
                          ): String = {

    val filters = Seq(
      Some(s"h.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})"),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.jarjestyspaikka_oid", selectedKayttooikeusOrganisaatiot)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.hakukohde_oid", hakukohteet)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.okm_ohjauksen_ala", okmOhjauksenAlat)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.aidinkieli", aidinkielet)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.kansalaisuus", kansalaisuudet)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "t.sukupuoli", sukupuoli)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "t.ensikertalainen", ensikertalainen)).filter(_.nonEmpty),
    ).collect { case Some(value) => value }.mkString("\n")

    filters
  }

  def selectHakukohteittainWithParams(
                                       selectedKayttooikeusOrganisaatiot: List[String],
                                       haut: List[String],
                                       hakukohteet: List[String],
                                       okmOhjauksenAlat: List[String],
                                       tutkinnonTasot: List[String],
                                       aidinkielet: List[String],
                                       kansalaisuudet: List[String],
                                       sukupuoli: Option[String],
                                       ensikertalainen: Option[Boolean],
                                     ): SqlStreamingAction[Vector[KkHakeneetHyvaksytytVastaanottaneetHakukohteittain], KkHakeneetHyvaksytytVastaanottaneetHakukohteittain, Effect] = {

    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, okmOhjauksenAlat, aidinkielet, kansalaisuudet, sukupuoli, ensikertalainen
    )

    sql"""SELECT h.hakukohde_nimi, h.organisaatio_nimi, SUM(t.hakijat) AS hakijat, SUM(t.ensisijaisia) AS ensisijaisia, SUM(t.ensikertalaisia) AS ensikertalaisia,
    SUM(t.hyvaksytyt) AS hyvaksytyt, SUM(t.vastaanottaneet) AS vastaanottaneet, SUM(t.lasna) AS lasna, SUM(t.poissa) AS poissa, SUM(t.ilm_yht) AS ilm_yht,
    SUM(t.maksuvelvollisia) AS maksuvelvollisia, MIN(t.valinnan_aloituspaikat) AS valinnan_aloituspaikat, MIN(h.hakukohteen_aloituspaikat) AS aloituspaikat,
    SUM(t.toive_1) AS toive1, SUM(t.toive_2) AS toive2, SUM(t.toive_3) AS toive3, SUM(t.toive_4) AS toive4, SUM(t.toive_5) AS toive5, SUM(t.toive_6) AS toive6
    FROM pub.pub_fct_raportti_tilastoraportti_kk t
    JOIN pub.pub_dim_hakukohde h
    ON t.hakukohde_oid = h.hakukohde_oid
    WHERE #$filters
    GROUP BY h.hakukohde_nimi, h.organisaatio_nimi""".as[KkHakeneetHyvaksytytVastaanottaneetHakukohteittain]
  }

  def selectHakijatYhteensaWithParams(
                                       selectedKayttooikeusOrganisaatiot: List[String],
                                       haut: List[String],
                                       hakukohteet: List[String],
                                       okmOhjauksenAlat: List[String],
                                       tutkinnonTasot: List[String],
                                       aidinkielet: List[String],
                                       kansalaisuudet: List[String],
                                       sukupuoli: Option[String],
                                       ensikertalainen: Option[Boolean],
                                     ): DBIO[Int] = {

    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, okmOhjauksenAlat, aidinkielet, kansalaisuudet, sukupuoli, ensikertalainen
    )

    sql"""SELECT count(distinct ht.henkilo_oid)
    FROM pub.pub_fct_raportti_tilastoraportti_kk t
    JOIN pub.pub_dim_hakutoive ht
    ON t.hakukohde_oid = ht.hakukohde_oid
    JOIN pub.pub_dim_hakukohde h
    ON t.hakukohde_oid = h.hakukohde_oid
    WHERE #$filters
    """.as[Int].head
  }

  def selectEnsikertalaisetHakijatYhteensaWithParams(
                                       selectedKayttooikeusOrganisaatiot: List[String],
                                       haut: List[String],
                                       hakukohteet: List[String],
                                       okmOhjauksenAlat: List[String],
                                       tutkinnonTasot: List[String],
                                       aidinkielet: List[String],
                                       kansalaisuudet: List[String],
                                       sukupuoli: Option[String],
                                     ): DBIO[Int] = {

    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, okmOhjauksenAlat, aidinkielet, kansalaisuudet, sukupuoli, ensikertalainen = Some(true)
    )

    sql"""SELECT count(distinct ht.henkilo_oid)
      FROM pub.pub_fct_raportti_tilastoraportti_kk t
      JOIN pub.pub_dim_hakutoive ht
      ON t.hakukohde_oid = ht.hakukohde_oid
      JOIN pub.pub_dim_hakukohde h
      ON t.hakukohde_oid = h.hakukohde_oid
      WHERE #$filters
      """.as[Int].head
  }

/*
  def selectOKMOhjauksenAloittainWithParams(
                                         selectedKayttooikeusOrganisaatiot: List[String],
                                         haut: List[String],
                                         hakukohteet: List[String],
                                         sukupuoli: Option[String],
                                       ): SqlStreamingAction[Vector[HakeneetHyvaksytytVastaanottaneetResult], HakeneetHyvaksytytVastaanottaneetResult, Effect] = {

    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, sukupuoli
    )

    sql"""SELECT ka.koodinimi as otsikko, SUM(t.hakijat) AS hakijat, SUM(t.ensisijaisia) AS ensisijaisia, SUM(t.varasija) AS varasija, SUM(t.hyvaksytyt) AS hyvaksytyt,
    SUM(t.vastaanottaneet) AS vastaanottaneet, SUM(t.lasna) AS lasna, SUM(t.poissa) AS poissa, SUM(t.ilm_yht) AS ilm_yht, SUM(DISTINCT t.aloituspaikat) AS aloituspaikat,
    SUM(t.toive_1) AS toive1, SUM(t.toive_2) AS toive2, SUM(t.toive_3) AS toive3, SUM(t.toive_4) AS toive4, SUM(t.toive_5) AS toive5, SUM(t.toive_6) AS toive6, SUM(t.toive_7) AS toive7
    FROM pub.pub_fct_raportti_tilastoraportti_toinen_aste t
    JOIN pub.pub_dim_hakukohde h
    ON t.hakukohde_oid = h.hakukohde_oid
    JOIN pub.pub_dim_koodisto_koulutusalataso1 ka
    ON t.koulutusalataso_1 = ka.koodiarvo
    WHERE #$filters
    GROUP BY ka.koodinimi""".as[HakeneetHyvaksytytVastaanottaneetResult]
  }*/

/*  def selectOrganisaatioittainWithParams(
                                          selectedKayttooikeusOrganisaatiot: List[String],
                                          haut: List[String],
                                          hakukohteet: List[String],
                                          sukupuoli: Option[String],
                                          organisaatiotaso: String,
                                        ): SqlStreamingAction[Vector[HakeneetHyvaksytytVastaanottaneetResult], HakeneetHyvaksytytVastaanottaneetResult, Effect] = {
    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, sukupuoli
    )
    val organisaatioSelect = organisaatiotaso match {
      case "oppilaitoksittain" => "h.oppilaitos_nimi as otsikko"
      case _ => "h.koulutustoimija_nimi as otsikko"
    }
    val organisaatioGroupBy = organisaatiotaso match {
      case "oppilaitoksittain" => "h.oppilaitos_nimi"
      case _ => "h.koulutustoimija_nimi"
    }
    sql"""SELECT #$organisaatioSelect, SUM(t.hakijat) AS hakijat, SUM(t.ensisijaisia) AS ensisijaisia, SUM(t.varasija) AS varasija, SUM(t.hyvaksytyt) AS hyvaksytyt,
    SUM(t.vastaanottaneet) AS vastaanottaneet, SUM(t.lasna) AS lasna, SUM(t.poissa) AS poissa, SUM(t.ilm_yht) AS ilm_yht, SUM(DISTINCT t.aloituspaikat) AS aloituspaikat,
    SUM(t.toive_1) AS toive1, SUM(t.toive_2) AS toive2, SUM(t.toive_3) AS toive3, SUM(t.toive_4) AS toive4, SUM(t.toive_5) AS toive5, SUM(t.toive_6) AS toive6, SUM(t.toive_7) AS toive7
    FROM pub.pub_fct_raportti_tilastoraportti_toinen_aste t
    JOIN pub.pub_dim_hakukohde h
    ON t.hakukohde_oid = h.hakukohde_oid
    WHERE #$filters
    GROUP BY #$organisaatioGroupBy""".as[HakeneetHyvaksytytVastaanottaneetResult]
  }

  def selectToimipisteittainWithParams(
                                        selectedKayttooikeusOrganisaatiot: List[String],
                                        haut: List[String],
                                        hakukohteet: List[String],
                                        sukupuoli: Option[String],
                                      ): SqlStreamingAction[Vector[HakeneetHyvaksytytVastaanottaneetToimipisteittain], HakeneetHyvaksytytVastaanottaneetToimipisteittain, Effect] = {
    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, sukupuoli
    )

    sql"""SELECT h.toimipiste, h.organisaatio_nimi, SUM(t.hakijat) AS hakijat, SUM(t.ensisijaisia) AS ensisijaisia, SUM(t.varasija) AS varasija, SUM(t.hyvaksytyt) AS hyvaksytyt,
    SUM(t.vastaanottaneet) AS vastaanottaneet, SUM(t.lasna) AS lasna, SUM(t.poissa) AS poissa, SUM(t.ilm_yht) AS ilm_yht, SUM(DISTINCT t.aloituspaikat) AS aloituspaikat,
    SUM(t.toive_1) AS toive1, SUM(t.toive_2) AS toive2, SUM(t.toive_3) AS toive3, SUM(t.toive_4) AS toive4, SUM(t.toive_5) AS toive5, SUM(t.toive_6) AS toive6, SUM(t.toive_7) AS toive7
    FROM pub.pub_fct_raportti_tilastoraportti_toinen_aste t
    JOIN pub.pub_dim_hakukohde h
    ON t.hakukohde_oid = h.hakukohde_oid
    WHERE #$filters
    GROUP BY h.toimipiste, h.organisaatio_nimi""".as[HakeneetHyvaksytytVastaanottaneetToimipisteittain]
  }*/

}

