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
    organisaatioOid: Option[String],
    valintarajaus: Valintarajaus
  ): Seq[HakijaRow] = {
    // Controller-level validation guarantees at least one of these is set.
    if (hakukohdeOid.isEmpty && organisaatioOid.isEmpty) return Seq.empty

    val stateSql      = stateSqlFragment(valintarajaus)
    val hakuFilterSql = hakuFilterSqlFragment(hakukohdeOid, organisaatioOid)

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
      (SELECT hk.koulutuksen_alkamisvuosi FROM gen.gen_hakutoive ht
        INNER JOIN gen.gen_hakukohde hk ON ht.hakukohde_oid = hk.hakukohde_oid
        WHERE ht.hakemus_oid = hakemus.hakemus_oid
        ORDER BY ht.hakutoivenumero
        LIMIT 1) AS hakukohde_vuosi,
      (SELECT hk.koulutuksen_alkamiskausiuri FROM gen.gen_hakutoive ht
        INNER JOIN gen.gen_hakukohde hk ON ht.hakukohde_oid = hk.hakukohde_oid
        WHERE ht.hakemus_oid = hakemus.hakemus_oid
        ORDER BY ht.hakutoivenumero
        LIMIT 1) AS hakukohde_kausi,
      haku.koulutuksen_alkamisvuosi    AS haku_vuosi,
      haku.koulutuksen_alkamiskausiuri AS haku_kausi,
      (SELECT t.koulutuksen_alkamisvuosi FROM gen.gen_hakutoive ht
        INNER JOIN gen.gen_hakukohde hk ON ht.hakukohde_oid = hk.hakukohde_oid
        INNER JOIN gen.gen_toteutus  t  ON hk.toteutus_oid   = t.toteutus_oid
        WHERE ht.hakemus_oid = hakemus.hakemus_oid
        ORDER BY ht.hakutoivenumero
        LIMIT 1) AS toteutus_vuosi,
      (SELECT t.koulutuksen_alkamiskausiuri FROM gen.gen_hakutoive ht
        INNER JOIN gen.gen_hakukohde hk ON ht.hakukohde_oid = hk.hakukohde_oid
        INNER JOIN gen.gen_toteutus  t  ON hk.toteutus_oid   = t.toteutus_oid
        WHERE ht.hakemus_oid = hakemus.hakemus_oid
        ORDER BY ht.hakutoivenumero
        LIMIT 1) AS toteutus_kausi,
      toinenaste_yhteishaku_hakemus_data.huoltaja1_etunimi,
      toinenaste_yhteishaku_hakemus_data.huoltaja1_sukunimi,
      toinenaste_yhteishaku_hakemus_data.huoltaja1_matkapuhelin,
      toinenaste_yhteishaku_hakemus_data.huoltaja1_email,
      toinenaste_yhteishaku_hakemus_data.huoltaja2_etunimi,
      toinenaste_yhteishaku_hakemus_data.huoltaja2_sukunimi,
      toinenaste_yhteishaku_hakemus_data.huoltaja2_matkapuhelin,
      toinenaste_yhteishaku_hakemus_data.huoltaja2_email,
      toinenaste_yhteishaku_hakemus_data.hakukohteet,
      toinenaste_yhteishaku_hakemus_data.kiinnostunut_urheilijan_ammatillisesta_koulutuksesta,
      toinenaste_yhteishaku_hakemus_data.urh_laji,
      toinenaste_yhteishaku_hakemus_data.urh_seura,
      toinenaste_yhteishaku_hakemus_data.urh_liitto,
      toinenaste_yhteishaku_hakemus_data.urh_sivulaji,
      toinenaste_yhteishaku_hakemus_data.urh_keskiarvo,
      toinenaste_yhteishaku_hakemus_data.urh_tamakausi,
      toinenaste_yhteishaku_hakemus_data.urh_peruskoulu,
      toinenaste_yhteishaku_hakemus_data.urh_viimekausi,
      toinenaste_yhteishaku_hakemus_data.urh_toissakausi,
      toinenaste_yhteishaku_hakemus_data.urh_valmentaja_puh,
      toinenaste_yhteishaku_hakemus_data.urh_valmentaja_nimi,
      toinenaste_yhteishaku_hakemus_data.urh_valmentaja_email,
      toinenaste_yhteishaku_hakemus_data.urh_valmennusryhma_maajoukkue,
      toinenaste_yhteishaku_hakemus_data.urh_valmennusryhma_piirijoukkue,
      toinenaste_yhteishaku_hakemus_data.urh_valmennusryhma_seurajoukkue,
      toinenaste_yhteishaku_hakemus_data.urh_amm_laji,
      toinenaste_yhteishaku_hakemus_data.urh_amm_seura,
      toinenaste_yhteishaku_hakemus_data.urh__amm_liitto,
      toinenaste_yhteishaku_hakemus_data.urh_amm_sivulaji,
      toinenaste_yhteishaku_hakemus_data.urh_amm_keskiarvo,
      toinenaste_yhteishaku_hakemus_data.urh_amm_tamakausi,
      toinenaste_yhteishaku_hakemus_data.urh_amm_peruskoulu,
      toinenaste_yhteishaku_hakemus_data.urh_amm_viimekausi,
      toinenaste_yhteishaku_hakemus_data.urh_amm_toissakausi,
      toinenaste_yhteishaku_hakemus_data.urh_amm_valmentaja_puh,
      toinenaste_yhteishaku_hakemus_data.urh_amm_valmentaja_nimi,
      toinenaste_yhteishaku_hakemus_data.urh_amm_valmentaja_email,
      toinenaste_yhteishaku_hakemus_data.urh_amm_valmennusryhma_maajoukkue,
      toinenaste_yhteishaku_hakemus_data.urh_amm_valmennusryhma_piirijoukkue,
      toinenaste_yhteishaku_hakemus_data.urh_amm_valmennusryhma_seurajoukkue
    FROM gen.gen_henkilo hlo
    INNER JOIN gen.gen_hakemus hakemus ON hakemus.henkilo_oid = hlo.henkilo_oid
    INNER JOIN gen.gen_haku    haku    ON hakemus.haku_oid    = haku.haku_oid
    LEFT JOIN gen.gen_hakemus_toinenaste_yhteishaku toinenaste_yhteishaku_hakemus_data ON toinenaste_yhteishaku_hakemus_data.hakemus_oid = hakemus.hakemus_oid
    WHERE haku.haku_oid = $hakuOid
    AND length(hakemus.hakemus_oid) = #$ataruOidLength
    AND haku.kohdejoukko_koodiuri LIKE 'haunkohdejoukko_11%'
    AND EXISTS (
      SELECT 1 FROM gen.gen_hakutoive ht
      INNER JOIN gen.gen_hakukohde hk ON ht.hakukohde_oid = hk.hakukohde_oid
      WHERE ht.hakemus_oid = hakemus.hakemus_oid
      #$hakuFilterSql
      #$stateSql
    )
    """.as[HakijaRow]

    LOG.debug(s"selectHakijatQuery: ${query.statements.head}")
    db.run(query, "selectHakijat")
  }

  def selectHakutoiveet(hakemusOids: Iterable[String]): Seq[HakijaHakutoiveRow] = {
    val query = sql"""
    SELECT
      ht.hakemus_oid,
      ht.hakukohde_oid,
      ht.hakutoivenumero,
      hk.jarjestyspaikka_oid,
      org.oppilaitosnumero,
      hk.hakukohteen_linja,
      hk.jarjestaa_urheilijan_ammkoulutusta,
      k.koulutukset_koodiuri,
      ht.valintatieto,
      ht.vastaanottotieto,
      ht.ilmoittautumisen_tila,
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
        LIMIT 1) AS pisteet
    FROM gen.gen_hakutoive ht
    LEFT JOIN gen.gen_hakukohde   hk  ON ht.hakukohde_oid    = hk.hakukohde_oid
    LEFT JOIN gen.gen_organisaatio org ON hk.organisaatio_oid = org.organisaatio_oid
    LEFT JOIN gen.gen_toteutus    t   ON hk.toteutus_oid     = t.toteutus_oid
    LEFT JOIN gen.gen_koulutus    k   ON t.koulutus_oid      = k.koulutus_oid
    WHERE ht.hakemus_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(hakemusOids)})
    """.as[HakijaHakutoiveRow]

    LOG.debug(s"selectHakutoiveetQuery: ${query.statements.head}")
    db.run(query, "selectHakijatHakutoiveet")
  }

  def selectLahtokoulut(hakemusOids: Iterable[String]): Seq[LahtokouluRow] = {
    if (hakemusOids.isEmpty) return Seq.empty
    // A person may hold multiple gen_henkilo rows sharing the same oppijanumero (master) but
    // different henkilo_oid (linked aliases). The hakemus's declared henkilo_oid and the
    // lahtokoulu row's henkilo_oid may be different aliases of the same person, so both must
    // be resolved through gen_henkilo.oppijanumero before matching.
    val query = sql"""
    SELECT hakemus.hakemus_oid,
      lk.oppilaitos_oid,
      COALESCE(org.nimi_fi, org.nimi_sv) AS lahtokoulunnimi,
      lk.luokka,
      lk.suoritus_tyyppi
    FROM gen.gen_hakemus hakemus
    INNER JOIN gen.gen_henkilo hlo_hak   ON hlo_hak.henkilo_oid   = hakemus.henkilo_oid
    INNER JOIN gen.gen_henkilo hlo_alias ON hlo_alias.oppijanumero = hlo_hak.oppijanumero
    INNER JOIN gen.gen_henkilo_lahtokoulu lk
      ON lk.henkilo_oid = hlo_alias.henkilo_oid
     AND lk.suorituksen_alku <= hakemus.jatetty
     AND (lk.suorituksen_loppu IS NULL OR hakemus.jatetty <= lk.suorituksen_loppu)
    LEFT JOIN gen.gen_organisaatio org ON org.organisaatio_oid = lk.oppilaitos_oid
    WHERE hakemus.hakemus_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(hakemusOids)})
      AND lk.suorituksen_alku = (
        SELECT MAX(lk2.suorituksen_alku)
        FROM gen.gen_henkilo_lahtokoulu lk2
        INNER JOIN gen.gen_henkilo hlo_alias2 ON hlo_alias2.henkilo_oid = lk2.henkilo_oid
        WHERE hlo_alias2.oppijanumero = hlo_hak.oppijanumero
          AND lk2.suorituksen_alku <= hakemus.jatetty
          AND (lk2.suorituksen_loppu IS NULL OR hakemus.jatetty <= lk2.suorituksen_loppu)
      )
    """.as[LahtokouluRow]

    LOG.debug(s"selectLahtokoulutQuery: ${query.statements.head}")
    db.run(query, "selectHakijatLahtokoulut")
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

  private def hakuFilterSqlFragment(
    hakukohdeOid: Option[String],
    organisaatioOid: Option[String]
  ): String =
    hakukohdeOid
      .map(hk => s" AND ht.hakukohde_oid = '$hk'")
      .orElse(organisaatioOid.map(org => s" AND hk.jarjestyspaikka_oid = '$org'"))
      .getOrElse("")

  private def stateSqlFragment(v: Valintarajaus): String = v match {
    case Valintarajaus.HAKENEET   => ""
    case Valintarajaus.HYVAKSYTYT =>
      " AND ht.valintatieto IN ('HYVAKSYTTY', 'HARKINNANVARAISESTI_HYVAKSYTTY', 'VARASIJALTA_HYVAKSYTTY')"
    case Valintarajaus.VASTAANOTTANEET =>
      " AND ht.vastaanottotieto = 'VASTAANOTTANUT_SITOVASTI'"
  }
}
