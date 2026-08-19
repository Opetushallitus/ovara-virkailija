package fi.oph.ovara.backend.external.toisenasteenhakijat

import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Repository
import slick.jdbc.PostgresProfile.api.actionBasedSQLInterpolation

@Repository
class ExternalToisenAsteenHakijatRepository(db: ReadOnlyDatabase) extends HakijatExtractors {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ExternalToisenAsteenHakijatRepository])

  private val ataruOidLength = 35

  def selectHakijat(
    hakuOid: String,
    hakukohdeOid: Option[String],
    organisaatioOids: Seq[String],
    valintarajaus: Valintarajaus,
    scope: KayttooikeusScope
  ): Seq[HakijaRow] = {
    // Rajaus käytännössä pakollinen joko hakukohdeOidilla tai organisaatioOidilla
    if (hakukohdeOid.isEmpty && organisaatioOids.isEmpty) {
      Seq.empty
    } else {

      val tilaFiltteriSql       = stateSqlFragment(valintarajaus)
      val hakurajausFiltteriSql = hakuFilterSqlFragment(hakukohdeOid, organisaatioOids)
      // Sallitut organisaatiot on laajennettu lapsiorganisaatioihin jo palvelukerroksessa.
      val kayttooikeusSql = sallitutOrganisaatiotKayttooikeusFragment(scope)

      val query = sql"""
      SELECT hlo.oppijanumero,
        hakemus.hakemus_oid,
        hakemus.sahkoposti,
        hakemus.puhelin,
        hakemus.lahiosoite,
        hakemus.postinumero,
        hakemus.postitoimipaikka,
        haku.haku_oid,
        hakemus.etunimet,
        hakemus.kutsumanimi,
        hakemus.sukunimi,
        hakemus.hetu,
        hakemus.asuinmaa,
        hakemus.kansalaisuus,
        hakemus.kotikunta,
        hakemus.sukupuoli,
        hakemus.koulutusmarkkinointilupa,
        hakemus.kiinnostunut_oppisopimuksesta,
        hakemus.sahkoinenviestintalupa,
        hakemus.valintatuloksen_julkaisulupa,
        hakemus.jatetty,
        hakemus.muokattu,
        hlo.aidinkieli,
        (SELECT st.arvo FROM gen.gen_supa_tieto st
          WHERE st.hakemus_oid = hakemus.hakemus_oid
            AND st.avain = 'perusopetuksen_kieli'
          LIMIT 1) AS opetuskieli,
        (SELECT st.arvo FROM gen.gen_supa_tieto st
          WHERE st.hakemus_oid = hakemus.hakemus_oid
            AND st.avain = 'POHJAKOULUTUS'
          LIMIT 1) AS pohjakoulutus,
        (SELECT st.arvo FROM gen.gen_supa_tieto st
          WHERE st.hakemus_oid = hakemus.hakemus_oid
            AND st.avain = 'PK_PAATTOTODISTUSVUOSI'
          LIMIT 1) AS todistusvuosi,
        (SELECT st.avain FROM gen.gen_supa_tieto st
          WHERE st.hakemus_oid = hakemus.hakemus_oid
            AND st.arvo IN ('true', '"true"')
            AND st.avain IN (
              'LISAKOULUTUS_KYMPPI',
              'LISAKOULUTUS_VAMMAISTEN',
              'LISAKOULUTUS_TALOUS',
              'LISAKOULUTUS_AMMATTISTARTTI',
              'LISAKOULUTUS_KANSANOPISTO',
              'LISAKOULUTUS_MAAHANMUUTTO',
              'LISAKOULUTUS_MAAHANMUUTTO_LUKIO',
              'LISAKOULUTUS_VALMA',
              'LISAKOULUTUS_OPISTOVUOSI',
              'LISAKOULUTUS_TUVA'
            )
          ORDER BY CASE st.avain
            WHEN 'LISAKOULUTUS_KYMPPI'             THEN 1
            WHEN 'LISAKOULUTUS_VAMMAISTEN'         THEN 2
            WHEN 'LISAKOULUTUS_TALOUS'             THEN 3
            WHEN 'LISAKOULUTUS_AMMATTISTARTTI'     THEN 4
            WHEN 'LISAKOULUTUS_KANSANOPISTO'       THEN 5
            WHEN 'LISAKOULUTUS_MAAHANMUUTTO'       THEN 6
            WHEN 'LISAKOULUTUS_MAAHANMUUTTO_LUKIO' THEN 7
            WHEN 'LISAKOULUTUS_VALMA'              THEN 8
            WHEN 'LISAKOULUTUS_OPISTOVUOSI'        THEN 9
            WHEN 'LISAKOULUTUS_TUVA'               THEN 10
          END
          LIMIT 1) AS lisapistekoulutus,
        haku.koulutuksen_alkamisvuosi    AS haku_vuosi,
        haku.koulutuksen_alkamiskausiuri AS haku_kausi
      FROM gen.gen_henkilo hlo
      INNER JOIN gen.gen_hakemus hakemus ON hakemus.henkilo_oid = hlo.henkilo_oid
      INNER JOIN gen.gen_haku    haku    ON hakemus.haku_oid    = haku.haku_oid
      WHERE haku.haku_oid = $hakuOid
      AND length(hakemus.hakemus_oid) = #$ataruOidLength
      AND haku.kohdejoukko_koodiuri LIKE 'haunkohdejoukko_11%'
      AND EXISTS (
        SELECT 1 FROM gen.gen_hakutoive ht
        INNER JOIN gen.gen_hakukohde hk ON ht.hakukohde_oid = hk.hakukohde_oid
        WHERE ht.hakemus_oid = hakemus.hakemus_oid
        #$hakurajausFiltteriSql
        #$tilaFiltteriSql
        #$kayttooikeusSql
      )
      """.as[HakijaRow]

      LOG.debug(s"selectHakijatQuery: ${query.statements.head}")
      db.run(query, "selectHakijat")
    }
  }

  /**
   * Haetaan omana kyselynään eikä LEFT JOINina selectHakijoiden yhteydessä: taulu on leveä (~40 saraketta,
   *  ~2 kt/rivi) ja liitos hakemus_oid:lla johti suunnitelmaan jossa koko taulu seq-skannattiin
   *  kertaalleen jokaista tulosriviä kohden (254 x 110 303 riviä = 18,3 s). Erillisenä kyselynä
   *  se luetaan kerran riippumatta siitä mitä suunnittelija päättää. Sama kuvio kuin
   *  selectLahtokoulut / selectKoodistot.
   */
  def selectToisenAsteenYhteishakuTiedot(hakemusOids: Iterable[String]): Seq[ToisenAsteenYhteishakuRow] = {
    if (hakemusOids.isEmpty) {
      Seq.empty
    } else {

      val query = sql"""
      SELECT hakemus_oid,
        huoltaja1_etunimi,
        huoltaja1_sukunimi,
        huoltaja1_matkapuhelin,
        huoltaja1_email,
        huoltaja2_etunimi,
        huoltaja2_sukunimi,
        huoltaja2_matkapuhelin,
        huoltaja2_email,
        hakukohteet,
        kiinnostunut_urheilijan_ammatillisesta_koulutuksesta,
        urh_laji,
        urh_seura,
        urh_liitto,
        urh_sivulaji,
        urh_keskiarvo,
        urh_tamakausi,
        urh_peruskoulu,
        urh_viimekausi,
        urh_toissakausi,
        urh_valmentaja_puh,
        urh_valmentaja_nimi,
        urh_valmentaja_email,
        urh_valmennusryhma_maajoukkue,
        urh_valmennusryhma_piirijoukkue,
        urh_valmennusryhma_seurajoukkue,
        urh_amm_laji,
        urh_amm_seura,
        urh__amm_liitto,
        urh_amm_sivulaji,
        urh_amm_keskiarvo,
        urh_amm_tamakausi,
        urh_amm_peruskoulu,
        urh_amm_viimekausi,
        urh_amm_toissakausi,
        urh_amm_valmentaja_puh,
        urh_amm_valmentaja_nimi,
        urh_amm_valmentaja_email,
        urh_amm_valmennusryhma_maajoukkue,
        urh_amm_valmennusryhma_piirijoukkue,
        urh_amm_valmennusryhma_seurajoukkue
      FROM gen.gen_hakemus_toinenaste_yhteishaku
      WHERE hakemus_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(hakemusOids)})
      """.as[ToisenAsteenYhteishakuRow]

      LOG.debug(s"selectToisenAsteenYhteishakuTiedotQuery: ${query.statements.head}")
      db.run(query, "selectHakijatToisenAsteenYhteishakuTiedot")
    }
  }

  def selectHakutoiveet(hakemusOids: Iterable[String]): Seq[HakijaHakutoiveRow] = {
    val query = sql"""
    SELECT
      ht.hakemus_oid,
      ht.hakukohde_oid,
      ht.hakutoivenumero,
      hk.jarjestyspaikka_oid,
      org.oppilaitosnumero,
      org_jarjestyspaikka.nimi_fi,
      org_jarjestyspaikka.nimi_sv,
      org_jarjestyspaikka.nimi_en,
      hk.hakukohteen_linja,
      hk.jarjestaa_urheilijan_ammkoulutusta,
      k.koulutukset_koodiuri,
      ht.valintatieto,
      ht.vastaanottotieto,
      ht.ilmoittautumisen_tila,
      ht.harkinnanvaraisuuden_syy,
      (SELECT vr.pisteet FROM gen.gen_valintarekisteri vr
        WHERE vr.hakemus_oid   = ht.hakemus_oid
          AND vr.hakukohde_oid = ht.hakukohde_oid
          AND vr.julkaistavissa = true
        ORDER BY
          CASE vr.valinnan_tila
            WHEN 'HYVAKSYTTY'                     THEN 1
            WHEN 'HARKINNANVARAISESTI_HYVAKSYTTY' THEN 2
            WHEN 'VARASIJALTA_HYVAKSYTTY'         THEN 3
            WHEN 'VARALLA'                        THEN 4
            WHEN 'PERUUTETTU'                     THEN 5
            WHEN 'PERUNUT'                        THEN 6
            WHEN 'PERUUNTUNUT'                    THEN 7
            WHEN 'HYLATTY'                        THEN 8
            WHEN 'KESKEN'                         THEN 9
            ELSE 10
          END,
          CASE
            WHEN vr.valinnan_tila = 'VARALLA' THEN vr.varasijan_numero
            ELSE vr.prioriteetti
          END NULLS LAST
        LIMIT 1) AS pisteet,
      (SELECT flft.arvo FROM gen.gen_valintalaskenta_jonosija_funktiotulokset flft
        WHERE flft.hakemus_oid   = ht.hakemus_oid
          AND flft.hakukohde_oid = ht.hakukohde_oid
          AND flft.tunniste IN ('keskiarvo_pk', 'keskiarvo_lk', 'painotettu_keskiarvo')
          AND flft.arvo IS NOT NULL
          AND flft.arvo <> ''
        LIMIT 1) AS keskiarvo_valintalaskennasta,
      -- Hakemuksen vuosi/kausi johdetaan ensimmäisestä hakutoiveesta, ja se tehdään Scalassa
      -- näiden sarakkeiden pohjalta. hk ja t on jo liitetty, joten sarakkeet ovat ilmaisia.
      -- *_present kertoo onko liitos osunut: selectHakijat käytti aiemmin INNER JOINia, joten
      -- ehdokas on ensimmäinen hakutoive JOLLA rivi on olemassa -- ei vain ensimmäinen hakutoive.
      hk.hakukohde_oid               AS hakukohde_present,
      hk.koulutuksen_alkamisvuosi    AS hakukohde_vuosi,
      hk.koulutuksen_alkamiskausiuri AS hakukohde_kausi,
      t.toteutus_oid                 AS toteutus_present,
      t.koulutuksen_alkamisvuosi     AS toteutus_vuosi,
      t.koulutuksen_alkamiskausiuri  AS toteutus_kausi
    FROM gen.gen_hakutoive ht
    LEFT JOIN gen.gen_hakukohde    hk                 ON ht.hakukohde_oid       = hk.hakukohde_oid
    LEFT JOIN gen.gen_organisaatio org                ON hk.organisaatio_oid    = org.organisaatio_oid
    LEFT JOIN gen.gen_organisaatio org_jarjestyspaikka ON hk.jarjestyspaikka_oid = org_jarjestyspaikka.organisaatio_oid
    LEFT JOIN gen.gen_toteutus     t                  ON hk.toteutus_oid        = t.toteutus_oid
    LEFT JOIN gen.gen_koulutus     k                  ON t.koulutus_oid         = k.koulutus_oid
    WHERE ht.hakemus_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(hakemusOids)})
    """.as[HakijaHakutoiveRow]

    LOG.debug(s"selectHakutoiveetQuery: ${query.statements.head}")
    db.run(query, "selectHakijatHakutoiveet")
  }

  def selectLahtokoulut(hakemusOids: Iterable[String]): Seq[LahtokouluRow] = {
    if (hakemusOids.isEmpty) {
      Seq.empty
    } else {
      // A person may hold multiple gen_henkilo rows sharing the same oppijanumero (master) but
      // different henkilo_oid (linked aliases). The hakemus's declared henkilo_oid and the
      // lahtokoulu row's henkilo_oid may be different aliases of the same person, so both must
      // be resolved through gen_henkilo.oppijanumero before matching.
      val query = sql"""
          SELECT hakemus_oid, lahtokoulu, lahtokoulunnimi, luokka, suoritus_tyyppi
          FROM (
            SELECT hakemus.hakemus_oid AS hakemus_oid,
              org.oppilaitosnumero AS lahtokoulu,
              COALESCE(org.nimi_fi, org.nimi_sv) AS lahtokoulunnimi,
              lk.luokka AS luokka,
              lk.suoritus_tyyppi AS suoritus_tyyppi,
              ROW_NUMBER() OVER (
                PARTITION BY hakemus.hakemus_oid
                ORDER BY lk.suorituksen_alku DESC, lk.henkilo_oid, lk.oppilaitos_oid NULLS LAST
              ) AS rn
            FROM gen.gen_hakemus hakemus
            INNER JOIN gen.gen_henkilo hlo_hak   ON hlo_hak.henkilo_oid   = hakemus.henkilo_oid
            INNER JOIN gen.gen_henkilo hlo_alias ON hlo_alias.oppijanumero = hlo_hak.oppijanumero
            INNER JOIN gen.gen_henkilo_lahtokoulu lk
              ON lk.henkilo_oid = hlo_alias.henkilo_oid
             AND lk.suorituksen_alku <= hakemus.jatetty
             AND (lk.suorituksen_loppu IS NULL OR hakemus.jatetty <= lk.suorituksen_loppu)
            LEFT JOIN gen.gen_organisaatio org ON org.organisaatio_oid = lk.oppilaitos_oid
            WHERE hakemus.hakemus_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(hakemusOids)})
          ) viimeisin
          WHERE rn = 1
          """.as[LahtokouluRow]

      LOG.debug(s"selectLahtokoulutQuery: ${query.statements.head}")
      db.run(query, "selectHakijatLahtokoulut")
    }
  }

  def selectKoodistot(koodiUrit: Set[String]): Seq[KoodistoArvo] = {
    val query = sql"""
    SELECT versioitu_koodiuri,
      koodiarvo,
      koodistouri,
      koodiversio,
      nimi_fi,
      nimi_sv,
      nimi_en
    FROM gen.gen_koodi
    WHERE versioitu_koodiuri in (#${RepositoryUtils.makeListOfValuesQueryStr(koodiUrit)})
    """.as[KoodistoArvo]

    LOG.debug(s"selectKoodistotQuery: ${query.statements.head}")
    db.run(query, "selectHakijatKoodistot")
  }

  /**
   * Molemmat rajaimet sovelletaan jos molemmat on annettu. Aiemmin vain hakukohdeOid päätyi
   *  SQL:ään ja organisaatiorajaus jäi Scalan `matchesFilters`-vaiheeseen; lopputulos on sama,
   *  mutta SQL:ssä joukko rajautuu jo lähteellä.
   */
  private def hakuFilterSqlFragment(
    hakukohdeOid: Option[String],
    organisaatioOids: Seq[String]
  ): String =
    Seq(
      hakukohdeOid.map(hk => s" AND ht.hakukohde_oid = '$hk'"),
      Option.when(organisaatioOids.nonEmpty)(
        s" AND hk.jarjestyspaikka_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(organisaatioOids)})"
      )
    ).flatten.mkString

  /**
   * Käyttäjän organisaatio-oikeudet: sama ehto jonka `matchesFilters` soveltaa Scalassa, mutta
   *  arvioituna jo kannassa. Ei siis kiristä rajausta, vain siirtää sen aiemmaksi.
   *  Tyhjä oikeusjoukko tarkoittaa ettei yhtään hakijaa palauteta (vrt. `allowedMatch`), joten
   *  emitoidaan ehto joka ei täsmää -- tyhjä `IN ()` olisi syntaksivirhe.
   */
  private def sallitutOrganisaatiotKayttooikeusFragment(scope: KayttooikeusScope): String =
    if (scope.isPaakayttaja) ""
    else if (scope.allowedOrgOids.isEmpty) " AND 1 = 0"
    else
      s" AND hk.jarjestyspaikka_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(scope.allowedOrgOids)})"

  private def stateSqlFragment(v: Valintarajaus): String = v match {
    case Valintarajaus.HAKENEET   => ""
    case Valintarajaus.HYVAKSYTYT =>
      " AND ht.valintatieto IN ('HYVAKSYTTY', 'HARKINNANVARAISESTI_HYVAKSYTTY', 'VARASIJALTA_HYVAKSYTTY')"
    case Valintarajaus.VASTAANOTTANEET =>
      " AND ht.vastaanottotieto = 'VASTAANOTTANUT_SITOVASTI'"
  }
}
