package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.{KkHakeneetHyvaksytytVastaanottaneetHakukohteittain, KkHakeneetHyvaksytytVastaanottaneetHauittainTunnisteella, KkHakeneetHyvaksytytVastaanottaneetResult, KkHakeneetHyvaksytytVastaanottaneetToimipisteittain, KkHakeneetHyvaksytytVastaanottaneetTunnisteella}
import fi.oph.ovara.backend.utils.{ParametriNimet, RepositoryUtils}
import fi.oph.ovara.backend.utils.RepositoryUtils.{buildTutkinnonTasoFilters, makeHakukohderyhmaQueryWithKayttooikeudet}
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
      isOrganisaatioRajain: Boolean,
      kayttooikeusHakukohderyhmat: List[String],
      hakukohteet: List[String],
      hakukohderyhmat: List[String],
      okmOhjauksenAlat: List[String],
      tutkinnonTasot: List[String],
      aidinkielet: List[String],
      kansalaisuudet: List[String],
      sukupuoli: Option[String],
      ensikertalainen: Option[Boolean]
  ): String = {

    val hakukohteetOrganisaatioJaKayttooikeusrajauksillaFilter: String = buildOrganisaatioKayttooikeusFilter(selectedKayttooikeusOrganisaatiot, isOrganisaatioRajain, kayttooikeusHakukohderyhmat)
    val filters = Seq(
      s"h.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})",
      hakukohteetOrganisaatioJaKayttooikeusrajauksillaFilter,
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.hakukohde_oid", hakukohteet),
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "hh.hakukohderyhma_oid", hakukohderyhmat),
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.okm_ohjauksen_ala", okmOhjauksenAlat),
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.aidinkieli", aidinkielet),
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.kansalaisuusluokka", kansalaisuudet),
      RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "t.sukupuoli", sukupuoli),
      RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "t.ensikertalainen", ensikertalainen),
      buildTutkinnonTasoFilters(tutkinnonTasot, "h")
    ).collect { case value if value.nonEmpty => value }.mkString("\n")

    filters
  }

  private def buildFiltersForMainQuery(
                                        haut: List[String],
                                        aidinkielet: List[String],
                                        kansalaisuudet: List[String],
                                        sukupuoli: Option[String],
                                        ensikertalainen: Option[Boolean]
                                      ): String = {
    Seq(
      s"h.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})",
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.aidinkieli", aidinkielet),
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.kansalaisuusluokka", kansalaisuudet),
      RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "t.sukupuoli", sukupuoli),
      RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "t.ensikertalainen", ensikertalainen),
    ).filter(_.nonEmpty).mkString("\n")
  }
  private def buildHakukohdeFilters(
                                     haut: List[String],
                                     selectedKayttooikeusOrganisaatiot: List[String],
                                     isOrganisaatioRajain: Boolean,
                                     kayttooikeusHakukohderyhmat: List[String],
                                     hakukohteet: List[String],
                                     hakukohderyhmat: List[String],
                                     okmOhjauksenAlat: List[String],
                                     tutkinnonTasot: List[String]
                                   ): String = {
    val hakukohteetOrganisaatioJaKayttooikeusrajauksillaFilter =
      buildOrganisaatioKayttooikeusFilter(selectedKayttooikeusOrganisaatiot, isOrganisaatioRajain, kayttooikeusHakukohderyhmat)

    Seq(
      s"h.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})",
      hakukohteetOrganisaatioJaKayttooikeusrajauksillaFilter,
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.hakukohde_oid", hakukohteet),
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "hh.hakukohderyhma_oid", hakukohderyhmat),
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.okm_ohjauksen_ala", okmOhjauksenAlat),
      buildTutkinnonTasoFilters(tutkinnonTasot, "h")
    ).filter(_.nonEmpty).mkString("\n")
  }

  private def buildOrganisaatioKayttooikeusFilter(selectedKayttooikeusOrganisaatiot: List[String], isOrganisaatioRajain: Boolean, kayttooikeusHakukohderyhmat: List[String]) = {
    if (isOrganisaatioRajain) {
      // jos organisaatio valittu, ei huomioida käyttäjän organisaation ulkopuolisia hakukohderyhmiä
      makeHakukohderyhmaQueryWithKayttooikeudet(selectedKayttooikeusOrganisaatiot, List.empty, "hh", "h")
    } else {
      makeHakukohderyhmaQueryWithKayttooikeudet(selectedKayttooikeusOrganisaatiot, kayttooikeusHakukohderyhmat, "hh", "h")
    }
  }

  def selectHakukohteittainWithParams(
      selectedKayttooikeusOrganisaatiot: List[String],
      isOrganisaatioRajain: Boolean,
      kayttooikeusHakukohderyhmat: List[String],
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
      isOrganisaatioRajain,
      kayttooikeusHakukohderyhmat,
      hakukohteet,
      hakukohderyhmat,
      okmOhjauksenAlat,
      tutkinnonTasot,
      aidinkielet,
      kansalaisuudet,
      sukupuoli,
      ensikertalainen
    )

    val needsHakukohderyhmaJoin = filters.contains("hh.hakukohderyhma_oid")

    val joinHakukohderyhma =
      if (needsHakukohderyhmaJoin)
        "JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hh ON h.hakukohde_oid = hh.hakukohde_oid"
      else
        ""

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
      #$joinHakukohderyhma
      WHERE #$filters
      GROUP BY h.hakukohde_oid, h.hakukohde_nimi, h.haku_oid, ha.haku_nimi, h.organisaatio_nimi"""
        .as[KkHakeneetHyvaksytytVastaanottaneetHakukohteittain]

    LOG.debug(s"selectHakukohteittainWithParams: ${query.statements.head}")
    query
  }

  def selectHauittainWithParams(
      selectedKayttooikeusOrganisaatiot: List[String],
      isOrganisaatioRajain: Boolean,
      kayttooikeusHakukohderyhmat: List[String],
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

    val filters = buildFiltersForMainQuery(
      haut,
      aidinkielet,
      kansalaisuudet,
      sukupuoli,
      ensikertalainen
    )
    val hakukohdeFilters = buildHakukohdeFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      isOrganisaatioRajain,
      kayttooikeusHakukohderyhmat,
      hakukohteet,
      hakukohderyhmat,
      okmOhjauksenAlat,
      tutkinnonTasot
    )

    val filteredHakukohteet = s"""WITH filtered_hakukohteet AS (
             SELECT h.*
             FROM pub.pub_dim_hakukohde h
             LEFT JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hh
               ON h.hakukohde_oid = hh.hakukohde_oid
             WHERE $hakukohdeFilters
         )
      """

      val query =
        sql"""#$filteredHakukohteet
        SELECT
          ha.haku_oid,
          ha.haku_nimi,
          h.organisaatio_nimi,
          COUNT(DISTINCT t.henkilo_oid) AS hakijat,
          COUNT(DISTINCT t.henkilo_oid) FILTER (WHERE ensisijainen) AS ensisijaisia,
          COUNT(DISTINCT t.henkilo_oid) FILTER (WHERE ensikertalainen) AS ensikertalaisia,
          COUNT(DISTINCT t.henkilo_oid) FILTER (WHERE hyvaksytty) AS hyvaksytyt,
          COUNT(DISTINCT t.henkilo_oid) FILTER (WHERE vastaanottanut) AS vastaanottaneet,
          COUNT(DISTINCT t.henkilo_oid) FILTER (WHERE lasna) AS lasna,
          COUNT(DISTINCT t.henkilo_oid) FILTER (WHERE poissa) AS poissa,
          COUNT(DISTINCT t.henkilo_oid) FILTER (WHERE ilmoittautunut) AS ilm_yht,
          COUNT(DISTINCT t.henkilo_oid) FILTER (WHERE maksuvelvollinen) AS maksuvelvollisia,
          (
            SELECT SUM(h2.valintaperusteiden_aloituspaikat)
            FROM (
              SELECT DISTINCT h3.hakukohde_oid, h3.haku_oid, h3.organisaatio_nimi, h3.valintaperusteiden_aloituspaikat
              FROM filtered_hakukohteet h3
            ) h2
            WHERE h2.haku_oid = ha.haku_oid AND h2.organisaatio_nimi = h.organisaatio_nimi
          ) AS valinnan_aloituspaikat,

          (
            SELECT SUM(h2.hakukohteen_aloituspaikat)
            FROM (
              SELECT DISTINCT h3.hakukohde_oid, h3.haku_oid, h3.organisaatio_nimi, h3.hakukohteen_aloituspaikat
              FROM filtered_hakukohteet h3
            ) h2
            WHERE h2.haku_oid = ha.haku_oid AND h2.organisaatio_nimi = h.organisaatio_nimi
          ) AS aloituspaikat,

          COUNT(DISTINCT t.henkilo_oid) FILTER (WHERE toive_1) AS toive_1,
          COUNT(DISTINCT t.henkilo_oid) FILTER (WHERE toive_2) AS toive_2,
          COUNT(DISTINCT t.henkilo_oid) FILTER (WHERE toive_3) AS toive_3,
          COUNT(DISTINCT t.henkilo_oid) FILTER (WHERE toive_4) AS toive_4,
          COUNT(DISTINCT t.henkilo_oid) FILTER (WHERE toive_5) AS toive_5,
          COUNT(DISTINCT t.henkilo_oid) FILTER (WHERE toive_6) AS toive_6
        FROM pub.pub_fct_raportti_tilastoraportti_kk_hakutoive t
        JOIN filtered_hakukohteet h ON t.hakukohde_oid = h.hakukohde_oid
        JOIN pub.pub_dim_haku ha ON h.haku_oid = ha.haku_oid
        WHERE #$filters
        GROUP BY ha.haku_oid, ha.haku_nimi, h.organisaatio_nimi
        """.as[KkHakeneetHyvaksytytVastaanottaneetHauittainTunnisteella]

    LOG.debug(s"selectHauittainWithParams: ${query.statements.head}")
    query
  }

  def selectToimipisteittainWithParams(
      selectedKayttooikeusOrganisaatiot: List[String],
      isOrganisaatioRajain: Boolean,
      kayttooikeusHakukohderyhmat: List[String],
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

    val filters = buildFiltersForMainQuery(
      haut,
      aidinkielet,
      kansalaisuudet,
      sukupuoli,
      ensikertalainen
    )
    val hakukohdeFilters = buildHakukohdeFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      isOrganisaatioRajain,
      kayttooikeusHakukohderyhmat,
      hakukohteet,
      hakukohderyhmat,
      okmOhjauksenAlat,
      tutkinnonTasot
    )

    val filteredHakukohteet = s"""WITH filtered_hakukohteet AS (
             SELECT h.*
             FROM pub.pub_dim_hakukohde h
             LEFT JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hh
               ON h.hakukohde_oid = hh.hakukohde_oid
             WHERE $hakukohdeFilters
         )
      """

    val query =
      sql"""#$filteredHakukohteet
      SELECT
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
      (
        SELECT SUM(h2.valintaperusteiden_aloituspaikat)
        FROM (
          SELECT DISTINCT h3.hakukohde_oid, h3.toimipiste, h3.organisaatio_nimi, h3.valintaperusteiden_aloituspaikat
          FROM filtered_hakukohteet h3
        ) h2
        WHERE h2.toimipiste = h.toimipiste AND h2.organisaatio_nimi = h.organisaatio_nimi
      ) AS valinnan_aloituspaikat,
      (
        SELECT SUM(h2.hakukohteen_aloituspaikat)
        FROM (
          SELECT DISTINCT h3.hakukohde_oid, h3.toimipiste, h3.organisaatio_nimi, h3.hakukohteen_aloituspaikat
          FROM filtered_hakukohteet h3
        ) h2
        WHERE h2.toimipiste = h.toimipiste AND h2.organisaatio_nimi = h.organisaatio_nimi
      ) AS aloituspaikat,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_1) AS toive_1,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_2) AS toive_2,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_3) AS toive_3,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_4) AS toive_4,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_5) AS toive_5,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_6) AS toive_6
      FROM pub.pub_fct_raportti_tilastoraportti_kk_hakutoive t
      JOIN filtered_hakukohteet h ON t.hakukohde_oid = h.hakukohde_oid
      WHERE #$filters
      GROUP BY h.toimipiste, h.organisaatio_nimi""".as[KkHakeneetHyvaksytytVastaanottaneetToimipisteittain]
    LOG.debug(s"selectToimipisteittainWithParams: ${query.statements.head}")
    query
  }

  def selectOrganisaatioittainWithParams(
      selectedKayttooikeusOrganisaatiot: List[String],
      isOrganisaatioRajain: Boolean,
      kayttooikeusHakukohderyhmat: List[String],
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

    val filters = buildFiltersForMainQuery(
      haut,
      aidinkielet,
      kansalaisuudet,
      sukupuoli,
      ensikertalainen
    )
    val hakukohdeFilters = buildHakukohdeFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      isOrganisaatioRajain,
      kayttooikeusHakukohderyhmat,
      hakukohteet,
      hakukohderyhmat,
      okmOhjauksenAlat,
      tutkinnonTasot
    )

    val filteredHakukohteet = s"""WITH filtered_hakukohteet AS (
             SELECT h.*
             FROM pub.pub_dim_hakukohde h
             LEFT JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hh
               ON h.hakukohde_oid = hh.hakukohde_oid
             WHERE $hakukohdeFilters
         )
      """

    val organisaatio = organisaatiotaso match {
      case "oppilaitoksittain" => "oppilaitos"
      case _                   => "koulutustoimija"
    }
    val organisaatioSelect = organisaatiotaso match {
      case "oppilaitoksittain" => "h.oppilaitos as tunniste, h.oppilaitos_nimi as otsikko"
      case _                   => "h.koulutustoimija as tunniste, h.koulutustoimija_nimi as otsikko"
    }

    val query =
      sql"""#$filteredHakukohteet
      SELECT
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
      (
        SELECT SUM(h2.valintaperusteiden_aloituspaikat)
        FROM (
          SELECT DISTINCT h3.hakukohde_oid, h3.#$organisaatio, h3.#${organisaatio}_nimi, h3.valintaperusteiden_aloituspaikat
          FROM filtered_hakukohteet h3
        ) h2
        WHERE h2.#$organisaatio = h.#$organisaatio AND h2.#${organisaatio}_nimi = h.#${organisaatio}_nimi
      ) AS valinnan_aloituspaikat,
      (
        SELECT SUM(h2.hakukohteen_aloituspaikat)
        FROM (
          SELECT DISTINCT h3.hakukohde_oid, h3.#$organisaatio, h3.#${organisaatio}_nimi, h3.hakukohteen_aloituspaikat
          FROM filtered_hakukohteet h3
        ) h2
        WHERE h2.#$organisaatio = h.#$organisaatio AND h2.#${organisaatio}_nimi = h.#${organisaatio}_nimi
      ) AS aloituspaikat,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_1) AS toive_1,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_2) AS toive_2,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_3) AS toive_3,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_4) AS toive_4,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_5) AS toive_5,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_6) AS toive_6
      FROM pub.pub_fct_raportti_tilastoraportti_kk_hakutoive t
      JOIN filtered_hakukohteet h ON t.hakukohde_oid = h.hakukohde_oid
      WHERE #$filters
      GROUP BY 1, 2, 12, 13""".as[KkHakeneetHyvaksytytVastaanottaneetTunnisteella]

    LOG.debug(s"selectOrganisaatioittainWithParams: ${query.statements.head}")
    query
  }


  def selectOkmOhjauksenAloittainWithParams(
      selectedKayttooikeusOrganisaatiot: List[String],
      isOrganisaatioRajain: Boolean,
      kayttooikeusHakukohderyhmat: List[String],
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

    val filters = buildFiltersForMainQuery(
      haut,
      aidinkielet,
      kansalaisuudet,
      sukupuoli,
      ensikertalainen
    )
    val hakukohdeFilters = buildHakukohdeFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      isOrganisaatioRajain,
      kayttooikeusHakukohderyhmat,
      hakukohteet,
      hakukohderyhmat,
      okmOhjauksenAlat,
      tutkinnonTasot
    )

    val filteredHakukohteet = s"""WITH filtered_hakukohteet AS (
             SELECT h.*
             FROM pub.pub_dim_hakukohde h
             LEFT JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hh
               ON h.hakukohde_oid = hh.hakukohde_oid
             WHERE $hakukohdeFilters
         )
      """

    val query =
      sql"""#$filteredHakukohteet
      SELECT
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
      (
        SELECT SUM(h2.valintaperusteiden_aloituspaikat)
        FROM (
          SELECT DISTINCT h3.hakukohde_oid, h3.okm_ohjauksen_ala, h3.valintaperusteiden_aloituspaikat
          FROM filtered_hakukohteet h3
        ) h2
        WHERE h2.okm_ohjauksen_ala = h.okm_ohjauksen_ala
      ) AS valinnan_aloituspaikat,
      (
        SELECT SUM(h2.hakukohteen_aloituspaikat)
        FROM (
          SELECT DISTINCT h3.hakukohde_oid, h3.okm_ohjauksen_ala, h3.hakukohteen_aloituspaikat
          FROM filtered_hakukohteet h3
        ) h2
        WHERE h2.okm_ohjauksen_ala = h.okm_ohjauksen_ala
      ) AS aloituspaikat,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_1) AS toive_1,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_2) AS toive_2,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_3) AS toive_3,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_4) AS toive_4,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_5) AS toive_5,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_6) AS toive_6
      FROM pub.pub_fct_raportti_tilastoraportti_kk_hakutoive t
      JOIN filtered_hakukohteet h ON t.hakukohde_oid = h.hakukohde_oid
      JOIN pub.pub_dim_koodisto_okmohjauksenala o ON h.okm_ohjauksen_ala = o.koodiarvo
      WHERE #$filters
      GROUP BY h.okm_ohjauksen_ala, o.koodinimi""".as[KkHakeneetHyvaksytytVastaanottaneetTunnisteella]

    LOG.debug(s"selectOkmOhjauksenAloittainWithParams: ${query.statements.head}")
    query
  }

  def selectKansalaisuuksittainWithParams(
      selectedKayttooikeusOrganisaatiot: List[String],
      isOrganisaatioRajain: Boolean,
      kayttooikeusHakukohderyhmat: List[String],
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
      isOrganisaatioRajain,
      kayttooikeusHakukohderyhmat,
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
      LEFT JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hh ON h.hakukohde_oid = hh.hakukohde_oid
      WHERE #$filters
      GROUP BY 1, 11, 12""".as[KkHakeneetHyvaksytytVastaanottaneetResult]

    LOG.debug(s"selectKansalaisuuksittainWithParams: ${query.statements.head}")
    query
  }


  def selectHakukohderyhmittainWithParams(
      selectedKayttooikeusOrganisaatiot: List[String],
      isOrganisaatioRajain: Boolean,
      kayttooikeusHakukohderyhmat: List[String],
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

    val filters = buildFiltersForMainQuery(
      haut,
      aidinkielet,
      kansalaisuudet,
      sukupuoli,
      ensikertalainen
    )
    val hakukohdeFilters = buildHakukohdeFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      isOrganisaatioRajain,
      kayttooikeusHakukohderyhmat,
      hakukohteet,
      hakukohderyhmat,
      okmOhjauksenAlat,
      tutkinnonTasot
    )

    val filteredHakukohteet = s"""WITH filtered_hakukohteet AS (
             SELECT h.*, hh.hakukohderyhma_oid
             FROM pub.pub_dim_hakukohde h
             LEFT JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hh
               ON h.hakukohde_oid = hh.hakukohde_oid
             WHERE $hakukohdeFilters
         )
      """

    val query =
      sql"""
      #$filteredHakukohteet
      SELECT
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
      (
        SELECT SUM(h2.valintaperusteiden_aloituspaikat)
        FROM (
          SELECT DISTINCT h3.hakukohde_oid, h3.hakukohderyhma_oid, h3.valintaperusteiden_aloituspaikat
          FROM filtered_hakukohteet h3
        ) h2
        WHERE h2.hakukohderyhma_oid = hr.hakukohderyhma_oid
      ) AS valinnan_aloituspaikat,
      (
        SELECT SUM(h2.hakukohteen_aloituspaikat)
        FROM (
          SELECT DISTINCT h3.hakukohde_oid, h3.hakukohderyhma_oid, h3.hakukohteen_aloituspaikat
          FROM filtered_hakukohteet h3
        ) h2
        WHERE h2.hakukohderyhma_oid = hr.hakukohderyhma_oid
      ) AS aloituspaikat,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_1) AS toive_1,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_2) AS toive_2,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_3) AS toive_3,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_4) AS toive_4,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_5) AS toive_5,
      COUNT(DISTINCT t.henkilo_oid) filter (WHERE toive_6) AS toive_6
      FROM pub.pub_fct_raportti_tilastoraportti_kk_hakutoive t
      JOIN filtered_hakukohteet h ON t.hakukohde_oid = h.hakukohde_oid
      JOIN pub.pub_dim_hakukohderyhma hr ON h.hakukohderyhma_oid = hr.hakukohderyhma_oid
      WHERE #$filters
      GROUP BY 1, 2, 12, 13""".as[KkHakeneetHyvaksytytVastaanottaneetTunnisteella]

    LOG.debug(s"selectHakukohderyhmittainWithParams: ${query.statements.head}")
    query
  }


  def selectHakijatYhteensaWithParams(
      selectedKayttooikeusOrganisaatiot: List[String],
      isOrganisaatioRajain: Boolean,
      kayttooikeusHakukohderyhmat: List[String],
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

    val hakukohteetOrganisaatioJaKayttooikeusrajauksillaFilter: String = buildOrganisaatioKayttooikeusFilter(selectedKayttooikeusOrganisaatiot, isOrganisaatioRajain, kayttooikeusHakukohderyhmat)

    val filters = Seq(
      s"ht.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})",
      hakukohteetOrganisaatioJaKayttooikeusrajauksillaFilter,
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
      JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hh ON h.hakukohde_oid = hh.hakukohde_oid
      JOIN pub.pub_dim_henkilo he on ht.henkilo_hakemus_id = he.henkilo_hakemus_id
      #$maksuvelvollisuusJoin
      WHERE #$filters
      """.as[Int].head

    LOG.debug(s"selectHakijatYhteensaWithParams: ${query.statements.head}")
    query
  }

  def hakuParamNamesQuery(haut: List[String], koulutustoimija: Option[String], oppilaitokset: List[String], toimipisteet: List[String],
                          hakukohderyhmat: List[String], hakukohteet: List[String], okmOhjauksenAlat: List[String],
                          sukupuoli: Option[String]):
  SqlStreamingAction[Vector[ParametriNimet], ParametriNimet, Effect] = {
    val koulutustoimijaQuery =
      if (koulutustoimija.isDefined)
        s"""UNION ALL SELECT 'koulutustoimija' AS param, organisaatio_nimi AS nimi FROM pub.pub_dim_organisaatio WHERE organisaatio_oid = '${koulutustoimija.get}'"""
      else
        ""
    val oppilaitosQuery = RepositoryUtils.makeHakuParamOptionalQueryStr("oppilaitos", "organisaatio_oid", "organisaatio_nimi", "pub.pub_dim_organisaatio", oppilaitokset)
    val toimipisteQuery = RepositoryUtils.makeHakuParamOptionalQueryStr("toimipiste", "organisaatio_oid", "organisaatio_nimi", "pub.pub_dim_organisaatio", toimipisteet)
    val hakukohderyhmaQuery = RepositoryUtils.makeHakuParamOptionalQueryStr("hakukohderyhma", "hakukohderyhma_oid", "hakukohderyhma_nimi", "pub.pub_dim_hakukohderyhma", hakukohderyhmat)
    val hakukohdeQuery = RepositoryUtils.makeHakuParamOptionalQueryStr("hakukohde", "hakukohde_oid", "hakukohde_nimi", "pub.pub_dim_hakukohde", hakukohteet)
    val okmOhjauksenAlaQuery = RepositoryUtils.makeHakuParamOptionalQueryStr("okm-ohjauksen-ala", "koodiarvo", "koodinimi", "pub.pub_dim_koodisto_okmohjauksenala", okmOhjauksenAlat)
    val sukupuoliQuery =
      if (sukupuoli.isDefined)
        s"""UNION ALL SELECT 'sukupuoli' AS param, koodinimi AS nimi FROM pub.pub_dim_koodisto_sukupuoli WHERE koodiarvo = '${sukupuoli.get}'"""
      else
        ""

    val query = sql"""
      SELECT param, jsonb_agg(nimi) AS nimet
      FROM (
        SELECT 'haku' AS param, haku_nimi AS nimi from pub.pub_dim_haku
        WHERE haku_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(haut)})
        #$koulutustoimijaQuery
        #$oppilaitosQuery
        #$toimipisteQuery
        #$hakukohderyhmaQuery
        #$hakukohdeQuery
        #$okmOhjauksenAlaQuery
        #$sukupuoliQuery
      ) subquery
      GROUP BY param
    """.as[ParametriNimet]
    LOG.debug(s"hakuParamNamesQuery: ${query.statements.head}")
    query
  }
}
