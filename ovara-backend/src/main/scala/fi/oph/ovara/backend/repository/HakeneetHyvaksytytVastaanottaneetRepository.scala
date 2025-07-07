package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.{HakeneetHyvaksytytVastaanottaneetHakukohteittain, HakeneetHyvaksytytVastaanottaneetResult, HakeneetHyvaksytytVastaanottaneetToimipisteittain, HakeneetHyvaksytytVastaanottaneetTunnisteella}
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.{Component, Repository}
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
@Repository
class HakeneetHyvaksytytVastaanottaneetRepository extends Extractors {
  val LOG: Logger = LoggerFactory.getLogger(classOf[HakeneetHyvaksytytVastaanottaneetRepository]);
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
      Option(
        RepositoryUtils.makeOptionalListOfValuesQueryStr(
          "AND",
          "h.jarjestyspaikka_oid",
          selectedKayttooikeusOrganisaatiot
        )
      ).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.hakukohde_oid", hakukohteet)).filter(
        _.nonEmpty
      ),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_1", koulutusalat1)).filter(
        _.nonEmpty
      ),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_2", koulutusalat2)).filter(
        _.nonEmpty
      ),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_3", koulutusalat3)).filter(
        _.nonEmpty
      ),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.sijaintimaakunta", maakunnat)).filter(
        _.nonEmpty
      ),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.sijaintikunta", kunnat)).filter(_.nonEmpty),
      Option(
        RepositoryUtils.makeOptionalListOfValuesQueryStr(
          "AND",
          "t.harkinnanvaraisuuden_syy",
          harkinnanvaraisuudetWithSureValues
        )
      ).filter(_.nonEmpty),
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

  def selectHakukohteittainWithParams2(
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
                                       sukupuoli: Option[String]
                                     ): SqlStreamingAction[Vector[
    HakeneetHyvaksytytVastaanottaneetHakukohteittain
  ], HakeneetHyvaksytytVastaanottaneetHakukohteittain, Effect] = {

    val filters = buildFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      hakukohteet,
      koulutusalat1,
      koulutusalat2,
      koulutusalat3,
      opetuskielet,
      maakunnat,
      kunnat,
      harkinnanvaraisuudet,
      sukupuoli
    )

    val query = sql"""SELECT
        h.hakukohde_oid,
        h.hakukohde_nimi,
        h.haku_oid,
        ha.haku_nimi,
        h.organisaatio_nimi,
        COUNT(t.hakutoive_id) AS hakijat,
        COUNT(t.hakutoive_id) filter (WHERE ensisijainen) AS ensisijaisia,
        COUNT(t.hakutoive_id) filter (WHERE varasija) AS varasija,
        COUNT(t.hakutoive_id) filter (WHERE hyvaksytty) AS hyvaksytyt,
        COUNT(t.hakutoive_id) filter (WHERE vastaanottanut) AS vastaanottaneet,
        COUNT(t.hakutoive_id) filter (WHERE lasna) AS lasna,
        COUNT(t.hakutoive_id) filter (WHERE poissa) AS poissa,
        COUNT(t.hakutoive_id) filter (WHERE ilmoittautunut) AS ilm_yht,
        MIN(h.hakukohteen_aloituspaikat) AS aloituspaikat,
        COUNT(t.hakutoive_id) filter (WHERE toive_1) AS toive1,
        COUNT(t.hakutoive_id) filter (WHERE toive_2) AS toive2,
        COUNT(t.hakutoive_id) filter (WHERE toive_3) AS toive3,
        COUNT(t.hakutoive_id) filter (WHERE toive_4) AS toive4,
        COUNT(t.hakutoive_id) filter (WHERE toive_5) AS toive5,
        COUNT(t.hakutoive_id) filter (WHERE toive_6) AS toive6,
        COUNT(t.hakutoive_id) filter (WHERE toive_7) AS toive7
    FROM pub.pub_fct_raportti_tilastoraportti_2aste_hakutoive t
    JOIN pub.pub_dim_hakukohde h
    ON t.hakukohde_oid = h.hakukohde_oid
    JOIN pub.pub_dim_haku ha
    ON h.haku_oid = ha.haku_oid
    WHERE #$filters
    GROUP BY h.hakukohde_oid, h.hakukohde_nimi, h.haku_oid, ha.haku_nimi, h.organisaatio_nimi""".as[HakeneetHyvaksytytVastaanottaneetHakukohteittain]
    LOG.debug(s"selectHakukohteittainWithParams2: ${query.statements.head}")
    query
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
      sukupuoli: Option[String]
  ): SqlStreamingAction[Vector[
    HakeneetHyvaksytytVastaanottaneetHakukohteittain
  ], HakeneetHyvaksytytVastaanottaneetHakukohteittain, Effect] = {

    val filters = buildFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      hakukohteet,
      koulutusalat1,
      koulutusalat2,
      koulutusalat3,
      opetuskielet,
      maakunnat,
      kunnat,
      harkinnanvaraisuudet,
      sukupuoli
    )

    val query = sql"""SELECT
        h.hakukohde_oid,
        h.hakukohde_nimi,
        h.haku_oid,
        ha.haku_nimi,
        h.organisaatio_nimi,
        SUM(t.hakijat) AS hakijat,
        SUM(t.ensisijaisia) AS ensisijaisia,
        SUM(t.varasija) AS varasija,
        SUM(t.hyvaksytyt) AS hyvaksytyt,
        SUM(t.vastaanottaneet) AS vastaanottaneet,
        SUM(t.lasna) AS lasna,
        SUM(t.poissa) AS poissa,
        SUM(t.ilm_yht) AS ilm_yht,
        MIN(h.hakukohteen_aloituspaikat) AS aloituspaikat,
        SUM(t.toive_1) AS toive1,
        SUM(t.toive_2) AS toive2,
        SUM(t.toive_3) AS toive3,
        SUM(t.toive_4) AS toive4,
        SUM(t.toive_5) AS toive5,
        SUM(t.toive_6) AS toive6,
        SUM(t.toive_7) AS toive7
    FROM pub.pub_fct_raportti_tilastoraportti_toinen_aste t
    JOIN pub.pub_dim_hakukohde h
    ON t.hakukohde_oid = h.hakukohde_oid
    JOIN pub.pub_dim_haku ha
    ON h.haku_oid = ha.haku_oid
    WHERE #$filters
    GROUP BY h.hakukohde_oid, h.hakukohde_nimi, h.haku_oid, ha.haku_nimi, h.organisaatio_nimi""".as[HakeneetHyvaksytytVastaanottaneetHakukohteittain]
    LOG.debug(s"selectHakukohteittainWithParams: ${query.statements.head}")
    query
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
      sukupuoli: Option[String]
  ): DBIO[Int] = {

    val harkinnanvaraisuudetWithSureValues = RepositoryUtils.enrichHarkinnanvaraisuudet(harkinnanvaraisuudet)
    val filters = Seq(
      Some(s"ht.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})"),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.jarjestyspaikka_oid", selectedKayttooikeusOrganisaatiot)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "ht.hakukohde_oid", hakukohteet)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_1", koulutusalat1)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_2", koulutusalat2)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "t.koulutusalataso_3", koulutusalat3)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.sijaintimaakunta", maakunnat)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.sijaintikunta", kunnat)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "ht.harkinnanvaraisuuden_syy", harkinnanvaraisuudetWithSureValues)).filter(_.nonEmpty),
      Option(RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "he.sukupuoli", sukupuoli)).filter(_.nonEmpty),
      buildOpetuskieletFilter(opetuskielet)
    ).collect { case Some(value) => value }.mkString("\n")

    val query = sql"""SELECT count(distinct ht.henkilo_oid)
      FROM pub.pub_dim_hakutoive ht
      JOIN pub.pub_dim_hakukohde h ON ht.hakukohde_oid = h.hakukohde_oid
      JOIN pub.pub_fct_raportti_tilastoraportti_toinen_aste t ON t.hakukohde_oid = ht.hakukohde_oid
      JOIN pub.pub_dim_henkilo he on ht.henkilo_hakemus_id = he.henkilo_hakemus_id
    WHERE #$filters
    """.as[Int].head
    LOG.debug(s"selectHakijatYhteensaWithParams: ${query.statements.head}")
    query
  }

  def selectKoulutusaloittainWithParams2(
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
                                         sukupuoli: Option[String]
                                       ): SqlStreamingAction[Vector[
    HakeneetHyvaksytytVastaanottaneetTunnisteella
  ], HakeneetHyvaksytytVastaanottaneetTunnisteella, Effect] = {

    val filters = buildFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      hakukohteet,
      koulutusalat1,
      koulutusalat2,
      koulutusalat3,
      opetuskielet,
      maakunnat,
      kunnat,
      harkinnanvaraisuudet,
      sukupuoli
    )

    val hakukohdeHakufilter = s"h.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})"
    val hakukohdeOrganisaatioFilter = RepositoryUtils.makeOptionalListOfValuesQueryStr(
      "AND",
      "h.jarjestyspaikka_oid",
      selectedKayttooikeusOrganisaatiot
    )
    val hakukohdeFilter = RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.hakukohde_oid", hakukohteet)
    val maakuntaFilter = RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.sijaintimaakunta", maakunnat)
    val kuntaFilter = RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.sijaintikunta", kunnat)

    val query = sql"""SELECT
    h.koulutusala,
	h.koulutusala_nimi,
    count(distinct t.henkilo_oid) AS hakijat,
    count(distinct t.henkilo_oid) filter (where ensisijainen) AS ensisijaisia,
    count(distinct t.henkilo_oid) filter (where varasija) AS varasija,
    count(distinct t.henkilo_oid) filter (where hyvaksytty) AS hyvaksytyt,
    count(distinct t.henkilo_oid) filter (where vastaanottanut = true) AS vastaanottaneet,
    count(distinct t.henkilo_oid) filter (where lasna) AS lasna,
    count(distinct t.henkilo_oid) filter (where poissa) AS poissa,
    count(distinct t.henkilo_oid) filter (where ilmoittautunut) AS ilm_yht,
    a.aloituspaikat,
    count(distinct t.henkilo_oid) filter (where toive_1) AS toive1,
    count(distinct t.henkilo_oid) filter (where toive_2) AS toive2,
    count(distinct t.henkilo_oid) filter (where toive_3) AS toive3,
    count(distinct t.henkilo_oid) filter (where toive_4) AS toive4,
    count(distinct t.henkilo_oid) filter (where toive_5) AS toive5,
    count(distinct t.henkilo_oid) filter (where toive_6) AS toive6,
    count(distinct t.henkilo_oid) filter (where toive_7) AS toive7
    FROM pub.pub_fct_raportti_tilastoraportti_2aste_hakutoive t
    JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
    JOIN pub.pub_dim_haku ha ON h.haku_oid = ha.haku_oid
    JOIN (
	    SELECT
		  h.koulutusala,
		  SUM(h.hakukohteen_aloituspaikat ) as aloituspaikat
	    FROM pub.pub_dim_hakukohde h
	    WHERE #$hakukohdeHakufilter
      #$hakukohdeOrganisaatioFilter
      #$hakukohdeFilter
      #$maakuntaFilter
      #$kuntaFilter
	    group by 1) a on h.koulutusala = a.koulutusala
    WHERE #$filters
    GROUP BY 1, 2, 11""".as[HakeneetHyvaksytytVastaanottaneetTunnisteella]
    LOG.info(s"selectKoulutusaloittainWithParams2: ${query.statements.head}")
    query
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
      sukupuoli: Option[String]
  ): SqlStreamingAction[Vector[
    HakeneetHyvaksytytVastaanottaneetResult
  ], HakeneetHyvaksytytVastaanottaneetResult, Effect] = {

    val filters = buildFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      hakukohteet,
      koulutusalat1,
      koulutusalat2,
      koulutusalat3,
      opetuskielet,
      maakunnat,
      kunnat,
      harkinnanvaraisuudet,
      sukupuoli
    )

    val query = sql"""SELECT
        ka.koodinimi AS otsikko,
	      SUM(a.hakijat) as hakijat,
	      SUM(a.ensisijaisia) AS ensisijaisia,
        SUM(a.varasija) AS varasija,
        SUM(a.hyvaksytyt) AS hyvaksytyt,
        SUM(a.vastaanottaneet) AS vastaanottaneet,
        SUM(a.lasna) AS lasna,
        SUM(a.poissa) AS poissa,
        SUM(a.ilm_yht) AS ilm_yht,
        SUM(b.hakukohteen_aloituspaikat) AS aloituspaikat,
        SUM(a.toive1) AS toive1,
        SUM(a.toive2) AS toive2,
        SUM(a.toive3) AS toive3,
        SUM(a.toive4) AS toive4,
        SUM(a.toive5) AS toive5,
        SUM(a.toive6) AS toive6,
        SUM(a.toive7) AS toive7
	    FROM (
		    SELECT
      	  t.koulutusalataso_1,
          h.hakukohde_oid,
          SUM(t.hakijat) AS hakijat,
          SUM(t.ensisijaisia) AS ensisijaisia,
          SUM(t.varasija) AS varasija,
          SUM(t.hyvaksytyt) AS hyvaksytyt,
          SUM(t.vastaanottaneet) AS vastaanottaneet,
          SUM(t.lasna) AS lasna,
          SUM(t.poissa) AS poissa,
          SUM(t.ilm_yht) AS ilm_yht,
          SUM(t.toive_1) AS toive1,
          SUM(t.toive_2) AS toive2,
          SUM(t.toive_3) AS toive3,
          SUM(t.toive_4) AS toive4,
          SUM(t.toive_5) AS toive5,
          SUM(t.toive_6) AS toive6,
          SUM(t.toive_7) AS toive7
	      FROM pub.pub_fct_raportti_tilastoraportti_toinen_aste t
        JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
	      WHERE #$filters
        GROUP BY t.koulutusalataso_1, h.hakukohde_oid
	    ) a
      JOIN pub.pub_dim_hakukohde b on a.hakukohde_oid = b.hakukohde_oid
      JOIN pub.pub_dim_koodisto_koulutusalataso1 ka ON a.koulutusalataso_1 = ka.koodiarvo
      GROUP BY 1""".as[HakeneetHyvaksytytVastaanottaneetResult]
    LOG.debug(s"selectKoulutusaloittainWithParams: ${query.statements.head}")
    query
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
      organisaatiotaso: String
  ): SqlStreamingAction[Vector[
    HakeneetHyvaksytytVastaanottaneetResult
  ], HakeneetHyvaksytytVastaanottaneetResult, Effect] = {
    val filters = buildFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      hakukohteet,
      koulutusalat1,
      koulutusalat2,
      koulutusalat3,
      opetuskielet,
      maakunnat,
      kunnat,
      harkinnanvaraisuudet,
      sukupuoli
    )
    val organisaatioSelect = organisaatiotaso match {
      case "oppilaitoksittain" => "b.oppilaitos_nimi as otsikko"
      case _                   => "b.koulutustoimija_nimi as otsikko"
    }
    val query = sql"""SELECT
        #$organisaatioSelect,
	      SUM(a.hakijat) as hakijat,
	      SUM(a.ensisijaisia) AS ensisijaisia,
        SUM(a.varasija) AS varasija,
        SUM(a.hyvaksytyt) AS hyvaksytyt,
        SUM(a.vastaanottaneet) AS vastaanottaneet,
        SUM(a.lasna) AS lasna,
        SUM(a.poissa) AS poissa,
        SUM(a.ilm_yht) AS ilm_yht,
        SUM(b.hakukohteen_aloituspaikat) AS aloituspaikat,
        SUM(a.toive1) AS toive1,
        SUM(a.toive2) AS toive2,
        SUM(a.toive3) AS toive3,
        SUM(a.toive4) AS toive4,
        SUM(a.toive5) AS toive5,
        SUM(a.toive6) AS toive6,
        SUM(a.toive7) AS toive7
	    FROM (
		    SELECT
      		h.hakukohde_oid,
            SUM(t.hakijat) AS hakijat,
            SUM(t.ensisijaisia) AS ensisijaisia,
            SUM(t.varasija) AS varasija,
            SUM(t.hyvaksytyt) AS hyvaksytyt,
            SUM(t.vastaanottaneet) AS vastaanottaneet,
            SUM(t.lasna) AS lasna,
            SUM(t.poissa) AS poissa,
            SUM(t.ilm_yht) AS ilm_yht,
            SUM(t.toive_1) AS toive1,
            SUM(t.toive_2) AS toive2,
            SUM(t.toive_3) AS toive3,
            SUM(t.toive_4) AS toive4,
            SUM(t.toive_5) AS toive5,
            SUM(t.toive_6) AS toive6,
            SUM(t.toive_7) AS toive7
	      FROM pub.pub_fct_raportti_tilastoraportti_toinen_aste t
	      JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
	      WHERE #$filters
        GROUP BY h.hakukohde_oid
	    ) a
      JOIN pub.pub_dim_hakukohde b on a.hakukohde_oid = b.hakukohde_oid
      GROUP BY 1""".as[HakeneetHyvaksytytVastaanottaneetResult]
    LOG.debug(s"selectOrganisaatioittainWithParams: ${query.statements.head}")
    query
  }

  def selectToimipisteittainWithParams2(
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
                                        sukupuoli: Option[String]
                                      ): SqlStreamingAction[Vector[
    HakeneetHyvaksytytVastaanottaneetToimipisteittain
  ], HakeneetHyvaksytytVastaanottaneetToimipisteittain, Effect] = {
    val filters = buildFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      hakukohteet,
      koulutusalat1,
      koulutusalat2,
      koulutusalat3,
      opetuskielet,
      maakunnat,
      kunnat,
      harkinnanvaraisuudet,
      sukupuoli
    )
    val hakukohdeHakufilter = s"h.haku_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(haut)})"
    val hakukohdeOrganisaatioFilter = RepositoryUtils.makeOptionalListOfValuesQueryStr(
      "AND",
      "h.jarjestyspaikka_oid",
      selectedKayttooikeusOrganisaatiot
    )
    val hakukohdeFilter = RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.hakukohde_oid", hakukohteet)
    val maakuntaFilter = RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.sijaintimaakunta", maakunnat)
    val kuntaFilter = RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "h.sijaintikunta", kunnat)

    val query = sql"""SELECT
    h.toimipiste,
    h.organisaatio_nimi,
    count(distinct t.henkilo_oid) AS hakijat,
    count(distinct t.henkilo_oid) filter (where ensisijainen) AS ensisijaisia,
    count(distinct t.henkilo_oid) filter (where varasija) AS varasija,
    count(distinct t.henkilo_oid) filter (where hyvaksytty) AS hyvaksytyt,
    count(distinct t.henkilo_oid) filter (where vastaanottanut = true) AS vastaanottaneet,
    count(distinct t.henkilo_oid) filter (where lasna) AS lasna,
    count(distinct t.henkilo_oid) filter (where poissa) AS poissa,
    count(distinct t.henkilo_oid) filter (where ilmoittautunut) AS ilm_yht,
    a.aloituspaikat,
    count(distinct t.henkilo_oid) filter (where toive_1) AS toive1,
    count(distinct t.henkilo_oid) filter (where toive_2) AS toive2,
    count(distinct t.henkilo_oid) filter (where toive_3) AS toive3,
    count(distinct t.henkilo_oid) filter (where toive_4) AS toive4,
    count(distinct t.henkilo_oid) filter (where toive_5) AS toive5,
    count(distinct t.henkilo_oid) filter (where toive_6) AS toive6,
    count(distinct t.henkilo_oid) filter (where toive_7) AS toive7
    FROM pub.pub_fct_raportti_tilastoraportti_2aste_hakutoive t
    JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
    JOIN pub.pub_dim_haku ha ON h.haku_oid = ha.haku_oid
    JOIN (
	    SELECT
		  h.toimipiste,
		  SUM(h.hakukohteen_aloituspaikat ) as aloituspaikat
	    FROM pub.pub_dim_hakukohde h
	    WHERE #$hakukohdeHakufilter
      #$hakukohdeOrganisaatioFilter
      #$hakukohdeFilter
      #$maakuntaFilter
      #$kuntaFilter
	    group by 1) a on h.toimipiste = a.toimipiste
    WHERE #$filters
    GROUP BY 1, 2, 11""".as[HakeneetHyvaksytytVastaanottaneetToimipisteittain]
    LOG.debug(s"selectToimipisteittainWithParams: ${query.statements.head}")
    query
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
      sukupuoli: Option[String]
  ): SqlStreamingAction[Vector[
    HakeneetHyvaksytytVastaanottaneetToimipisteittain
  ], HakeneetHyvaksytytVastaanottaneetToimipisteittain, Effect] = {
    val filters = buildFilters(
      haut,
      selectedKayttooikeusOrganisaatiot,
      hakukohteet,
      koulutusalat1,
      koulutusalat2,
      koulutusalat3,
      opetuskielet,
      maakunnat,
      kunnat,
      harkinnanvaraisuudet,
      sukupuoli
    )

    val query = sql"""SELECT
	    b.toimipiste,
	    b.organisaatio_nimi,
	    SUM(a.hakijat) as hakijat,
	    SUM(a.ensisijaisia) AS ensisijaisia,
      SUM(a.varasija) AS varasija,
      SUM(a.hyvaksytyt) AS hyvaksytyt,
      SUM(a.vastaanottaneet) AS vastaanottaneet,
      SUM(a.lasna) AS lasna,
      SUM(a.poissa) AS poissa,
      SUM(a.ilm_yht) AS ilm_yht,
      SUM(b.hakukohteen_aloituspaikat) AS aloituspaikat,
      SUM(a.toive1) AS toive1,
      SUM(a.toive2) AS toive2,
      SUM(a.toive3) AS toive3,
      SUM(a.toive4) AS toive4,
      SUM(a.toive5) AS toive5,
      SUM(a.toive6) AS toive6,
      SUM(a.toive7) AS toive7
	    FROM (
		    SELECT
      		h.hakukohde_oid,
          SUM(t.hakijat) AS hakijat,
          SUM(t.ensisijaisia) AS ensisijaisia,
          SUM(t.varasija) AS varasija,
          SUM(t.hyvaksytyt) AS hyvaksytyt,
          SUM(t.vastaanottaneet) AS vastaanottaneet,
          SUM(t.lasna) AS lasna,
          SUM(t.poissa) AS poissa,
          SUM(t.ilm_yht) AS ilm_yht,
          SUM(t.toive_1) AS toive1,
          SUM(t.toive_2) AS toive2,
          SUM(t.toive_3) AS toive3,
          SUM(t.toive_4) AS toive4,
          SUM(t.toive_5) AS toive5,
          SUM(t.toive_6) AS toive6,
          SUM(t.toive_7) AS toive7
	      FROM pub.pub_fct_raportti_tilastoraportti_toinen_aste t
	      JOIN pub.pub_dim_hakukohde h ON t.hakukohde_oid = h.hakukohde_oid
	      WHERE #$filters
        GROUP BY h.hakukohde_oid
	    ) a
      JOIN pub.pub_dim_hakukohde b ON a.hakukohde_oid = b.hakukohde_oid
      GROUP BY 1,2""".as[HakeneetHyvaksytytVastaanottaneetToimipisteittain]
    LOG.debug(s"selectToimipisteittainWithParams: ${query.statements.head}")
    query
  }

}
