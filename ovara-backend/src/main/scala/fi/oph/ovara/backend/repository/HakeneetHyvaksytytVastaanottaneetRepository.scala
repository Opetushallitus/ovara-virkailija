package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.{HakeneetHyvaksytytVastaanottaneetHakukohteittain, HakeneetHyvaksytytVastaanottaneetResult}
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.springframework.stereotype.Component
import slick.sql.SqlStreamingAction
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*

@Component
class HakeneetHyvaksytytVastaanottaneetRepository extends Extractors {

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
    val hakuStr = RepositoryUtils.makeListOfValuesQueryStr(haut)
    val raportointiorganisaatiotStr = RepositoryUtils.makeListOfValuesQueryStr(selectedKayttooikeusOrganisaatiot)
    val harkinnanvaraisuudetWithSureValues = RepositoryUtils.enrichHarkinnanvaraisuudet(harkinnanvaraisuudet)
    val opetuskieletFilter = if (opetuskielet.nonEmpty) {
      val opetuskieletStr = opetuskielet.map(k => s"'$k'").mkString(", ")
      s"""AND EXISTS (
              SELECT 1 FROM jsonb_array_elements_text(h.oppilaitoksen_opetuskieli) AS elem
              WHERE elem IN ($opetuskieletStr)
            )"""
    } else ""

    sql"""SELECT h.hakukohde_nimi, h.organisaatio_nimi, SUM(t.hakijat) AS hakijat, SUM(t.ensisijaisia) AS ensisijaisia, SUM(t.varasija) AS varasija, SUM(t.hyvaksytyt) AS hyvaksytyt,
    SUM(t.vastaanottaneet) AS vastaanottaneet, SUM(t.lasna) AS lasna, SUM(t.poissa) AS poissa, SUM(t.ilm_yht) AS ilm_yht, MIN(t.aloituspaikat) AS aloituspaikat,
    SUM(t.toive_1) AS toive1, SUM(t.toive_2) AS toive2, SUM(t.toive_3) AS toive3, SUM(t.toive_4) AS toive4, SUM(t.toive_5) AS toive5, SUM(t.toive_6) AS toive6, SUM(t.toive_7) AS toive7
    FROM pub.pub_fct_raportti_tilastoraportti_toinen_aste t
    JOIN pub.pub_dim_hakukohde h
    ON t.hakukohde_oid = h.hakukohde_oid
    WHERE h.haku_oid IN (#$hakuStr)
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.jarjestyspaikka_oid", selectedKayttooikeusOrganisaatiot)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.hakukohde_oid", hakukohteet)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_1", koulutusalat1)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_2", koulutusalat2)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_3", koulutusalat3)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.sijaintimaakunta", maakunnat)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.sijaintikunta", kunnat)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.harkinnanvaraisuuden_syy", harkinnanvaraisuudetWithSureValues)}
    #$opetuskieletFilter
    GROUP BY h.hakukohde_nimi, h.organisaatio_nimi""".as[HakeneetHyvaksytytVastaanottaneetHakukohteittain]
  }

  def selectHakijatYhteensaHakukohteittainWithParams(
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
    val hakuStr = RepositoryUtils.makeListOfValuesQueryStr(haut)
    val raportointiorganisaatiotStr = RepositoryUtils.makeListOfValuesQueryStr(selectedKayttooikeusOrganisaatiot)
    val harkinnanvaraisuudetWithSureValues = RepositoryUtils.enrichHarkinnanvaraisuudet(harkinnanvaraisuudet)
    val opetuskieletFilter = if (opetuskielet.nonEmpty) {
      val opetuskieletStr = opetuskielet.map(k => s"'$k'").mkString(", ")
      s"""AND EXISTS (
              SELECT 1 FROM jsonb_array_elements_text(h.oppilaitoksen_opetuskieli) AS elem
              WHERE elem IN ($opetuskieletStr)
            )"""
    } else ""

    sql"""SELECT count(distinct ht.henkilo_oid)
    FROM pub.pub_fct_raportti_tilastoraportti_toinen_aste t
    JOIN pub.pub_dim_hakutoive ht
    ON t.hakukohde_oid = ht.hakukohde_oid
    JOIN pub.pub_dim_hakukohde h
    ON t.hakukohde_oid = h.hakukohde_oid
    WHERE h.haku_oid IN (#$hakuStr)
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.jarjestyspaikka_oid", selectedKayttooikeusOrganisaatiot)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.hakukohde_oid", hakukohteet)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_1", koulutusalat1)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_2", koulutusalat2)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_3", koulutusalat3)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.sijaintimaakunta", maakunnat)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.sijaintikunta", kunnat)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.harkinnanvaraisuuden_syy", harkinnanvaraisuudetWithSureValues)}
    #$opetuskieletFilter
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
    val hakuStr = RepositoryUtils.makeListOfValuesQueryStr(haut)
    val raportointiorganisaatiotStr = RepositoryUtils.makeListOfValuesQueryStr(selectedKayttooikeusOrganisaatiot)
    val harkinnanvaraisuudetWithSureValues = RepositoryUtils.enrichHarkinnanvaraisuudet(harkinnanvaraisuudet)
    val opetuskieletFilter = if (opetuskielet.nonEmpty) {
      val opetuskieletStr = opetuskielet.map(k => s"'$k'").mkString(", ")
      s"""AND EXISTS (
              SELECT 1 FROM jsonb_array_elements_text(h.oppilaitoksen_opetuskieli) AS elem
              WHERE elem IN ($opetuskieletStr)
            )"""
    } else ""

    sql"""SELECT ka.kansallinenkoulutusluokitus2016koulutusalataso1_nimi as otsikko, SUM(DISTINCT t.hakijat) AS hakijat, SUM(DISTINCT t.ensisijaisia) AS ensisijaisia, SUM(DISTINCT t.varasija) AS varasija, SUM(DISTINCT t.hyvaksytyt) AS hyvaksytyt,
    SUM(DISTINCT t.vastaanottaneet) AS vastaanottaneet, SUM(DISTINCT t.lasna) AS lasna, SUM(DISTINCT t.poissa) AS poissa, SUM(DISTINCT t.ilm_yht) AS ilm_yht, MIN(DISTINCT t.aloituspaikat) AS aloituspaikat,
    SUM(DISTINCT t.toive_1) AS toive1, SUM(DISTINCT t.toive_2) AS toive2, SUM(DISTINCT t.toive_3) AS toive3, SUM(DISTINCT t.toive_4) AS toive4, SUM(DISTINCT t.toive_5) AS toive5, SUM(DISTINCT t.toive_6) AS toive6, SUM(DISTINCT t.toive_7) AS toive7
    FROM pub.pub_fct_raportti_tilastoraportti_toinen_aste t
    JOIN pub.pub_dim_hakukohde h
    ON t.hakukohde_oid = h.hakukohde_oid
    JOIN pub.pub_dim_koodisto_koulutus_alat_ja_asteet ka
    ON t.koulutusalataso_1 = ka.kansallinenkoulutusluokitus2016koulutusalataso1
    WHERE h.haku_oid IN (#$hakuStr)
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.jarjestyspaikka_oid", selectedKayttooikeusOrganisaatiot)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.hakukohde_oid", hakukohteet)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_1", koulutusalat1)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_2", koulutusalat2)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_3", koulutusalat3)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.sijaintimaakunta", maakunnat)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.sijaintikunta", kunnat)}
    #${RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.harkinnanvaraisuuden_syy", harkinnanvaraisuudetWithSureValues)}
    #$opetuskieletFilter
    GROUP BY ka.kansallinenkoulutusluokitus2016koulutusalataso1_nimi""".as[HakeneetHyvaksytytVastaanottaneetResult]
  }

}
