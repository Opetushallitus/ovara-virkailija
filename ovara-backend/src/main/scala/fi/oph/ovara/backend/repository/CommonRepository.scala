package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.*
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.{Component, Repository}
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
@Repository
class CommonRepository extends Extractors {

  val LOG = LoggerFactory.getLogger(classOf[CommonRepository])

  def selectDistinctAlkamisvuodet(): SqlStreamingAction[Vector[String], String, Effect] = {
    sql"""SELECT DISTINCT koulutuksen_alkamisvuosi
          FROM pub.pub_dim_toteutus pdt
          WHERE koulutuksen_alkamisvuosi IS NOT NULL""".as[String]
  }

  def selectDistinctHarkinnanvaraisuudet(): SqlStreamingAction[Vector[String], String, Effect] = {
    sql"""SELECT DISTINCT harkinnanvaraisuuden_syy
          FROM pub.pub_fct_raportti_hakijat_toinen_aste
          WHERE harkinnanvaraisuuden_syy IS NOT NULL
          AND harkinnanvaraisuuden_syy NOT LIKE 'EI_HARKINNANVARAINEN%'
          AND harkinnanvaraisuuden_syy NOT LIKE 'SURE%'
          AND harkinnanvaraisuuden_syy NOT LIKE '%ULKOMAILLA_OPISKELTU'""".as[String]
  }

  def selectDistinctExistingHaut(
      alkamiskaudet: List[String] = List(),
      selectedHaut: List[String] = List(),
      haunTyyppi: String
  ): SqlStreamingAction[Vector[Haku], Haku, Effect] = {
    val alkamiskaudetAndHenkKohtSuunnitelma =
      RepositoryUtils.extractAlkamisvuosiKausiAndHenkkohtSuunnitelma(alkamiskaudet)

    val alkamiskaudetQueryStr = if (alkamiskaudet.isEmpty) {
      ""
    } else {
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(alkamiskaudetAndHenkKohtSuunnitelma)
    }
    val selectedHautQueryStr = RepositoryUtils.makeOptionalListOfValuesQueryStr("", "h.haku_oid", selectedHaut)

    val combinedQueryStr = (alkamiskaudetQueryStr, selectedHautQueryStr) match {
      case (a, b) if a.nonEmpty && b.nonEmpty => s"AND ($a OR $b)"
      case (a, _) if a.nonEmpty => s"AND $a"
      case (_, b) if b.nonEmpty => s"AND $b"
      case _ => ""
    }

    val query = sql"""SELECT h.haku_oid, h.haku_nimi
          FROM pub.pub_dim_kontrolli_haku h
          WHERE h.haun_tyyppi = $haunTyyppi
          #$combinedQueryStr""".as[Haku]
    query
  }

  def selectDistinctExistingHakukohteetWithSelectedOrgsAsJarjestaja(
      orgs: List[String],
      haut: List[String],
      hakukohderyhmat: List[String],
      hakukohteet: List[String]
  ): SqlStreamingAction[Vector[Hakukohde], Hakukohde, Effect] = {
    val organisaatiotStr = RepositoryUtils.makeListOfValuesQueryStr(orgs)
    val organisaatiotQueryStr = if (organisaatiotStr.isEmpty) {
      ""
    } else { s"AND hk.jarjestyspaikka_oid in ($organisaatiotStr)" }

    val hautStr = RepositoryUtils.makeListOfValuesQueryStr(haut)
    val hautQueryStr = if (hautStr.isEmpty) {
      ""
    } else {
      s"AND hk.haku_oid in ($hautStr)"
    }

    val hakukohderyhmatStr = RepositoryUtils.makeListOfValuesQueryStr(hakukohderyhmat)
    val hakukohderyhmatQueryStr = if (hakukohderyhmatStr.isEmpty) {
      ""
    } else {
      s"AND hkr_hk.hakukohderyhma_oid in ($hakukohderyhmatStr)"
    }

    val hakukohteetStr = RepositoryUtils.makeListOfValuesQueryStr(hakukohteet)
    val hakukohteetQueryStr = if (hakukohteetStr.isEmpty) {
      ""
    } else {
      s"OR hk.hakukohde_oid in ($hakukohteetStr)"
    }

    val query = sql"""SELECT DISTINCT hk.hakukohde_oid, hk.hakukohde_nimi
          FROM pub.pub_dim_hakukohde hk
          LEFT JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hkr_hk
          ON hkr_hk.hakukohde_oid = hk.hakukohde_oid
          WHERE hk.tila != 'poistettu'
          #$organisaatiotQueryStr
          #$hautQueryStr
          #$hakukohderyhmatQueryStr
          #$hakukohteetQueryStr
          """.as[Hakukohde]

    LOG.debug(s"selectDistinctExistingHakukohteetWithSelectedOrgsAsJarjestaja: ${query.statements.head}")
    query
  }

  def selectToisenAsteenPohjakoulutukset = {
    sql"""SELECT pk.koodiarvo, pk.koodinimi
          FROM pub.pub_dim_koodisto_2asteenpohjakoulutus2021 pk
          """.as[Koodi]
  }

  def selectDistinctValintatiedot: SqlStreamingAction[Vector[String], String, Effect] = {
    sql"""SELECT DISTINCT v.valinnan_tila
          FROM pub.pub_dim_valinnantulos v
          WHERE v.valinnan_tila IS NOT NULL
       """.as[String]
  }

  def selectDistinctVastaanottotiedot: SqlStreamingAction[Vector[String], String, Effect] = {
    sql"""SELECT DISTINCT ht.vastaanottotieto
          FROM pub.pub_dim_hakutoive ht
          WHERE ht.vastaanottotieto IS NOT NULL
       """.as[String]
  }

  def selectDistinctOpetuskielet: SqlStreamingAction[Vector[Koodi], Koodi, Effect] = {
    sql"""SELECT ook.koodiarvo, ook.koodinimi
          FROM pub.pub_dim_koodisto_oppilaitoksenopetuskieli ook
       """.as[Koodi]
  }

  def selectDistinctMaakunnat: SqlStreamingAction[Vector[Koodi], Koodi, Effect] = {
    sql"""SELECT mk.koodiarvo, mk.koodinimi
          FROM pub.pub_dim_koodisto_maakunta mk
       """.as[Koodi]
  }

  def selectDistinctKunnat(maakunnat: List[String], selectedKunnat: List[String]): SqlStreamingAction[Vector[Koodi], Koodi, Effect] = {
    val conditions = Map(
      "km.maakunta_koodiarvo" -> maakunnat,
      "k.koodiarvo" -> selectedKunnat
    )
    val whereClause = RepositoryUtils.makeOptionalWhereClause(conditions)

    sql"""SELECT DISTINCT k.koodiarvo, k.koodinimi
          FROM pub.pub_dim_koodisto_kunta k
          JOIN pub.pub_dim_koodisto_kunta_maakunta km
          ON k.koodiarvo = km.kunta_koodiarvo
          #$whereClause""".as[Koodi]
  }

  def selectHakukohderyhmat(
      kayttooikeusHakukohderyhmaOids: List[String],
      haut: List[String]
  ): SqlStreamingAction[Vector[Hakukohderyhma], Hakukohderyhma, Effect] = {
    val hautStr = RepositoryUtils.makeListOfValuesQueryStr(haut)
    val hautQueryStr = if (hautStr.isEmpty) {
      ""
    } else {
      s"WHERE hkr_hk.haku_oid in ($hautStr)"
    }

    val hakukohderyhmaStr = RepositoryUtils.makeListOfValuesQueryStr(kayttooikeusHakukohderyhmaOids)
    val hakukohderyhmaQueryStr = if (kayttooikeusHakukohderyhmaOids.isEmpty) {
      ""
    } else {
      s"AND hkr.hakukohderyhma_oid in ($hakukohderyhmaStr)"
    }

    sql"""SELECT DISTINCT hkr.hakukohderyhma_oid, hkr.hakukohderyhma_nimi
          FROM pub.pub_dim_hakukohderyhma hkr
          JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hkr_hk
          ON hkr.hakukohderyhma_oid = hkr_hk.hakukohderyhma_oid
          #$hautQueryStr
          #$hakukohderyhmaQueryStr
          """.as[Hakukohderyhma]
  }

  def selectDistinctKoulutusalat1(): SqlStreamingAction[Vector[Koodi], Koodi, Effect] = {
    sql"""SELECT DISTINCT k.kansallinenkoulutusluokitus2016koulutusalataso1 as koodiarvo, k.kansallinenkoulutusluokitus2016koulutusalataso1_nimi as koodinimi
          FROM pub.pub_dim_koodisto_koulutus_alat_ja_asteet k""".as[Koodi]
  }

  def selectDistinctKoulutusalat2(koulutusalat1: List[String], selectedKoulutusalat2: List[String]): SqlStreamingAction[Vector[Koodi], Koodi, Effect] = {
    val conditions = Map(
      "k.kansallinenkoulutusluokitus2016koulutusalataso1" -> koulutusalat1,
      "k.kansallinenkoulutusluokitus2016koulutusalataso2" -> selectedKoulutusalat2
    )
    val whereClause = RepositoryUtils.makeOptionalWhereClause(conditions)

    sql"""SELECT DISTINCT k.kansallinenkoulutusluokitus2016koulutusalataso2 as koodiarvo, k.kansallinenkoulutusluokitus2016koulutusalataso2_nimi as koodinimi
          FROM pub.pub_dim_koodisto_koulutus_alat_ja_asteet k
            #$whereClause""".as[Koodi]
  }

  def selectDistinctKoulutusalat3(koulutusalat2: List[String], selectedKoulutusalat3: List[String]): SqlStreamingAction[Vector[Koodi], Koodi, Effect] = {
    val conditions = Map(
      "k.kansallinenkoulutusluokitus2016koulutusalataso2" -> koulutusalat2,
      "k.kansallinenkoulutusluokitus2016koulutusalataso3" -> selectedKoulutusalat3
    )
    val whereClause = RepositoryUtils.makeOptionalWhereClause(conditions)

    sql"""SELECT DISTINCT k.kansallinenkoulutusluokitus2016koulutusalataso3 as koodiarvo, k.kansallinenkoulutusluokitus2016koulutusalataso3_nimi as koodinimi
          FROM pub.pub_dim_koodisto_koulutus_alat_ja_asteet k
          #$whereClause""".as[Koodi]
  }

  def selectDistinctOkmOhjauksenAlat: SqlStreamingAction[Vector[Koodi], Koodi, Effect] = {
    sql"""SELECT oo.koodiarvo, oo.koodinimi
          FROM pub.pub_dim_koodisto_okmohjauksenala oo""".as[Koodi]
  }

  def selectDistinctYokokeet: SqlStreamingAction[Vector[Koodi], Koodi, Effect] = {
    sql"""SELECT yo.koodiarvo, yo.koodinimi
          FROM pub.pub_dim_koodisto_yokokeet yo
       """.as[Koodi]
  }

  def selectDistinctOrganisaatiot(
      organisaatiot: List[String]
  ): SqlStreamingAction[Vector[Organisaatio], Organisaatio, Effect] = {
    val organisaatiotStr = organisaatiot.map(s => s"'$s'").mkString(",")
    val optionalOrganisaatiotClause =
      if (organisaatiotStr.isEmpty) "" else s"where org.organisaatio_oid in ($organisaatiotStr)"
    sql"""SELECT *
          FROM (SELECT organisaatio_oid, organisaatio_nimi, organisaatiotyypit
                FROM pub.pub_dim_organisaatio o) AS org
                #$optionalOrganisaatiotClause""".as[Organisaatio]
  }

  def selectDistinctKoulutustoimijat(
      organisaatiot: List[String]
  ): SqlStreamingAction[Vector[Organisaatio], Organisaatio, Effect] = {
    val organisaatiotStr = organisaatiot.map(s => s"'$s'").mkString(",")
    val optionalOrganisaatiotClause =
      if (organisaatiotStr.isEmpty) "" else s"where org.organisaatio_oid in ($organisaatiotStr)"
    sql"""SELECT *
          FROM (SELECT organisaatio_oid, organisaatio_nimi, organisaatiotyypit
                FROM pub.pub_dim_organisaatio o
          WHERE organisaatiotyypit ?? '01' AND tila='AKTIIVINEN') AS org
          #$optionalOrganisaatiotClause""".as[Organisaatio]
  }

  def selectChildOrganisaatiot(
      organisaatiot: List[String]
  ): SqlStreamingAction[Vector[OrganisaatioParentChild], OrganisaatioParentChild, Effect] = {
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
          ) SELECT parent_oid, child_oid, organisaatio_nimi, organisaatiotyypit
            FROM x
            INNER JOIN pub.pub_dim_organisaatio org
            ON org.organisaatio_oid = x.child_oid""".as[OrganisaatioParentChild]
  }

  val selectOrganisaatioHierarkiaSql =
    """SELECT organisaatio_oid,
              organisaatio_nimi,
              organisaatiotyypit,
              oppilaitostyyppi,
              tila,
              parent_oids,
              children"""

  def selectKoulutustoimijaDescendants(
      koulutustoimijaOids: List[String]
  ): SqlStreamingAction[Vector[OrganisaatioHierarkia], OrganisaatioHierarkia, Effect] = {
    val organisaatiotStr = koulutustoimijaOids.map(s => s"'$s'").mkString(",")
    sql"""#$selectOrganisaatioHierarkiaSql
          FROM pub.pub_dim_koulutustoimija_ja_toimipisteet,
          LATERAL jsonb_array_elements_text(parent_oids) AS parent_oid
          WHERE parent_oid IN (#$organisaatiotStr)
          AND tila='AKTIIVINEN'
          """.as[OrganisaatioHierarkia]
  }

  def selectOppilaitosDescendants(
      oids: List[String]
  ): SqlStreamingAction[Vector[OrganisaatioHierarkia], OrganisaatioHierarkia, Effect] = {
    val organisaatiotStr = oids.map(s => s"'$s'").mkString(",")
    sql"""#$selectOrganisaatioHierarkiaSql
          FROM pub.pub_dim_oppilaitos_ja_toimipisteet,
          LATERAL jsonb_array_elements_text(parent_oids) AS parent_oid
          WHERE parent_oid IN (#$organisaatiotStr)
          AND tila='AKTIIVINEN'
          """.as[OrganisaatioHierarkia]
  }

  def selectToimipisteDescendants(
      oids: List[String]
  ): SqlStreamingAction[Vector[OrganisaatioHierarkia], OrganisaatioHierarkia, Effect] = {
    val organisaatiotStr = oids.map(s => s"'$s'").mkString(",")
    sql"""#$selectOrganisaatioHierarkiaSql
          FROM pub.pub_dim_toimipiste_ja_toimipisteet,
          LATERAL jsonb_array_elements_text(parent_oids) AS parent_oid
          WHERE parent_oid IN (#$organisaatiotStr)
          AND tila='AKTIIVINEN'
          """.as[OrganisaatioHierarkia]
  }
}
