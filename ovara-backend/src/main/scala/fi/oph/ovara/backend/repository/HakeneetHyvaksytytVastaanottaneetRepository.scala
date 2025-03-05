package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.{HakeneetHyvaksytytVastaanottaneetHakukohteittain, HakeneetHyvaksytytVastaanottaneetResult, HakeneetHyvaksytytVastaanottaneetToimipisteittain}
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.springframework.stereotype.Component
import slick.sql.SqlStreamingAction
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*

@Component
class HakeneetHyvaksytytVastaanottaneetRepository extends Extractors {

  private def buildFilters(
                            haut: List[String],
                            selectedKayttooikeusOrganisaatiot: List[String],
                            hakukohteet: List[String],
                            koulutusalat1: List[String],
                            koulutusalat2: List[String],
                            koulutusalat3: List[String],
                            opetuskielet: List[String],
                            maakunnat: List[String],
                            kunnat: List[String],
                            harkinnanvaraisuudet: List[String],
                            sukupuoli: Option[String]
                          ): String = {
    val harkinnanvaraisuudetWithSureValues = RepositoryUtils.enrichHarkinnanvaraisuudet(harkinnanvaraisuudet)

    val filters = Seq(
      Some(s"h.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})"),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.jarjestyspaikka_oid", selectedKayttooikeusOrganisaatiot)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.hakukohde_oid", hakukohteet)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_1", koulutusalat1)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_2", koulutusalat2)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_3", koulutusalat3)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.sijaintimaakunta", maakunnat)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.sijaintikunta", kunnat)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.harkinnanvaraisuuden_syy", harkinnanvaraisuudetWithSureValues)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "t.sukupuoli", sukupuoli)).filter(_.nonEmpty),
      buildOpetuskieletFilter(opetuskielet)
    ).collect { case Some(value) => value }.mkString("\n")

    filters
  }

  private def buildOpetuskieletFilter(opetuskielet: List[String]): Option[String] = {
    if (opetuskielet.nonEmpty) {
      val opetuskieletStr = opetuskielet.map(k => s"'$k'").mkString(", ")
      Some(
        s"""AND EXISTS (
           |  SELECT 1 FROM jsonb_array_elements_text(h.oppilaitoksen_opetuskieli) AS elem
           |  WHERE elem IN ($opetuskieletStr)
           |)""".stripMargin
      )
    } else None
  }

  def selectHakukohteittainWithParams(
                        selectedKayttooikeusOrganisaatiot: List[String],
                        haut: List[String],
                        hakukohteet: List[String],
                        koulutusalat1: List[String],
                        koulutusalat2: List[String],
                        koulutusalat3: List[String],
                        opetuskielet: List[String],
                        maakunnat: List[String],
                        kunnat: List[String],
                        harkinnanvaraisuudet: List[String],
                        sukupuoli: Option[String],
                      ): SqlStreamingAction[Vector[HakeneetHyvaksytytVastaanottaneetHakukohteittain], HakeneetHyvaksytytVastaanottaneetHakukohteittain, Effect] = {

    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, koulutusalat1, koulutusalat2, koulutusalat3,
      opetuskielet, maakunnat, kunnat, harkinnanvaraisuudet, sukupuoli
    )

    sql"""SELECT h.hakukohde_nimi, h.organisaatio_nimi, SUM(t.hakijat) AS hakijat, SUM(t.ensisijaisia) AS ensisijaisia, SUM(t.varasija) AS varasija, SUM(t.hyvaksytyt) AS hyvaksytyt,
    SUM(t.vastaanottaneet) AS vastaanottaneet, SUM(t.lasna) AS lasna, SUM(t.poissa) AS poissa, SUM(t.ilm_yht) AS ilm_yht, MIN(t.aloituspaikat) AS aloituspaikat,
    SUM(t.toive_1) AS toive1, SUM(t.toive_2) AS toive2, SUM(t.toive_3) AS toive3, SUM(t.toive_4) AS toive4, SUM(t.toive_5) AS toive5, SUM(t.toive_6) AS toive6, SUM(t.toive_7) AS toive7
    FROM pub.pub_fct_raportti_tilastoraportti_toinen_aste t
    JOIN pub.pub_dim_hakukohde h
    ON t.hakukohde_oid = h.hakukohde_oid
    WHERE #$filters
    GROUP BY h.hakukohde_nimi, h.organisaatio_nimi""".as[HakeneetHyvaksytytVastaanottaneetHakukohteittain]
  }

  def selectHakijatYhteensaWithParams(
                                       selectedKayttooikeusOrganisaatiot: List[String],
                                       haut: List[String],
                                       hakukohteet: List[String],
                                       koulutusalat1: List[String],
                                       koulutusalat2: List[String],
                                       koulutusalat3: List[String],
                                       opetuskielet: List[String],
                                       maakunnat: List[String],
                                       kunnat: List[String],
                                       harkinnanvaraisuudet: List[String],
                                       sukupuoli: Option[String],
                                     ): DBIO[Int] = {

    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, koulutusalat1, koulutusalat2, koulutusalat3,
      opetuskielet, maakunnat, kunnat, harkinnanvaraisuudet, sukupuoli
    )

    sql"""SELECT count(distinct ht.henkilo_oid)
    FROM pub.pub_fct_raportti_tilastoraportti_toinen_aste t
    JOIN pub.pub_dim_hakutoive ht
    ON t.hakukohde_oid = ht.hakukohde_oid
    JOIN pub.pub_dim_hakukohde h
    ON t.hakukohde_oid = h.hakukohde_oid
    WHERE #$filters
    """.as[Int].head
  }

  def selectKoulutusaloittainWithParams(
                                       selectedKayttooikeusOrganisaatiot: List[String],
                                       haut: List[String],
                                       hakukohteet: List[String],
                                       koulutusalat1: List[String],
                                       koulutusalat2: List[String],
                                       koulutusalat3: List[String],
                                       opetuskielet: List[String],
                                       maakunnat: List[String],
                                       kunnat: List[String],
                                       harkinnanvaraisuudet: List[String],
                                       sukupuoli: Option[String],
                                     ): SqlStreamingAction[Vector[HakeneetHyvaksytytVastaanottaneetResult], HakeneetHyvaksytytVastaanottaneetResult, Effect] = {

    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, koulutusalat1, koulutusalat2, koulutusalat3,
      opetuskielet, maakunnat, kunnat, harkinnanvaraisuudet, sukupuoli
    )

    sql"""SELECT ka.kansallinenkoulutusluokitus2016koulutusalataso1_nimi as otsikko, SUM(DISTINCT t.hakijat) AS hakijat, SUM(DISTINCT t.ensisijaisia) AS ensisijaisia, SUM(DISTINCT t.varasija) AS varasija, SUM(DISTINCT t.hyvaksytyt) AS hyvaksytyt,
    SUM(DISTINCT t.vastaanottaneet) AS vastaanottaneet, SUM(DISTINCT t.lasna) AS lasna, SUM(DISTINCT t.poissa) AS poissa, SUM(DISTINCT t.ilm_yht) AS ilm_yht, MIN(DISTINCT t.aloituspaikat) AS aloituspaikat,
    SUM(DISTINCT t.toive_1) AS toive1, SUM(DISTINCT t.toive_2) AS toive2, SUM(DISTINCT t.toive_3) AS toive3, SUM(DISTINCT t.toive_4) AS toive4, SUM(DISTINCT t.toive_5) AS toive5, SUM(DISTINCT t.toive_6) AS toive6, SUM(DISTINCT t.toive_7) AS toive7
    FROM pub.pub_fct_raportti_tilastoraportti_toinen_aste t
    JOIN pub.pub_dim_hakukohde h
    ON t.hakukohde_oid = h.hakukohde_oid
    JOIN pub.pub_dim_koodisto_koulutus_alat_ja_asteet ka
    ON t.koulutusalataso_1 = ka.kansallinenkoulutusluokitus2016koulutusalataso1
    WHERE #$filters
    GROUP BY ka.kansallinenkoulutusluokitus2016koulutusalataso1_nimi""".as[HakeneetHyvaksytytVastaanottaneetResult]
  }

  def selectOrganisaatioittainWithParams(
                                         selectedKayttooikeusOrganisaatiot: List[String],
                                         haut: List[String],
                                         hakukohteet: List[String],
                                         koulutusalat1: List[String],
                                         koulutusalat2: List[String],
                                         koulutusalat3: List[String],
                                         opetuskielet: List[String],
                                         maakunnat: List[String],
                                         kunnat: List[String],
                                         harkinnanvaraisuudet: List[String],
                                         sukupuoli: Option[String],
                                         organisaatiotaso: String,
                                       ): SqlStreamingAction[Vector[HakeneetHyvaksytytVastaanottaneetResult], HakeneetHyvaksytytVastaanottaneetResult, Effect] = {
    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, koulutusalat1, koulutusalat2, koulutusalat3,
      opetuskielet, maakunnat, kunnat, harkinnanvaraisuudet, sukupuoli
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
                                       koulutusalat1: List[String],
                                       koulutusalat2: List[String],
                                       koulutusalat3: List[String],
                                       opetuskielet: List[String],
                                       maakunnat: List[String],
                                       kunnat: List[String],
                                       harkinnanvaraisuudet: List[String],
                                       sukupuoli: Option[String],
                                     ): SqlStreamingAction[Vector[HakeneetHyvaksytytVastaanottaneetToimipisteittain], HakeneetHyvaksytytVastaanottaneetToimipisteittain, Effect] = {
    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, koulutusalat1, koulutusalat2, koulutusalat3,
      opetuskielet, maakunnat, kunnat, harkinnanvaraisuudet, sukupuoli
    )

    sql"""SELECT h.toimipiste, h.organisaatio_nimi, SUM(t.hakijat) AS hakijat, SUM(t.ensisijaisia) AS ensisijaisia, SUM(t.varasija) AS varasija, SUM(t.hyvaksytyt) AS hyvaksytyt,
    SUM(t.vastaanottaneet) AS vastaanottaneet, SUM(t.lasna) AS lasna, SUM(t.poissa) AS poissa, SUM(t.ilm_yht) AS ilm_yht, SUM(DISTINCT t.aloituspaikat) AS aloituspaikat,
    SUM(t.toive_1) AS toive1, SUM(t.toive_2) AS toive2, SUM(t.toive_3) AS toive3, SUM(t.toive_4) AS toive4, SUM(t.toive_5) AS toive5, SUM(t.toive_6) AS toive6, SUM(t.toive_7) AS toive7
    FROM pub.pub_fct_raportti_tilastoraportti_toinen_aste t
    JOIN pub.pub_dim_hakukohde h
    ON t.hakukohde_oid = h.hakukohde_oid
    WHERE #$filters
    GROUP BY h.toimipiste, h.organisaatio_nimi""".as[HakeneetHyvaksytytVastaanottaneetToimipisteittain]
  }

}
