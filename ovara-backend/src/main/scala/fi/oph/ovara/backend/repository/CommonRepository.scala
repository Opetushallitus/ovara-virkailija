package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.*
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.springframework.stereotype.Component
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
class CommonRepository extends Extractors {
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
      haunTyyppi: String
  ): SqlStreamingAction[Vector[Haku], Haku, Effect] = {
    val alkamiskaudetAndHenkKohtSuunnitelma =
      RepositoryUtils.extractAlkamisvuosiKausiAndHenkkohtSuunnitelma(alkamiskaudet)

    val alkamiskaudetQueryStr = if (alkamiskaudet.isEmpty) {
      ""
    } else {
      RepositoryUtils.makeHakuQueryWithAlkamiskausiParams(alkamiskaudetAndHenkKohtSuunnitelma)
    }

    sql"""SELECT h.haku_oid, h.haku_nimi
          FROM pub.pub_dim_haku h
          LEFT JOIN (
            SELECT haku_oid, jsonb_array_elements(koulutuksen_alkamiskausi) as alkamiskausi
            FROM pub.pub_dim_haku h2
          ) alkamiskaudet
          ON h.haku_oid = alkamiskaudet.haku_oid
          WHERE h.haun_tyyppi = $haunTyyppi
          AND h.tila != 'poistettu'
          #$alkamiskaudetQueryStr""".as[Haku]
  }

  def selectDistinctExistingHakukohteetWithSelectedOrgsAsJarjestaja(
      orgs: List[String],
      haut: List[String]
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

    sql"""SELECT hk.hakukohde_oid, hk.hakukohde_nimi
          FROM pub.pub_dim_hakukohde hk
          WHERE hk.tila != 'poistettu'
          #$organisaatiotQueryStr
          #$hautQueryStr
          """.as[Hakukohde]
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

  def selectDistinctKunnat(maakunnat: List[String]): SqlStreamingAction[Vector[Koodi], Koodi, Effect] = {
    val maakunnatStr = RepositoryUtils.makeListOfValuesQueryStr(maakunnat)
    val maakunnatQueryStr = if (maakunnatStr.isEmpty) {
      ""
    } else {
      s"AND km.maakunta_koodiarvo in ($maakunnatStr)"
    }
    sql"""SELECT DISTINCT k.koodiarvo, k.koodinimi
          FROM pub.pub_dim_koodisto_kunta k
          JOIN pub.pub_dim_koodisto_kunta_maakunta km
          ON k.koodiarvo = km.kunta_koodiarvo
          #$maakunnatQueryStr""".as[Koodi]
  }

  def selectHakukohderyhmat(
      kayttooikeusHakukohderyhmaOids: List[String],
      haut: List[String]
  ): SqlStreamingAction[Vector[Hakukohderyhma], Hakukohderyhma, Effect] = {
    val hakukohderyhmaStr = RepositoryUtils.makeListOfValuesQueryStr(kayttooikeusHakukohderyhmaOids)
    val hakukohderyhmaQueryStr = if (kayttooikeusHakukohderyhmaOids.isEmpty) {
      ""
    } else {
      s"WHERE hkr.hakukohderyhma_oid in ($hakukohderyhmaStr)"
    }

    val hautStr = RepositoryUtils.makeListOfValuesQueryStr(haut)
    val hautQueryStr = if (hautStr.isEmpty) {
      ""
    } else {
      s"AND hkr_hk.haku_oid in ($hautStr)"
    }

    sql"""SELECT DISTINCT hkr.hakukohderyhma_oid, hkr.hakukohderyhma_nimi
          FROM pub.pub_dim_hakukohderyhma hkr
          JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hkr_hk
          ON hkr.hakukohderyhma_oid = hkr_hk.hakukohderyhma_oid
          #$hakukohderyhmaQueryStr
          #$hautQueryStr""".as[Hakukohderyhma]
  }

  def selectDistinctKoulutusalat1(): SqlStreamingAction[Vector[Koodi], Koodi, Effect] = {
    sql"""SELECT k.kansallinenkoulutusluokitus2016koulutusalataso1 as koodiarvo, k.kansallinenkoulutusluokitus2016koulutusalataso1_nimi as koodinimi
          FROM pub.pub_dim_koodisto_koulutus_alat_ja_asteet k""".as[Koodi]
  }

  def selectDistinctKoulutusalat2(koulutusalat1: List[String]): SqlStreamingAction[Vector[Koodi], Koodi, Effect] = {
    val koulutusala1Str = RepositoryUtils.makeListOfValuesQueryStr(koulutusalat1)
    val koulutusala1QueryStr = if (koulutusala1Str.isEmpty) {
      ""
    } else {
      s"AND k.kansallinenkoulutusluokitus2016koulutusalataso1 in ($koulutusala1Str)"
    }
    sql"""SELECT k.kansallinenkoulutusluokitus2016koulutusalataso2 as koodiarvo, k.kansallinenkoulutusluokitus2016koulutusalataso2_nimi as koodinimi
                  FROM pub.pub_dim_koodisto_koulutus_alat_ja_asteet k
            #$koulutusala1QueryStr""".as[Koodi]
  }

  def selectDistinctKoulutusalat3(koulutusalat2: List[String]): SqlStreamingAction[Vector[Koodi], Koodi, Effect] = {
    val koulutusala2Str = RepositoryUtils.makeListOfValuesQueryStr(koulutusalat2)
    val koulutusala2QueryStr = if (koulutusala2Str.isEmpty) {
      ""
    } else {
      s"AND k.kansallinenkoulutusluokitus2016koulutusalataso2 in ($koulutusala2Str)"
    }
    sql"""SELECT k.kansallinenkoulutusluokitus2016koulutusalataso3 as koodiarvo, k.kansallinenkoulutusluokitus2016koulutusalataso3_nimi as koodinimi
          FROM pub.pub_dim_koodisto_koulutus_alat_ja_asteet k
          #$koulutusala2QueryStr""".as[Koodi]
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
          WHERE organisaatiotyypit ?? '01') AS org
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
            inner join pub.pub_dim_organisaatio org
            on org.organisaatio_oid = x.child_oid""".as[OrganisaatioParentChild]
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
          """.as[OrganisaatioHierarkia]
  }
}
