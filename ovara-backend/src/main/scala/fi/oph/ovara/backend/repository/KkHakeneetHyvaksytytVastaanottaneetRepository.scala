package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.{KkHakeneetHyvaksytytVastaanottaneetOrganisaatioNimella, KkHakeneetHyvaksytytVastaanottaneetResult, KkHakeneetHyvaksytytVastaanottaneetToimipisteittain}
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Component
import slick.dbio.{DBIO, Effect}
import slick.sql.SqlStreamingAction
import slick.jdbc.PostgresProfile.api.*

@Component
class KkHakeneetHyvaksytytVastaanottaneetRepository extends Extractors {

  val LOG: Logger = LoggerFactory.getLogger(classOf[KkHakeneetHyvaksytytVastaanottaneetRepository]);
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

    val hakukohderyhmaFilter =
      if (hakukohderyhmat.nonEmpty) {
        Some(s"AND (t.hakukohde_oid IN (SELECT DISTINCT hakukohde_oid FROM pub.pub_dim_hakukohderyhma_ja_hakukohteet WHERE hakukohderyhma_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(hakukohderyhmat)})))")
      }
      else
        None
    val filters = Seq(
      Some(s"h.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})"),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.jarjestyspaikka_oid", selectedKayttooikeusOrganisaatiot)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.hakukohde_oid", hakukohteet)).filter(_.nonEmpty),
      hakukohderyhmaFilter,
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.okm_ohjauksen_ala", okmOhjauksenAlat)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.aidinkieli", aidinkielet)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.kansalaisuusluokka", kansalaisuudet)).filter(_.nonEmpty),
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
                                     ): SqlStreamingAction[Vector[KkHakeneetHyvaksytytVastaanottaneetOrganisaatioNimella], KkHakeneetHyvaksytytVastaanottaneetOrganisaatioNimella, Effect] = {

    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, hakukohderyhmat, okmOhjauksenAlat, tutkinnonTasot, aidinkielet, kansalaisuudet, sukupuoli, ensikertalainen
    )

    val query =
      sql"""SELECT
            h.hakukohde_nimi AS otsikko,
            h.organisaatio_nimi,
            SUM(t.hakijat) AS hakijat,
            SUM(t.ensisijaisia) AS ensisijaisia,
            SUM(t.ensikertalaisia) AS ensikertalaisia,
            SUM(t.hyvaksytyt) AS hyvaksytyt,
            SUM(t.vastaanottaneet) AS vastaanottaneet,
            SUM(t.lasna) AS lasna,
            SUM(t.poissa) AS poissa,
            SUM(t.ilm_yht) AS ilm_yht,
            SUM(t.maksuvelvollisia) AS maksuvelvollisia,
            MIN(h.valintaperusteiden_aloituspaikat) AS valinnan_aloituspaikat,
            MIN(h.hakukohteen_aloituspaikat) AS aloituspaikat,
            SUM(t.toive_1) AS toive1,
            SUM(t.toive_2) AS toive2,
            SUM(t.toive_3) AS toive3,
            SUM(t.toive_4) AS toive4,
            SUM(t.toive_5) AS toive5,
            SUM(t.toive_6) AS toive6
      FROM pub.pub_fct_raportti_tilastoraportti_kk t
      JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
      WHERE #$filters
      GROUP BY h.hakukohde_nimi, h.organisaatio_nimi""".as[KkHakeneetHyvaksytytVastaanottaneetOrganisaatioNimella]

    LOG.debug(s"selectHakukohteittainWithParams: ${query.statements.head}")
    query
  }

  def selectHauittainWithParams(
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
                                     ): SqlStreamingAction[Vector[KkHakeneetHyvaksytytVastaanottaneetOrganisaatioNimella], KkHakeneetHyvaksytytVastaanottaneetOrganisaatioNimella, Effect] = {

    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, hakukohderyhmat, okmOhjauksenAlat, tutkinnonTasot, aidinkielet, kansalaisuudet, sukupuoli, ensikertalainen
    )

    val query =
      sql"""SELECT
        ha.haku_nimi,
        b.organisaatio_nimi,
	      SUM(a.hakijat) as hakijat,
	      SUM(a.ensisijaisia) AS ensisijaisia,
        SUM(a.ensikertalaisia) AS ensikertalaisia,
        SUM(a.hyvaksytyt) AS hyvaksytyt,
        SUM(a.vastaanottaneet) AS vastaanottaneet,
        SUM(a.lasna) AS lasna,
        SUM(a.poissa) AS poissa,
        SUM(a.ilm_yht) AS ilm_yht,
        SUM(a.maksuvelvollisia) AS maksuvelvollisia,
        SUM(b.valintaperusteiden_aloituspaikat) AS valinnan_aloituspaikat,
        SUM(b.hakukohteen_aloituspaikat) AS aloituspaikat,
        SUM(a.toive1) AS toive1,
        SUM(a.toive2) AS toive2,
        SUM(a.toive3) AS toive3,
        SUM(a.toive4) AS toive4,
        SUM(a.toive5) AS toive5,
        SUM(a.toive6) AS toive6
	    FROM (
		    SELECT
      		h.hakukohde_oid,
          SUM(t.hakijat) AS hakijat,
          SUM(t.ensisijaisia) AS ensisijaisia,
          SUM(t.ensikertalaisia) AS ensikertalaisia,
          SUM(t.hyvaksytyt) AS hyvaksytyt,
          SUM(t.vastaanottaneet) AS vastaanottaneet,
          SUM(t.lasna) AS lasna,
          SUM(t.poissa) AS poissa,
          SUM(t.ilm_yht) AS ilm_yht,
          SUM(t.maksuvelvollisia) AS maksuvelvollisia,
          SUM(t.toive_1) AS toive1,
          SUM(t.toive_2) AS toive2,
          SUM(t.toive_3) AS toive3,
          SUM(t.toive_4) AS toive4,
          SUM(t.toive_5) AS toive5,
          SUM(t.toive_6) AS toive6
	      FROM pub.pub_fct_raportti_tilastoraportti_kk t
	      JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
	      WHERE #$filters
        GROUP BY h.hakukohde_oid
	    ) a
      JOIN pub.pub_dim_hakukohde b on a.hakukohde_oid = b.hakukohde_oid
      JOIN pub.pub_dim_haku ha ON b.haku_oid = ha.haku_oid
      GROUP BY 1,2""".as[KkHakeneetHyvaksytytVastaanottaneetOrganisaatioNimella]

    LOG.debug(s"selectHauittainWithParams: ${query.statements.head}")
    query
  }

  def selectToimipisteittainWithParams(
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
                                     ): SqlStreamingAction[Vector[KkHakeneetHyvaksytytVastaanottaneetToimipisteittain], KkHakeneetHyvaksytytVastaanottaneetToimipisteittain, Effect] = {

    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, hakukohderyhmat, okmOhjauksenAlat, tutkinnonTasot, aidinkielet, kansalaisuudet, sukupuoli, ensikertalainen
    )

    val query =
      sql"""SELECT
	    b.toimipiste,
	    b.organisaatio_nimi,
	    SUM(a.hakijat) as hakijat,
	    SUM(a.ensisijaisia) AS ensisijaisia,
      SUM(a.ensikertalaisia) AS ensikertalaisia,
      SUM(a.hyvaksytyt) AS hyvaksytyt,
      SUM(a.vastaanottaneet) AS vastaanottaneet,
      SUM(a.lasna) AS lasna,
      SUM(a.poissa) AS poissa,
      SUM(a.ilm_yht) AS ilm_yht,
      SUM(a.maksuvelvollisia) AS maksuvelvollisia,
      SUM(b.valintaperusteiden_aloituspaikat) AS valinnan_aloituspaikat,
      SUM(b.hakukohteen_aloituspaikat) AS aloituspaikat,
      SUM(a.toive1) AS toive1,
      SUM(a.toive2) AS toive2,
      SUM(a.toive3) AS toive3,
      SUM(a.toive4) AS toive4,
      SUM(a.toive5) AS toive5,
      SUM(a.toive6) AS toive6
	    FROM (
		    SELECT
      		h.hakukohde_oid,
          SUM(t.hakijat) AS hakijat,
          SUM(t.ensisijaisia) AS ensisijaisia,
          SUM(t.ensikertalaisia) AS ensikertalaisia,
          SUM(t.hyvaksytyt) AS hyvaksytyt,
          SUM(t.vastaanottaneet) AS vastaanottaneet,
          SUM(t.lasna) AS lasna,
          SUM(t.poissa) AS poissa,
          SUM(t.ilm_yht) AS ilm_yht,
          SUM(t.maksuvelvollisia) AS maksuvelvollisia,
          SUM(t.toive_1) AS toive1,
          SUM(t.toive_2) AS toive2,
          SUM(t.toive_3) AS toive3,
          SUM(t.toive_4) AS toive4,
          SUM(t.toive_5) AS toive5,
          SUM(t.toive_6) AS toive6
	      FROM pub.pub_fct_raportti_tilastoraportti_kk t
	      JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
	      WHERE #$filters
        GROUP BY h.hakukohde_oid
	    ) a
      JOIN pub.pub_dim_hakukohde b ON a.hakukohde_oid = b.hakukohde_oid
      GROUP BY 1,2""".as[KkHakeneetHyvaksytytVastaanottaneetToimipisteittain]

    LOG.debug(s"selectToimipisteittainWithParams: ${query.statements.head}")
    query
  }

  def selectOrganisaatioittainWithParams(
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
                                       organisaatiotaso: String,
                                     ): SqlStreamingAction[Vector[KkHakeneetHyvaksytytVastaanottaneetResult], KkHakeneetHyvaksytytVastaanottaneetResult, Effect] = {

    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, hakukohderyhmat, okmOhjauksenAlat, tutkinnonTasot, aidinkielet, kansalaisuudet, sukupuoli, ensikertalainen
    )

    val organisaatioSelect = organisaatiotaso match {
      case "oppilaitoksittain" => "b.oppilaitos_nimi as otsikko"
      case _ => "b.koulutustoimija_nimi as otsikko"
    }
    val query =
      sql"""SELECT
        #$organisaatioSelect,
	      SUM(a.hakijat) as hakijat,
	      SUM(a.ensisijaisia) AS ensisijaisia,
        SUM(a.ensikertalaisia) AS ensikertalaisia,
        SUM(a.hyvaksytyt) AS hyvaksytyt,
        SUM(a.vastaanottaneet) AS vastaanottaneet,
        SUM(a.lasna) AS lasna,
        SUM(a.poissa) AS poissa,
        SUM(a.ilm_yht) AS ilm_yht,
        SUM(a.maksuvelvollisia) AS maksuvelvollisia,
        SUM(b.valintaperusteiden_aloituspaikat) AS valinnan_aloituspaikat,
        SUM(b.hakukohteen_aloituspaikat) AS aloituspaikat,
        SUM(a.toive1) AS toive1,
        SUM(a.toive2) AS toive2,
        SUM(a.toive3) AS toive3,
        SUM(a.toive4) AS toive4,
        SUM(a.toive5) AS toive5,
        SUM(a.toive6) AS toive6
	    FROM (
		    SELECT
      		h.hakukohde_oid,
            SUM(t.hakijat) AS hakijat,
            SUM(t.ensisijaisia) AS ensisijaisia,
            SUM(t.ensikertalaisia) AS ensikertalaisia,
            SUM(t.hyvaksytyt) AS hyvaksytyt,
            SUM(t.vastaanottaneet) AS vastaanottaneet,
            SUM(t.lasna) AS lasna,
            SUM(t.poissa) AS poissa,
            SUM(t.ilm_yht) AS ilm_yht,
            SUM(t.maksuvelvollisia) AS maksuvelvollisia,
            SUM(t.toive_1) AS toive1,
            SUM(t.toive_2) AS toive2,
            SUM(t.toive_3) AS toive3,
            SUM(t.toive_4) AS toive4,
            SUM(t.toive_5) AS toive5,
            SUM(t.toive_6) AS toive6
	      FROM pub.pub_fct_raportti_tilastoraportti_kk t
	      JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
	      WHERE #$filters
        GROUP BY h.hakukohde_oid
	    ) a
      JOIN pub.pub_dim_hakukohde b on a.hakukohde_oid = b.hakukohde_oid
      GROUP BY 1""".as[KkHakeneetHyvaksytytVastaanottaneetResult]

    LOG.debug(s"selectOrganisaatioittainWithParams: ${query.statements.head}")
    query
  }

  def selectOkmOhjauksenAloittainWithParams(
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
                                      ): SqlStreamingAction[Vector[KkHakeneetHyvaksytytVastaanottaneetResult], KkHakeneetHyvaksytytVastaanottaneetResult, Effect] = {

    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, hakukohderyhmat, okmOhjauksenAlat, tutkinnonTasot, aidinkielet, kansalaisuudet, sukupuoli, ensikertalainen
    )

    val query =
      sql"""SELECT
      o.koodinimi AS otsikko,
      SUM(a.hakijat) AS hakijat,
      SUM(a.ensisijaisia) AS ensisijaisia,
      SUM(a.ensikertalaisia) AS ensikertalaisia,
      SUM(a.hyvaksytyt) AS hyvaksytyt,
      SUM(a.vastaanottaneet) AS vastaanottaneet,
      SUM(a.lasna) AS lasna,
      SUM(a.poissa) AS poissa,
      SUM(a.ilm_yht) AS ilm_yht,
      SUM(a.maksuvelvollisia) AS maksuvelvollisia,
      SUM(b.valintaperusteiden_aloituspaikat) AS valinnan_aloituspaikat,
      SUM(b.hakukohteen_aloituspaikat) AS aloituspaikat,
      SUM(a.toive1) AS toive1,
      SUM(a.toive2) AS toive2,
      SUM(a.toive3) AS toive3,
      SUM(a.toive4) AS toive4,
      SUM(a.toive5) AS toive5,
      SUM(a.toive6) AS toive6
      FROM (
        SELECT
          h.okm_ohjauksen_ala,
          h.hakukohde_oid,
          SUM(t.hakijat) AS hakijat,
          SUM(t.ensisijaisia) AS ensisijaisia,
          SUM(t.ensikertalaisia) AS ensikertalaisia,
          SUM(t.hyvaksytyt) AS hyvaksytyt,
          SUM(t.vastaanottaneet) AS vastaanottaneet,
          SUM(t.lasna) AS lasna,
          SUM(t.poissa) AS poissa,
          SUM(t.ilm_yht) AS ilm_yht,
          SUM(t.maksuvelvollisia) AS maksuvelvollisia,
          SUM(t.toive_1) AS toive1,
          SUM(t.toive_2) AS toive2,
          SUM(t.toive_3) AS toive3,
          SUM(t.toive_4) AS toive4,
          SUM(t.toive_5) AS toive5,
          SUM(t.toive_6) AS toive6
        FROM pub.pub_fct_raportti_tilastoraportti_kk t
        JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
        WHERE #$filters
        GROUP BY h.okm_ohjauksen_ala, h.hakukohde_oid
      ) a
      JOIN pub.pub_dim_hakukohde b ON a.hakukohde_oid = b.hakukohde_oid
      JOIN pub.pub_dim_koodisto_okmohjauksenala o ON a.okm_ohjauksen_ala = o.koodiarvo
      GROUP BY 1""".as[KkHakeneetHyvaksytytVastaanottaneetResult]

    LOG.debug(s"selectOkmOhjauksenAloittainWithParams: ${query.statements.head}")
    query
  }

  def selectKansalaisuuksittainWithParams(
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
                                           ): SqlStreamingAction[Vector[KkHakeneetHyvaksytytVastaanottaneetResult], KkHakeneetHyvaksytytVastaanottaneetResult, Effect] = {

    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, hakukohderyhmat, okmOhjauksenAlat, tutkinnonTasot, aidinkielet, kansalaisuudet, sukupuoli, ensikertalainen
    )

    val query =
      sql"""SELECT
      c.koodinimi AS otsikko,
      SUM(a.hakijat) AS hakijat,
      SUM(a.ensisijaisia) AS ensisijaisia,
      SUM(a.ensikertalaisia) AS ensikertalaisia,
      SUM(a.hyvaksytyt) AS hyvaksytyt,
      SUM(a.vastaanottaneet) AS vastaanottaneet,
      SUM(a.lasna) AS lasna,
      SUM(a.poissa) AS poissa,
      SUM(a.ilm_yht) AS ilm_yht,
      SUM(a.maksuvelvollisia) AS maksuvelvollisia,
      SUM(b.valintaperusteiden_aloituspaikat) AS valinnan_aloituspaikat,
      SUM(b.hakukohteen_aloituspaikat) AS aloituspaikat,
      SUM(a.toive1) AS toive1,
      SUM(a.toive2) AS toive2,
      SUM(a.toive3) AS toive3,
      SUM(a.toive4) AS toive4,
      SUM(a.toive5) AS toive5,
      SUM(a.toive6) AS toive6
      FROM (
        SELECT
          h.hakukohde_oid,
          t.kansalaisuus,
          SUM(t.hakijat) AS hakijat,
          SUM(t.ensisijaisia) AS ensisijaisia,
          SUM(t.ensikertalaisia) AS ensikertalaisia,
          SUM(t.hyvaksytyt) AS hyvaksytyt,
          SUM(t.vastaanottaneet) AS vastaanottaneet,
          SUM(t.lasna) AS lasna,
          SUM(t.poissa) AS poissa,
          SUM(t.ilm_yht) AS ilm_yht,
          SUM(t.maksuvelvollisia) AS maksuvelvollisia,
          SUM(t.toive_1) AS toive1,
          SUM(t.toive_2) AS toive2,
          SUM(t.toive_3) AS toive3,
          SUM(t.toive_4) AS toive4,
          SUM(t.toive_5) AS toive5,
          SUM(t.toive_6) AS toive6
        FROM pub.pub_fct_raportti_tilastoraportti_kk t
        JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
        WHERE #$filters
        GROUP BY h.hakukohde_oid, t.kansalaisuus
      ) a
      JOIN pub.pub_dim_hakukohde b ON a.hakukohde_oid = b.hakukohde_oid
      JOIN pub.pub_dim_koodisto_maa_2 c ON a.kansalaisuus = c.koodiarvo
      GROUP BY 1""".as[KkHakeneetHyvaksytytVastaanottaneetResult]

    LOG.debug(s"selectKansalaisuuksittainWithParams: ${query.statements.head}")
    query
  }

  def selectHakukohderyhmittainWithParams(
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
                                         ): SqlStreamingAction[Vector[KkHakeneetHyvaksytytVastaanottaneetResult], KkHakeneetHyvaksytytVastaanottaneetResult, Effect] = {

    val filters = buildFilters(
      haut, selectedKayttooikeusOrganisaatiot, hakukohteet, hakukohderyhmat, okmOhjauksenAlat, tutkinnonTasot, aidinkielet, kansalaisuudet, sukupuoli, ensikertalainen
    )

    val query =
      sql"""SELECT
      hr.hakukohderyhma_nimi AS otsikko,
      SUM(a.hakijat) AS hakijat,
      SUM(a.ensisijaisia) AS ensisijaisia,
      SUM(a.ensikertalaisia) AS ensikertalaisia,
      SUM(a.hyvaksytyt) AS hyvaksytyt,
      SUM(a.vastaanottaneet) AS vastaanottaneet,
      SUM(a.lasna) AS lasna,
      SUM(a.poissa) AS poissa,
      SUM(a.ilm_yht) AS ilm_yht,
      SUM(a.maksuvelvollisia) AS maksuvelvollisia,
      SUM(b.valintaperusteiden_aloituspaikat) AS valinnan_aloituspaikat,
      SUM(b.hakukohteen_aloituspaikat) AS aloituspaikat,
      SUM(a.toive1) AS toive1,
      SUM(a.toive2) AS toive2,
      SUM(a.toive3) AS toive3,
      SUM(a.toive4) AS toive4,
      SUM(a.toive5) AS toive5,
      SUM(a.toive6) AS toive6
      FROM (
        SELECT
          h.hakukohde_oid,
          SUM(t.hakijat) AS hakijat,
          SUM(t.ensisijaisia) AS ensisijaisia,
          SUM(t.ensikertalaisia) AS ensikertalaisia,
          SUM(t.hyvaksytyt) AS hyvaksytyt,
          SUM(t.vastaanottaneet) AS vastaanottaneet,
          SUM(t.lasna) AS lasna,
          SUM(t.poissa) AS poissa,
          SUM(t.ilm_yht) AS ilm_yht,
          SUM(t.maksuvelvollisia) AS maksuvelvollisia,
          SUM(t.toive_1) AS toive1,
          SUM(t.toive_2) AS toive2,
          SUM(t.toive_3) AS toive3,
          SUM(t.toive_4) AS toive4,
          SUM(t.toive_5) AS toive5,
          SUM(t.toive_6) AS toive6
        FROM pub.pub_fct_raportti_tilastoraportti_kk t
        JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
        WHERE #$filters
        GROUP BY h.hakukohde_oid
      ) a
      JOIN pub.pub_dim_hakukohde b ON a.hakukohde_oid = b.hakukohde_oid
      JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hh ON b.hakukohde_oid = hh.hakukohde_oid
      JOIN pub.pub_dim_hakukohderyhma hr ON hh.hakukohderyhma_oid = hr.hakukohderyhma_oid
      GROUP BY 1""".as[KkHakeneetHyvaksytytVastaanottaneetResult]

    LOG.debug(s"selectHakukohderyhmittainWithParams: ${query.statements.head}")
    query
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
                                       maksuvelvollinen: Option[String] = None,
                                     ): DBIO[Int] = {
    val hakukohderyhmaFilter =
      if (hakukohderyhmat.nonEmpty) {
        Some(s"AND (ht.hakukohde_oid IN (SELECT DISTINCT hakukohde_oid FROM pub.pub_dim_hakukohderyhma_ja_hakukohteet WHERE hakukohderyhma_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(hakukohderyhmat)})))")
      }
      else
        None
    val filters = Seq(
      Some(s"ht.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})"),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.jarjestyspaikka_oid", selectedKayttooikeusOrganisaatiot)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "ht.hakukohde_oid", hakukohteet)).filter(_.nonEmpty),
      hakukohderyhmaFilter,
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.okm_ohjauksen_ala", okmOhjauksenAlat)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "he.aidinkieli", aidinkielet)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "he.kansalaisuusluokka", kansalaisuudet)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "he.sukupuoli", sukupuoli)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "ht.ensikertalainen", ensikertalainen)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "m.maksuvelvollisuus", maksuvelvollinen)).filter(_.nonEmpty),
      buildTutkinnonTasoFilters(tutkinnonTasot)
    ).collect { case Some(value) => value }.mkString("\n")

    val maksuvelvollisuusJoin =
      if (maksuvelvollinen.isDefined) {
       s"JOIN pub.pub_dim_maksuvelvollisuus m ON ht.hakutoive_id = m.hakutoive_id"
      }
      else
        ""
    val query =
      sql"""SELECT count(distinct ht.henkilo_oid)
      FROM pub.pub_dim_hakutoive ht
      JOIN pub.pub_dim_hakukohde h ON ht.hakukohde_oid = h.hakukohde_oid
      JOIN pub.pub_dim_henkilo he on ht.henkilo_hakemus_id = he.henkilo_hakemus_id
      #$maksuvelvollisuusJoin
      WHERE #$filters
      """.as[Int].head

    LOG.debug(s"selectHakijatYhteensaWithParams: ${query.statements.head}")
    query
  }

}

