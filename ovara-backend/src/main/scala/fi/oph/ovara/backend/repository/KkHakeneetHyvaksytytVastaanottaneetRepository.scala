package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.{
  KkHakeneetHyvaksytytVastaanottaneetHakukohteittain,
  KkHakeneetHyvaksytytVastaanottaneetHauittainTunnisteella,
  KkHakeneetHyvaksytytVastaanottaneetResult,
  KkHakeneetHyvaksytytVastaanottaneetToimipisteittain,
  KkHakeneetHyvaksytytVastaanottaneetTunnisteella
}
import fi.oph.ovara.backend.utils.RepositoryUtils
import fi.oph.ovara.backend.utils.RepositoryUtils.buildTutkinnonTasoFilters
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
      ensikertalainen: Option[Boolean]
  ): String = {

    val hakukohderyhmaFilter =
      if (hakukohderyhmat.nonEmpty) {
          s"AND (t.hakukohde_oid IN (SELECT DISTINCT hakukohde_oid FROM pub.pub_dim_hakukohderyhma_ja_hakukohteet WHERE hakukohderyhma_oid IN (${RepositoryUtils
            .makeListOfValuesQueryStr(hakukohderyhmat)})))"
      } else
        ""
    val filters = Seq(
      s"h.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})",
        RepositoryUtils.makeOptionalListOfValuesQueryStr(
          "AND",
          "h.jarjestyspaikka_oid",
          selectedKayttooikeusOrganisaatiot
        ),
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.hakukohde_oid", hakukohteet),
      hakukohderyhmaFilter,
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.okm_ohjauksen_ala", okmOhjauksenAlat),
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.aidinkieli", aidinkielet),
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.kansalaisuusluokka", kansalaisuudet),
      RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "t.sukupuoli", sukupuoli),
      RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "t.ensikertalainen", ensikertalainen),
      buildTutkinnonTasoFilters(tutkinnonTasot, "h")
    ).collect { case value if value.nonEmpty => value }.mkString("\n")

    filters
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
      ensikertalainen: Option[Boolean]
  ): SqlStreamingAction[Vector[
    KkHakeneetHyvaksytytVastaanottaneetHakukohteittain
  ], KkHakeneetHyvaksytytVastaanottaneetHakukohteittain, Effect] = {

    val filters = buildFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      hakukohteet,
      hakukohderyhmat,
      okmOhjauksenAlat,
      tutkinnonTasot,
      aidinkielet,
      kansalaisuudet,
      sukupuoli,
      ensikertalainen
    )

    val query =
      sql"""SELECT
            h.hakukohde_oid,
            h.hakukohde_nimi,
            h.haku_oid,
            ha.haku_nimi,
            h.organisaatio_nimi,
            COUNT(t.hakutoive_id) AS hakijat,
            COUNT(t.hakutoive_id) filter (WHERE ensisijainen) AS ensisijaisia,
            COUNT(t.hakutoive_id) filter (WHERE ensikertalainen) AS ensikertalaisia,
            COUNT(t.hakutoive_id) filter (WHERE hyvaksytty) AS hyvaksytyt,
            COUNT(t.hakutoive_id) filter (WHERE vastaanottanut) AS vastaanottaneet,
            COUNT(t.hakutoive_id) filter (WHERE lasna) AS lasna,
            COUNT(t.hakutoive_id) filter (WHERE poissa) AS poissa,
            COUNT(t.hakutoive_id) filter (WHERE ilmoittautunut) AS ilm_yht,
            COUNT(t.hakutoive_id) filter (WHERE maksuvelvollinen) AS maksuvelvollisia,
            MIN(h.valintaperusteiden_aloituspaikat) AS valinnan_aloituspaikat,
            MIN(h.hakukohteen_aloituspaikat) AS aloituspaikat,
            COUNT(t.hakutoive_id) filter (WHERE toive_1) AS toive_1,
            COUNT(t.hakutoive_id) filter (WHERE toive_2) AS toive_2,
            COUNT(t.hakutoive_id) filter (WHERE toive_3) AS toive_3,
            COUNT(t.hakutoive_id) filter (WHERE toive_4) AS toive_4,
            COUNT(t.hakutoive_id) filter (WHERE toive_5) AS toive_5,
            COUNT(t.hakutoive_id) filter (WHERE toive_6) AS toive_6
      FROM pub.pub_fct_raportti_tilastoraportti_kk_hakutoive t
      JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
      JOIN pub.pub_dim_haku ha ON h.haku_oid = ha.haku_oid
      WHERE #$filters
      GROUP BY h.hakukohde_oid, h.hakukohde_nimi, h.haku_oid, ha.haku_nimi, h.organisaatio_nimi"""
        .as[KkHakeneetHyvaksytytVastaanottaneetHakukohteittain]

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
      ensikertalainen: Option[Boolean]
  ): SqlStreamingAction[Vector[
    KkHakeneetHyvaksytytVastaanottaneetHauittainTunnisteella
  ], KkHakeneetHyvaksytytVastaanottaneetHauittainTunnisteella, Effect] = {

    val filters = buildFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      hakukohteet,
      hakukohderyhmat,
      okmOhjauksenAlat,
      tutkinnonTasot,
      aidinkielet,
      kansalaisuudet,
      sukupuoli,
      ensikertalainen
    )
    val hakukohdeHakuFilter = s"h.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})"
    val hakukohdeFilter     = RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.hakukohde_oid", hakukohteet)
    val hakukohdeOrganisaatioFilter = RepositoryUtils.makeOptionalListOfValuesQueryStr(
      "AND",
      "h.jarjestyspaikka_oid",
      selectedKayttooikeusOrganisaatiot
    )
    val hakukohdeOkmOhjauksenalaFilter =
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.okm_ohjauksen_ala", okmOhjauksenAlat)
    val hakukohdeTutkinnontasoFilter = buildTutkinnonTasoFilters(tutkinnonTasot, "h")

    val query =
      sql"""SELECT
        ha.haku_oid,
        ha.haku_nimi,
        h.organisaatio_nimi,
        COUNT(DISTINCT t.henkilo_oid) AS hakijat,
        COUNT(DISTINCT t.henkilo_oid) filter (WHERE ensisijainen) AS ensisijaisia,
        COUNT(DISTINCT t.henkilo_oid) filter (WHERE ensikertalainen) AS ensikertalaisia,
        COUNT(DISTINCT t.henkilo_oid) filter (WHERE hyvaksytty) AS hyvaksytyt,
        COUNT(DISTINCT t.henkilo_oid) filter (WHERE vastaanottanut) AS vastaanottaneet,
        COUNT(DISTINCT t.henkilo_oid) filter (WHERE lasna) AS lasna,
        COUNT(DISTINCT t.henkilo_oid) filter (WHERE poissa) AS poissa,
        COUNT(DISTINCT t.henkilo_oid) filter (WHERE ilmoittautunut) AS ilm_yht,
        COUNT(DISTINCT t.henkilo_oid) filter (WHERE maksuvelvollinen) AS maksuvelvollisia,
        a.valinnan_aloituspaikat,
        a.aloituspaikat,
        COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_1) AS toive_1,
        COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_2) AS toive_2,
        COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_3) AS toive_3,
        COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_4) AS toive_4,
        COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_5) AS toive_5,
        COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_6) AS toive_6
        FROM pub.pub_fct_raportti_tilastoraportti_kk_hakutoive t
        JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
        JOIN pub.pub_dim_haku ha ON h.haku_oid = ha.haku_oid
        JOIN (
	      SELECT
		    h.haku_oid,
            SUM(h.valintaperusteiden_aloituspaikat) as valinnan_aloituspaikat,
		    SUM(h.hakukohteen_aloituspaikat) as aloituspaikat
	      FROM pub.pub_dim_hakukohde h
	    WHERE #$hakukohdeHakuFilter
        #$hakukohdeFilter
        #$hakukohdeOrganisaatioFilter
        #$hakukohdeOkmOhjauksenalaFilter
        #$hakukohdeTutkinnontasoFilter
	    group by 1) a on h.haku_oid = a.haku_oid
    WHERE #$filters
    GROUP BY 1, 2, 3, 13, 14""".as[KkHakeneetHyvaksytytVastaanottaneetHauittainTunnisteella]

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
      ensikertalainen: Option[Boolean]
  ): SqlStreamingAction[Vector[
    KkHakeneetHyvaksytytVastaanottaneetToimipisteittain
  ], KkHakeneetHyvaksytytVastaanottaneetToimipisteittain, Effect] = {

    val filters = buildFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      hakukohteet,
      hakukohderyhmat,
      okmOhjauksenAlat,
      tutkinnonTasot,
      aidinkielet,
      kansalaisuudet,
      sukupuoli,
      ensikertalainen
    )

    val hakukohdeHakuFilter = s"h.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})"
    val hakukohdeFilter     = RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.hakukohde_oid", hakukohteet)
    val hakukohdeOrganisaatioFilter = RepositoryUtils.makeOptionalListOfValuesQueryStr(
      "AND",
      "h.jarjestyspaikka_oid",
      selectedKayttooikeusOrganisaatiot
    )
    val hakukohdeOkmOhjauksenalaFilter =
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.okm_ohjauksen_ala", okmOhjauksenAlat)
    val hakukohdeTutkinnontasoFilter = buildTutkinnonTasoFilters(tutkinnonTasot, "h")

    val query =
      sql"""SELECT
      h.toimipiste,
      h.organisaatio_nimi,
      COUNT(DISTINCT t.henkilo_oid) AS hakijat,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE ensisijainen) AS ensisijaisia,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE ensikertalainen) AS ensikertalaisia,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE hyvaksytty) AS hyvaksytyt,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE vastaanottanut) AS vastaanottaneet,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE lasna) AS lasna,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE poissa) AS poissa,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE ilmoittautunut) AS ilm_yht,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE maksuvelvollinen) AS maksuvelvollisia,
      a.valinnan_aloituspaikat,
      a.aloituspaikat,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_1) AS toive_1,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_2) AS toive_2,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_3) AS toive_3,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_4) AS toive_4,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_5) AS toive_5,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_6) AS toive_6
      FROM pub.pub_fct_raportti_tilastoraportti_kk_hakutoive t
      JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
      JOIN (
	    SELECT
		  h.toimipiste,
          SUM(h.valintaperusteiden_aloituspaikat) as valinnan_aloituspaikat,
		  SUM(h.hakukohteen_aloituspaikat) as aloituspaikat
	    FROM pub.pub_dim_hakukohde h
	    WHERE #$hakukohdeHakuFilter
        #$hakukohdeFilter
        #$hakukohdeOrganisaatioFilter
        #$hakukohdeOkmOhjauksenalaFilter
        #$hakukohdeTutkinnontasoFilter
	    GROUP BY  1) a ON h.toimipiste = a.toimipiste
      WHERE #$filters
      GROUP BY 1, 2, 12, 13""".as[KkHakeneetHyvaksytytVastaanottaneetToimipisteittain]
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
      organisaatiotaso: String
  ): SqlStreamingAction[Vector[
    KkHakeneetHyvaksytytVastaanottaneetTunnisteella
  ], KkHakeneetHyvaksytytVastaanottaneetTunnisteella, Effect] = {

    val filters = buildFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      hakukohteet,
      hakukohderyhmat,
      okmOhjauksenAlat,
      tutkinnonTasot,
      aidinkielet,
      kansalaisuudet,
      sukupuoli,
      ensikertalainen
    )

    val hakukohdeHakuFilter = s"h.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})"
    val hakukohdeFilter     = RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.hakukohde_oid", hakukohteet)
    val hakukohdeOrganisaatioFilter = RepositoryUtils.makeOptionalListOfValuesQueryStr(
      "AND",
      "h.jarjestyspaikka_oid",
      selectedKayttooikeusOrganisaatiot
    )
    val hakukohdeOkmOhjauksenalaFilter =
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.okm_ohjauksen_ala", okmOhjauksenAlat)
    val hakukohdeTutkinnontasoFilter = buildTutkinnonTasoFilters(tutkinnonTasot, "h")

    val organisaatioSelect = organisaatiotaso match {
      case "oppilaitoksittain" => "h.oppilaitos as tunniste, h.oppilaitos_nimi as otsikko"
      case _                   => "h.koulutustoimija as tunniste, h.koulutustoimija_nimi as otsikko"
    }
    val organisaatioJoin = organisaatiotaso match {
      case "oppilaitoksittain" => "h.oppilaitos = a.oppilaitos"
      case _                   => "h.koulutustoimija = a.koulutustoimija"
    }
    val organisaatio = organisaatiotaso match {
      case "oppilaitoksittain" => "h.oppilaitos"
      case _                   => "h.koulutustoimija"
    }
    val query =
      sql"""SELECT
      #$organisaatioSelect,
      COUNT(DISTINCT t.henkilo_oid) AS hakijat,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE ensisijainen) AS ensisijaisia,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE ensikertalainen) AS ensikertalaisia,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE hyvaksytty) AS hyvaksytyt,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE vastaanottanut) AS vastaanottaneet,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE lasna) AS lasna,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE poissa) AS poissa,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE ilmoittautunut) AS ilm_yht,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE maksuvelvollinen) AS maksuvelvollisia,
      a.valinnan_aloituspaikat,
      a.aloituspaikat,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_1) AS toive_1,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_2) AS toive_2,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_3) AS toive_3,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_4) AS toive_4,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_5) AS toive_5,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_6) AS toive_6
      FROM pub.pub_fct_raportti_tilastoraportti_kk_hakutoive t
      JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
      JOIN (
	    SELECT
		  #$organisaatio,
          SUM(h.valintaperusteiden_aloituspaikat) as valinnan_aloituspaikat,
		  SUM(h.hakukohteen_aloituspaikat) as aloituspaikat
	    FROM pub.pub_dim_hakukohde h
	    WHERE #$hakukohdeHakuFilter
        #$hakukohdeFilter
        #$hakukohdeOrganisaatioFilter
        #$hakukohdeOkmOhjauksenalaFilter
        #$hakukohdeTutkinnontasoFilter
	    GROUP BY  1) a ON #$organisaatioJoin
      WHERE #$filters
      GROUP BY 1, 2, 12, 13""".as[KkHakeneetHyvaksytytVastaanottaneetTunnisteella]

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
      ensikertalainen: Option[Boolean]
  ): SqlStreamingAction[Vector[
    KkHakeneetHyvaksytytVastaanottaneetTunnisteella
  ], KkHakeneetHyvaksytytVastaanottaneetTunnisteella, Effect] = {

    val filters = buildFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      hakukohteet,
      hakukohderyhmat,
      okmOhjauksenAlat,
      tutkinnonTasot,
      aidinkielet,
      kansalaisuudet,
      sukupuoli,
      ensikertalainen
    )

    val hakukohdeHakuFilter = s"h.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})"
    val hakukohdeFilter     = RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.hakukohde_oid", hakukohteet)
    val hakukohdeOrganisaatioFilter = RepositoryUtils.makeOptionalListOfValuesQueryStr(
      "AND",
      "h.jarjestyspaikka_oid",
      selectedKayttooikeusOrganisaatiot
    )
    val hakukohdeOkmOhjauksenalaFilter =
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.okm_ohjauksen_ala", okmOhjauksenAlat)
    val hakukohdeTutkinnontasoFilter = buildTutkinnonTasoFilters(tutkinnonTasot, "h")

    val query =
      sql"""SELECT
      h.okm_ohjauksen_ala AS tunniste,
      o.koodinimi AS otsikko,
      COUNT(DISTINCT t.henkilo_oid) AS hakijat,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE ensisijainen) AS ensisijaisia,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE ensikertalainen) AS ensikertalaisia,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE hyvaksytty) AS hyvaksytyt,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE vastaanottanut) AS vastaanottaneet,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE lasna) AS lasna,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE poissa) AS poissa,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE ilmoittautunut) AS ilm_yht,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE maksuvelvollinen) AS maksuvelvollisia,
      a.valinnan_aloituspaikat,
      a.aloituspaikat,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_1) AS toive_1,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_2) AS toive_2,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_3) AS toive_3,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_4) AS toive_4,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_5) AS toive_5,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_6) AS toive_6
      FROM pub.pub_fct_raportti_tilastoraportti_kk_hakutoive t
      JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
      JOIN pub.pub_dim_koodisto_okmohjauksenala o ON h.okm_ohjauksen_ala = o.koodiarvo
      JOIN (
	    SELECT
		  h.okm_ohjauksen_ala,
          SUM(h.valintaperusteiden_aloituspaikat) as valinnan_aloituspaikat,
		  SUM(h.hakukohteen_aloituspaikat) as aloituspaikat
	    FROM pub.pub_dim_hakukohde h
	    WHERE #$hakukohdeHakuFilter
        #$hakukohdeFilter
        #$hakukohdeOrganisaatioFilter
        #$hakukohdeOkmOhjauksenalaFilter
        #$hakukohdeTutkinnontasoFilter
	    GROUP BY  1) a ON h.okm_ohjauksen_ala = a.okm_ohjauksen_ala
      WHERE #$filters
      GROUP BY 1, 2, 12, 13""".as[KkHakeneetHyvaksytytVastaanottaneetTunnisteella]

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
      ensikertalainen: Option[Boolean]
  ): SqlStreamingAction[Vector[
    KkHakeneetHyvaksytytVastaanottaneetResult
  ], KkHakeneetHyvaksytytVastaanottaneetResult, Effect] = {

    val filters = buildFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      hakukohteet,
      hakukohderyhmat,
      okmOhjauksenAlat,
      tutkinnonTasot,
      aidinkielet,
      kansalaisuudet,
      sukupuoli,
      ensikertalainen
    )
    // kansalaisuuksittain tulostaessa Exceliin ei tule aloituspaikkasarakkeita
    val query =
      sql"""SELECT
      m.koodinimi AS otsikko,
      COUNT(DISTINCT t.henkilo_oid) AS hakijat,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE ensisijainen) AS ensisijaisia,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE ensikertalainen) AS ensikertalaisia,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE hyvaksytty) AS hyvaksytyt,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE vastaanottanut) AS vastaanottaneet,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE lasna) AS lasna,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE poissa) AS poissa,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE ilmoittautunut) AS ilm_yht,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE maksuvelvollinen) AS maksuvelvollisia,
      NULL,
      NULL,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_1) AS toive_1,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_2) AS toive_2,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_3) AS toive_3,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_4) AS toive_4,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_5) AS toive_5,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_6) AS toive_6
      FROM pub.pub_fct_raportti_tilastoraportti_kk_hakutoive t
      JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
      JOIN pub.pub_dim_koodisto_maa_2 m ON t.kansalaisuus = m.koodiarvo
      WHERE #$filters
      GROUP BY 1, 11, 12""".as[KkHakeneetHyvaksytytVastaanottaneetResult]

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
      ensikertalainen: Option[Boolean]
  ): SqlStreamingAction[Vector[
    KkHakeneetHyvaksytytVastaanottaneetTunnisteella
  ], KkHakeneetHyvaksytytVastaanottaneetTunnisteella, Effect] = {

    val filters = buildFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      hakukohteet,
      hakukohderyhmat,
      okmOhjauksenAlat,
      tutkinnonTasot,
      aidinkielet,
      kansalaisuudet,
      sukupuoli,
      ensikertalainen
    )
    val hakukohdeHakuFilter = s"h.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})"
    val hakukohdeFilter     = RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.hakukohde_oid", hakukohteet)
    val hakukohdeOrganisaatioFilter = RepositoryUtils.makeOptionalListOfValuesQueryStr(
      "AND",
      "h.jarjestyspaikka_oid",
      selectedKayttooikeusOrganisaatiot
    )
    val hakukohdeOkmOhjauksenalaFilter =
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.okm_ohjauksen_ala", okmOhjauksenAlat)
    val hakukohdeTutkinnontasoFilter = buildTutkinnonTasoFilters(tutkinnonTasot, "h")

    val query =
      sql"""SELECT
      hr.hakukohderyhma_oid AS tunniste,
      hr.hakukohderyhma_nimi AS otsikko,
      COUNT(DISTINCT t.henkilo_oid) AS hakijat,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE ensisijainen) AS ensisijaisia,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE ensikertalainen) AS ensikertalaisia,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE hyvaksytty) AS hyvaksytyt,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE vastaanottanut) AS vastaanottaneet,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE lasna) AS lasna,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE poissa) AS poissa,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE ilmoittautunut) AS ilm_yht,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE maksuvelvollinen) AS maksuvelvollisia,
      a.valinnan_aloituspaikat,
      a.aloituspaikat,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_1) AS toive_1,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_2) AS toive_2,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_3) AS toive_3,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_4) AS toive_4,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_5) AS toive_5,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_6) AS toive_6
      FROM pub.pub_fct_raportti_tilastoraportti_kk_hakutoive t
      JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
      JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hh ON h.hakukohde_oid = hh.hakukohde_oid
      JOIN pub.pub_dim_hakukohderyhma hr ON hh.hakukohderyhma_oid = hr.hakukohderyhma_oid
      JOIN (
	    SELECT
		  hh.hakukohderyhma_oid,
          SUM(h.valintaperusteiden_aloituspaikat) as valinnan_aloituspaikat,
		  SUM(h.hakukohteen_aloituspaikat) as aloituspaikat
	    FROM pub.pub_dim_hakukohde h
        JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hh ON h.hakukohde_oid = hh.hakukohde_oid
	    WHERE #$hakukohdeHakuFilter
        #$hakukohdeFilter
        #$hakukohdeOrganisaatioFilter
        #$hakukohdeOkmOhjauksenalaFilter
        #$hakukohdeTutkinnontasoFilter
	    GROUP BY  1) a ON hr.hakukohderyhma_oid = a.hakukohderyhma_oid
      WHERE #$filters
      GROUP BY 1, 2, 12, 13""".as[KkHakeneetHyvaksytytVastaanottaneetTunnisteella]

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
      maksuvelvollinen: Option[String] = None
  ): DBIO[Int] = {
    val hakukohderyhmaFilter =
      if (hakukohderyhmat.nonEmpty) {
          s"AND (ht.hakukohde_oid IN (SELECT DISTINCT hakukohde_oid FROM pub.pub_dim_hakukohderyhma_ja_hakukohteet WHERE hakukohderyhma_oid IN (${RepositoryUtils
            .makeListOfValuesQueryStr(hakukohderyhmat)})))"
      } else
        ""
    val filters = Seq(
      s"ht.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})",
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.jarjestyspaikka_oid", selectedKayttooikeusOrganisaatiot),
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "ht.hakukohde_oid", hakukohteet),
      hakukohderyhmaFilter,
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.okm_ohjauksen_ala", okmOhjauksenAlat),
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "he.aidinkieli", aidinkielet),
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "he.kansalaisuusluokka", kansalaisuudet),
      RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "he.sukupuoli", sukupuoli),
      RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "ht.ensikertalainen", ensikertalainen),
      RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "m.maksuvelvollisuus", maksuvelvollinen),
      buildTutkinnonTasoFilters(tutkinnonTasot, "h")
    ).collect { case value if value.nonEmpty => value }.mkString("\n")

    val maksuvelvollisuusJoin =
      if (maksuvelvollinen.isDefined) {
        s"JOIN pub.pub_dim_maksuvelvollisuus m ON ht.hakutoive_id = m.hakutoive_id"
      } else
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
