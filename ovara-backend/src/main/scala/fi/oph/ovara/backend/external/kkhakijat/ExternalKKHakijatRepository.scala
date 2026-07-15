package fi.oph.ovara.backend.external.kkhakijat

import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Repository
import slick.jdbc.PostgresProfile.api.actionBasedSQLInterpolation

@Repository
class ExternalKKHakijatRepository(db: ReadOnlyDatabase) extends KKHakijatExtractors {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ExternalKKHakijatRepository])

  private val ataruOidLength = 35

  def selectKKHakijat(
    hakuOid: String,
    hakukohdeOid: Option[String],
    organisaatioOid: Option[String],
    valintarajaus: Valintarajaus
  ): Seq[KKHakijaRow] = {
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
      hakemus.asiointikieli,
      hakemus.koulutusmarkkinointilupa,
      hakemus.valintatuloksen_julkaisulupa,
      hakemus.jatetty,
      hakemus.muokattu,
      hlo.aidinkieli,
      COALESCE(
        (SELECT true FROM gen.gen_ylioppilas yo
          INNER JOIN gen.gen_henkilo hlo_alias ON hlo_alias.henkilo_oid = yo.henkilo_oid
          WHERE hlo_alias.oppijanumero = hlo.oppijanumero
            AND yo.on_ylioppilas = true
          LIMIT 1),
        false) AS on_ylioppilas,
      (SELECT yo.valmistumis_vuosi FROM gen.gen_ylioppilas yo
        INNER JOIN gen.gen_henkilo hlo_alias ON hlo_alias.henkilo_oid = yo.henkilo_oid
        WHERE hlo_alias.oppijanumero = hlo.oppijanumero
          AND yo.valmistumis_vuosi IS NOT NULL
        LIMIT 1) AS yo_valmistumis_vuosi,
      (SELECT true FROM gen.gen_supa_tieto st
        WHERE st.hakemus_oid = hakemus.hakemus_oid
          AND st.avain = 'ensikertalainen'
          AND st.arvo IN ('true', '"true"')
        LIMIT 1) AS ensikertalainen,
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
        LIMIT 1) AS toteutus_kausi
    FROM gen.gen_henkilo hlo
    INNER JOIN gen.gen_hakemus hakemus ON hakemus.henkilo_oid = hlo.henkilo_oid
    INNER JOIN gen.gen_haku    haku    ON hakemus.haku_oid    = haku.haku_oid
    WHERE haku.haku_oid = $hakuOid
    AND length(hakemus.hakemus_oid) = #$ataruOidLength
    AND haku.kohdejoukko_koodiuri LIKE 'haunkohdejoukko_12%'
    AND EXISTS (
      SELECT 1 FROM gen.gen_hakutoive ht
      INNER JOIN gen.gen_hakukohde hk ON ht.hakukohde_oid = hk.hakukohde_oid
      WHERE ht.hakemus_oid = hakemus.hakemus_oid
      #$hakuFilterSql
      #$stateSql
    )
    """.as[KKHakijaRow]

    LOG.debug(s"selectHakijatQuery: ${query.statements.head}")
    db.run(query, "selectKKHakijat")
  }

  def selectHakemukset(hakemusOids: Iterable[String]): Seq[KKHakemusRow] = {
    val query = sql"""
    SELECT
      ht.hakemus_oid,
      ht.hakukohde_oid,
      ht.hakutoivenumero,
      hk.organisaatio_oid,
      hk.jarjestyspaikka_oid,
      ht.valintatieto,
      ht.vastaanottotieto,
      ht.ilmoittautumisen_tila,
      (SELECT vr.maksun_tila FROM gen.gen_valintarekisteri vr
        WHERE vr.hakemus_oid   = ht.hakemus_oid
          AND vr.hakukohde_oid = ht.hakukohde_oid
          AND vr.maksun_tila IS NOT NULL
          AND vr.maksun_tila <> ''
        LIMIT 1) AS lukuvuosimaksu,
      k.ulkoinen_tunniste AS hakukohde_kk_id
    FROM gen.gen_hakutoive ht
    LEFT JOIN gen.gen_hakukohde hk ON ht.hakukohde_oid = hk.hakukohde_oid
    LEFT JOIN gen.gen_toteutus  t  ON hk.toteutus_oid  = t.toteutus_oid
    LEFT JOIN gen.gen_koulutus  k  ON t.koulutus_oid   = k.koulutus_oid
    WHERE ht.hakemus_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(hakemusOids)})
    """.as[KKHakemusRow]

    LOG.debug(s"selectKKHakemuksetQuery: ${query.statements.head}")
    db.run(query, "selectKKHakemukset")
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
