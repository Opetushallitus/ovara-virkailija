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
                            hakukohderyhmat: List[String],
                            okmOhjauksenAlat: List[String],
                            tutkinnonTasot: List[String],
                            aidinkielet: List[String],
                            kansalaisuudet: List[String],
                            sukupuoli: Option[String],
                            ensikertalainen: Option[Boolean],
                          ): String = {

    val filters = Seq(
      Some(s"h.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})"),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.jarjestyspaikka_oid", selectedKayttooikeusOrganisaatiot)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.hakukohde_oid", hakukohteet)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "hkr_hk.hakukohderyhma_oid", hakukohderyhmat)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.okm_ohjauksen_ala", okmOhjauksenAlat)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.aidinkieli", aidinkielet)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.kansalaisuus", kansalaisuudet)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "t.sukupuoli", sukupuoli)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "t.ensikertalainen", ensikertalainen)).filter(_.nonEmpty),
      buildTutkinnonTasoFilters(tutkinnonTasot)
    ).collect { case Some(value) => value }.mkString("\n")

    filters
  }

  def buildTutkinnonTasoFilters(
                                 tutkinnonTasot: List[String],
                               ): Option[String] = {
    if (tutkinnonTasot.nonEmpty) {
      var conditions = List[String]()
      if (tutkinnonTasot.contains("alempi-ja-ylempi")) {
        conditions = conditions :+ "h.alempi_kk_aste = true AND h.ylempi_kk_aste = true"
      }
      if (tutkinnonTasot.contains("alempi")) {
        conditions = conditions :+ "h.alempi_kk_aste = true AND h.ylempi_kk_aste = false"
      }
      if (tutkinnonTasot.contains("ylempi")) {
        conditions = conditions :+ "h.alempi_kk_aste = false AND h.ylempi_kk_aste = true"
      }
      Some(s"AND (" + conditions.mkString(" OR ") + ")")
    }
    else
      None
  }

  def selectHakukohteittainWithParams(
                                       selectedKayttooikeusOrganisaatiot: List[String],
                                       haut: List[String],
                                       hakukohteet: List[String],
                                       hakukohderyhmat: List[String],
                                       okmOhjauksenAlat: List[String],
                                       tutkinnonTasot: List[String],
                                       aidinkielet: List[String],
                                       kansalaisuudet: List[String],
                                       sukupuoli: Option[String],
                                       ensikertalainen: Option[Boolean],
                                     ): SqlStreamingAction[Vector[KkHakeneetHyvaksytytVastaanottaneetHakukohteittain], KkHakeneetHyvaksytytVastaanottaneetHakukohteittain, Effect] = {

    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, hakukohderyhmat, okmOhjauksenAlat, tutkinnonTasot, aidinkielet, kansalaisuudet, sukupuoli, ensikertalainen
    )

    sql"""SELECT h.hakukohde_nimi, h.organisaatio_nimi, SUM(t.hakijat) AS hakijat, SUM(t.ensisijaisia) AS ensisijaisia, SUM(t.ensikertalaisia) AS ensikertalaisia,
    SUM(t.hyvaksytyt) AS hyvaksytyt, SUM(t.vastaanottaneet) AS vastaanottaneet, SUM(t.lasna) AS lasna, SUM(t.poissa) AS poissa, SUM(t.ilm_yht) AS ilm_yht,
    SUM(t.maksuvelvollisia) AS maksuvelvollisia, MIN(h.valintaperusteiden_aloituspaikat) AS valinnan_aloituspaikat, MIN(h.hakukohteen_aloituspaikat) AS aloituspaikat,
    SUM(t.toive_1) AS toive1, SUM(t.toive_2) AS toive2, SUM(t.toive_3) AS toive3, SUM(t.toive_4) AS toive4, SUM(t.toive_5) AS toive5, SUM(t.toive_6) AS toive6
    FROM pub.pub_fct_raportti_tilastoraportti_kk t
    JOIN pub.pub_dim_hakukohde h
    ON t.hakukohde_oid = h.hakukohde_oid
    JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hkr_hk
    ON h.hakukohde_oid = hkr_hk.hakukohde_oid
    WHERE #$filters
    GROUP BY h.hakukohde_nimi, h.organisaatio_nimi""".as[KkHakeneetHyvaksytytVastaanottaneetHakukohteittain]
  }

  def selectHakijatYhteensaWithParams(
                                       selectedKayttooikeusOrganisaatiot: List[String],
                                       haut: List[String],
                                       hakukohteet: List[String],
                                       hakukohderyhmat: List[String],
                                       okmOhjauksenAlat: List[String],
                                       tutkinnonTasot: List[String],
                                       aidinkielet: List[String],
                                       kansalaisuudet: List[String],
                                       sukupuoli: Option[String],
                                       ensikertalainen: Option[Boolean],
                                     ): DBIO[Int] = {

    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, hakukohderyhmat, okmOhjauksenAlat, tutkinnonTasot, aidinkielet, kansalaisuudet, sukupuoli, ensikertalainen
    )

    sql"""SELECT count(distinct ht.henkilo_oid)
    FROM pub.pub_fct_raportti_tilastoraportti_kk t
    JOIN pub.pub_dim_hakutoive ht
    ON t.hakukohde_oid = ht.hakukohde_oid
    JOIN pub.pub_dim_hakukohde h
    ON t.hakukohde_oid = h.hakukohde_oid
    JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hkr_hk
    ON h.hakukohde_oid = hkr_hk.hakukohde_oid
    WHERE #$filters
    """.as[Int].head
  }

  def selectEnsikertalaisetHakijatYhteensaWithParams(
                                                      selectedKayttooikeusOrganisaatiot: List[String],
                                                      haut: List[String],
                                                      hakukohteet: List[String],
                                                      hakukohderyhmat: List[String],
                                                      okmOhjauksenAlat: List[String],
                                                      tutkinnonTasot: List[String],
                                                      aidinkielet: List[String],
                                                      kansalaisuudet: List[String],
                                                      sukupuoli: Option[String],
                                                    ): DBIO[Int] = {

    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, hakukohderyhmat, okmOhjauksenAlat, tutkinnonTasot, aidinkielet, kansalaisuudet, sukupuoli, ensikertalainen = Some(true)
    )

    sql"""SELECT count(distinct ht.henkilo_oid)
      FROM pub.pub_fct_raportti_tilastoraportti_kk t
      JOIN pub.pub_dim_hakutoive ht
      ON t.hakukohde_oid = ht.hakukohde_oid
      JOIN pub.pub_dim_hakukohde h
      ON t.hakukohde_oid = h.hakukohde_oid
      JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hkr_hk
      ON h.hakukohde_oid = hkr_hk.hakukohde_oid
      WHERE #$filters
      """.as[Int].head
  }

}

