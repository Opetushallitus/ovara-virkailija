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
    organisaatioOid: Option[String]
  ): Seq[HakijaRow] = {
    val query = (hakukohdeOid, organisaatioOid) match {
      case (Some(hk), _) =>
        sql"""
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
          WHERE ht.hakemus_oid = hakemus.hakemus_oid
          AND ht.hakukohde_oid = $hk
        )
        """.as[HakijaRow]
      case (_, Some(org)) =>
        sql"""
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
          AND hk.jarjestyspaikka_oid = $org
        )
        """.as[HakijaRow]
      case _ =>
        // Controller-level validation guarantees this branch is unreachable.
        return Seq.empty
    }

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
      ht.ilmoittautumisen_tila
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
}
