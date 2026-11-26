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

  // gen_hakemus_kasittelymerkinnat.requirement -arvot: sama taulu tarjoilee sekä
  // hakukelpoisuuden että maksuvelvollisuuden tilan, erotellaan tällä.
  private val eligibilityRequirement = "eligibility-state"
  private val paymentRequirement     = "payment-obligation"

  // Ohjausparametrit (gen_ohjausparametri_haku.avain), joilla valinta-tulos-service laskee vastaanoton
  // määräajan (VastaanottoUtils.laskeVastaanottoDeadline) ja varasijasääntöjen voimassaolon.
  private val vastaanottoPaattyyParametri         = "PH_OPVP"
  private val hakijakohtainenVastaanottoaikaParam = "PH_HPVOA"
  private val valintaesitysHyvaksyttavissaParam   = "PH_VEH"
  private val varasijasaannotVoimaanParametri     = "PH_VSSAV"
  private val kaikkiJonotSijoittelussaParametri   = "PH_VTSSV"
  // Haun sijoittelu ja hakutoiveiden priorisointi (Haku.sijoitteluJaPriorisointi) hakutoiveiden välisiin sääntöihin.
  private val sijoitteluParametri             = "sijoittelu"
  private val jarjestetytHakutoiveetParametri = "jarjestetytHakutoiveet"

  private val hyvaksytytHakemusmaksunTilatSql =
    RepositoryUtils.makeListOfValuesQueryStr(Seq("not-required", "paid", "ok-by-proxy"))

  private val hyvaksytytValinnanTilatSql =
    RepositoryUtils.makeListOfValuesQueryStr(Valintatila.hyvaksytyt.map(_.name))
  private val vastaanottaneetSql =
    RepositoryUtils.makeListOfValuesQueryStr(Vastaanottotila.vastaanottaneet.map(_.name))

  def selectKKHakijat(
    hakuOid: Option[String],
    hakukohdeOids: Seq[String],
    organisaatioOids: Seq[String],
    valintarajaus: Valintarajaus,
    oppijanumero: Option[String] = None
  ): Seq[KKHakijaRow] = {
    // Rajaus käytännössä pakollinen joko hakukohteilla, organisaatioOidilla tai oppijanumerolla
    if (hakukohdeOids.isEmpty && organisaatioOids.isEmpty && oppijanumero.isEmpty) {
      Seq.empty
    } else {
      val stateSql      = stateSqlFragment(valintarajaus)
      val hakuFilterSql = hakuFilterSqlFragment(hakukohdeOids, organisaatioOids)

      // hakuOid on ehdollinen (oppijanumerohaussa sitä ei tarvita).
      // Turvallisuus nojaa kontrollerin oid-validointiin (ParameterValidator).
      val hakuSql         = hakuOid.map(oid => s" AND haku.haku_oid = '$oid'").getOrElse("")
      val oppijanumeroSql = oppijanumeroSqlFragment(oppijanumero)

      val query = sql"""
          SELECT hlo.oppijanumero,
            hakemus.hakemus_oid,
            hakemus.sahkoposti,
            hakemus.puhelin,
            hakemus.lahiosoite,
            hakemus.postinumero,
            -- Atarun tapaan ulkomaan (tai puuttuvan) asuinmaan postitoimipaikka on kaupunki (city).
            CASE WHEN coalesce(hakemus.asuinmaa, '') <> '246' THEN hakemus.ulk_kunta
                 ELSE hakemus.postitoimipaikka END,
            haku.haku_oid,
            hakemus.etunimet,
            hakemus.kutsumanimi,
            hakemus.sukunimi,
            hakemus.hetu,
            -- Ataru olettaa puuttuvaksi asuinmaaksi Suomen.
            coalesce(hakemus.asuinmaa, '246'),
            hlo.kaikki_kansalaisuudet,
            hakemus.kotikunta,
            hakemus.sukupuoli,
            hakemus.asiointikieli,
            coalesce(hakemus.koulutusmarkkinointilupa, false),
            coalesce(hakemus.valintatuloksen_julkaisulupa, false),
            hakemus.jatetty,
            -- Suren tapaan viimeisin muokkaus on hakemuksen viimeisimmän version luontiaika.
            hakemus.luotu,
            hakemus.pohjakoulutus_kk,
            hlo.aidinkieli,
            hlo.syntymaaika,
            hlo.turvakielto,
            -- Supan valintadata kattaa vain sen omat haut. Muuten käytetään gen_hakutoiveen
            -- ensikertalaisuutta: vanhoissa hauissa (int_sure_haut) Suren oma tieto, muissa Supan
            -- ensikertalaisuustieto.
            coalesce(
              (SELECT bool_or(st.arvo IN ('true', '"true"')) FROM gen.gen_supa_tieto st
                WHERE st.hakemus_oid = hakemus.hakemus_oid
                  AND st.avain = 'ensikertalainen'
                  AND st.arvo IN ('true', '"true"', 'false', '"false"')),
              (SELECT bool_or(ht.ensikertalainen) FROM gen.gen_hakutoive ht
                WHERE ht.hakemus_oid = hakemus.hakemus_oid)
            ) AS ensikertalainen,
            haku.haku_vuosi,
            haku.haku_kausi,
            -- ONR:n master-henkilö; etusijajärjestys valitaan KKHakijaRow.asHakijassa.
            -- gen_henkilossa ei ole kutsumanimeä, joten se tulee aina hakemukselta.
            mhlo.henkilo_oid,
            mhlo.hetu,
            mhlo.etunimet,
            mhlo.sukunimi,
            mhlo.sukupuoli,
            mhlo.kaikki_kansalaisuudet,
            mhlo.aidinkieli,
            mhlo.syntymaaika,
            mhlo.turvakielto
          FROM gen.gen_henkilo hlo
          INNER JOIN gen.gen_hakemus hakemus ON hakemus.henkilo_oid = hlo.henkilo_oid
          INNER JOIN gen.gen_haku    haku    ON hakemus.haku_oid    = haku.haku_oid
          LEFT JOIN gen.gen_henkilo  mhlo    ON mhlo.henkilo_oid    = hlo.oppijanumero
          WHERE length(hakemus.hakemus_oid) = #$ataruOidLength
          AND haku.kohdejoukko_koodiuri LIKE 'haunkohdejoukko_12%'
          -- Suren kkhakijat-rajapinta ei palauttanut passivoituja hakemuksia (Atarussa
          -- ar.state <> 'inactivated').
          AND NOT coalesce(hakemus.poistettu, false)
          -- Ataru ei toimittanut Surellekaan kk-hakemuksia, joiden mahdollista hakemusmaksua ei ole maksettu
          -- (approved_at puuttuu). Ei maksutietoa (ei maksullinen haku) = mukana.
          AND (hakemus.hakemusmaksun_tila IS NULL
               OR hakemus.hakemusmaksun_tila IN (#$hyvaksytytHakemusmaksunTilatSql))
          #$hakuSql
          #$oppijanumeroSql
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
  }

  def selectHakemukset(hakemusOids: Iterable[String]): Seq[KKHakemusRow] = {
    if (hakemusOids.isEmpty) {
      Seq.empty
    } else {
      val hakemusOidList = RepositoryUtils.makeListOfValuesQueryStr(hakemusOids)
      // Valinnan tulos lasketaan valinta-tulos-servicen tapaan (ks. KKHakemusRow.vtsTila):
      //   merkitseva_jono_yleinen: hakutoiveen merkitsevä jono (JonoFinder.merkitseväJono): tilan
      //                            järjestys, VARALLA-jonoissa varasijanumero, muuten valintatapajonon
      //                            prioriteetti (ei jonosijan prioriteetti, joka on hakutoiveen numero).
      //                            Kaikki jonokohtaiset tiedot tulevat siitä.
      //   jonojen_julkaisu:        onko hakutoiveen jokainen jono julkaistavissa.
      val query = sql"""
          WITH merkitseva_jono_yleinen AS (
            SELECT hakemus_oid, hakukohde_oid, valinnan_tila, hyvaksyttyjajulkaistu, valintatapajono_id,
                   julkaistavissa, hyvaksytty_harkinnanvaraisesti, pisteet, ehdollisesti_hyvaksyttavissa,
                   ehdollisen_hyvaksymisen_ehto_fi, ehdollisen_hyvaksymisen_ehto_sv,
                   ehdollisen_hyvaksymisen_ehto_en, ei_varasijatayttoa
            FROM (
              SELECT vr.hakemus_oid, vr.hakukohde_oid, vr.valinnan_tila, vr.hyvaksyttyjajulkaistu,
                     vr.valintatapajono_id, vr.julkaistavissa, vr.hyvaksytty_harkinnanvaraisesti,
                     vr.pisteet, vr.ehdollisesti_hyvaksyttavissa,
                     vr.ehdollisen_hyvaksymisen_ehto_fi, vr.ehdollisen_hyvaksymisen_ehto_sv,
                     vr.ehdollisen_hyvaksymisen_ehto_en, vrj.ei_varasijatayttoa,
                ROW_NUMBER() OVER (
                  PARTITION BY vr.hakemus_oid, vr.hakukohde_oid
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
                      ELSE vrj.prioriteetti
                    END NULLS LAST
                ) AS rn
              FROM gen.gen_valintarekisteri vr
              LEFT JOIN gen.gen_valintarekisteri_valintatapajono vrj
                ON vrj.valintatapajono_id = vr.valintatapajono_id
              WHERE vr.valinnan_tila IS NOT NULL
                AND vr.hakemus_oid IN (#$hakemusOidList)
            ) x WHERE rn = 1
          ),
          jonojen_julkaisu AS (
            SELECT vr.hakemus_oid, vr.hakukohde_oid,
                   bool_and(coalesce(vr.julkaistavissa, false)) AS kaikki_jonot_julkaistu
            FROM gen.gen_valintarekisteri vr
            WHERE vr.hakemus_oid IN (#$hakemusOidList)
            GROUP BY vr.hakemus_oid, vr.hakukohde_oid
          )
          SELECT
            ht.hakemus_oid,
            ht.hakukohde_oid,
            ht.hakutoivenumero,
            hk.organisaatio_oid,
            hk.jarjestyspaikka_oid,
            ht.vastaanottotieto,
            ht.ilmoittautumisen_tila,
            (SELECT vr.maksun_tila FROM gen.gen_valintarekisteri vr
              WHERE vr.hakemus_oid   = ht.hakemus_oid
                AND vr.hakukohde_oid = ht.hakukohde_oid
                AND vr.maksun_tila IS NOT NULL
                AND vr.maksun_tila <> ''
              LIMIT 1) AS lukuvuosimaksu,
            k.ulkoinen_tunniste AS hakukohde_kk_id,
            k.koulutus_oid,
            k.koulutukset_koodiuri,
            k.johtaa_tutkintoon,
            t.koulutuksen_alkamisvuosi AS koulutus_alkamisvuosi,
            t.koulutuksen_alkamiskausiuri AS koulutus_alkamiskausi_uri,
            merk_y.valinnan_tila                   AS vr_valinnan_tila,
            merk_y.hyvaksyttyjajulkaistu           AS vr_valinnan_aikaleima,
            merk_y.pisteet                         AS pisteet,
            merk_y.ehdollisesti_hyvaksyttavissa    AS ehdollisesti_hyvaksyttavissa,
            merk_y.ehdollisen_hyvaksymisen_ehto_fi AS ehto_fi,
            merk_y.ehdollisen_hyvaksymisen_ehto_sv AS ehto_sv,
            merk_y.ehdollisen_hyvaksymisen_ehto_en AS ehto_en,
            vp.valintatapajono_tyyppi          AS valintatapajono_tyyppi,
            vp.valintatapajono_nimi            AS valintatapajono_nimi,
            vtj.nimi_fi                        AS valintatapajono_tyyppi_nimi_fi,
            km_kelp.state                      AS h_kelpoisuus,
            km_maksu.state                     AS h_kelpoisuus_maksuvelvollisuus,
            merk_y.julkaistavissa                  AS merkitseva_jono_julkaistavissa,
            merk_y.hyvaksytty_harkinnanvaraisesti  AS hyvaksytty_harkinnanvaraisesti,
            merk_y.ei_varasijatayttoa              AS ei_varasijatayttoa,
            jj.kaikki_jonot_julkaistu              AS kaikki_jonot_julkaistu,
            op_opvp.aikaleima                      AS vastaanotto_paattyy,
            op_hpvoa.arvo                          AS hakijakohtainen_vastaanottoaika,
            op_veh.aikaleima                       AS valintaesitys_hyvaksyttavissa,
            op_vssav.aikaleima                     AS varasijasaannot_voimaan,
            op_vtssv.aikaleima                     AS kaikki_jonot_sijoittelussa,
            -- Totuusarvoparametrit ovat alkuperainen_arvo-sarakkeessa (arvo on vain {"value": ...}-muotoisille).
            -- VTS tulkitsee puuttuvan tai muun kuin totuusarvon epätodeksi.
            coalesce(CAST(op_sijoittelu.alkuperainen_arvo AS text) = 'true', false)  AS sijoittelu,
            coalesce(CAST(op_jarjestetyt.alkuperainen_arvo AS text) = 'true', false) AS jarjestetyt_hakutoiveet,
            -- Yhden paikan sääntö ja koulutuksen alkamiskausi (getYpsHakukohde) samassa järjestyksessä kuin
            -- selectYpsVastaanototissa.
            hu.tila                           AS haku_tila,
            hk.tila                           AS hakukohde_tila,
            hu.kohdejoukon_tarkenne_koodiuri  AS kohdejoukon_tarkenne,
            k.koulutustyyppi                  AS koulutustyyppi,
            k.johtaa_tutkintoon               AS yps_johtaa_tutkintoon,
            hk.koulutuksen_alkamiskausiuri    AS hakukohde_alkamiskausi_uri,
            hk.koulutuksen_alkamisvuosi       AS hakukohde_alkamisvuosi,
            hk.koulutuksen_alkamispaivamaara  AS hakukohde_alkamisaika,
            hu.koulutuksen_alkamiskausiuri    AS haku_alkamiskausi_uri,
            hu.koulutuksen_alkamisvuosi       AS haku_alkamisvuosi,
            hu.koulutuksen_tarkka_alkamisaika AS haku_alkamisaika,
            t.koulutuksen_alkamiskausiuri     AS toteutus_alkamiskausi_uri,
            t.koulutuksen_alkamisvuosi        AS toteutus_alkamisvuosi,
            t.koulutuksen_tarkka_alkamisaika  AS toteutus_alkamisaika
          FROM gen.gen_hakutoive ht
          LEFT JOIN gen.gen_hakukohde hk ON ht.hakukohde_oid = hk.hakukohde_oid
          LEFT JOIN gen.gen_toteutus  t  ON hk.toteutus_oid  = t.toteutus_oid
          LEFT JOIN gen.gen_koulutus  k  ON t.koulutus_oid   = k.koulutus_oid
          LEFT JOIN merkitseva_jono_yleinen merk_y
            ON merk_y.hakemus_oid = ht.hakemus_oid AND merk_y.hakukohde_oid = ht.hakukohde_oid
          LEFT JOIN jonojen_julkaisu jj
            ON jj.hakemus_oid = ht.hakemus_oid AND jj.hakukohde_oid = ht.hakukohde_oid
          -- Haun ohjausparametrit vastaanoton määräaikaa ja varasijasääntöjä varten (ks. KKHakemusRow.vtsTila).
          LEFT JOIN gen.gen_hakemus hm ON hm.hakemus_oid = ht.hakemus_oid
          -- Haku yhden paikan sääntöä ja koulutuksen alkamiskautta varten.
          LEFT JOIN gen.gen_haku    hu ON hu.haku_oid    = hm.haku_oid
          LEFT JOIN gen.gen_ohjausparametri_haku op_opvp
            ON op_opvp.haku_oid = hm.haku_oid AND op_opvp.avain = '#$vastaanottoPaattyyParametri'
          LEFT JOIN gen.gen_ohjausparametri_haku op_hpvoa
            ON op_hpvoa.haku_oid = hm.haku_oid AND op_hpvoa.avain = '#$hakijakohtainenVastaanottoaikaParam'
          LEFT JOIN gen.gen_ohjausparametri_haku op_veh
            ON op_veh.haku_oid = hm.haku_oid AND op_veh.avain = '#$valintaesitysHyvaksyttavissaParam'
          LEFT JOIN gen.gen_ohjausparametri_haku op_vssav
            ON op_vssav.haku_oid = hm.haku_oid AND op_vssav.avain = '#$varasijasaannotVoimaanParametri'
          LEFT JOIN gen.gen_ohjausparametri_haku op_vtssv
            ON op_vtssv.haku_oid = hm.haku_oid AND op_vtssv.avain = '#$kaikkiJonotSijoittelussaParametri'
          LEFT JOIN gen.gen_ohjausparametri_haku op_sijoittelu
            ON op_sijoittelu.haku_oid = hm.haku_oid AND op_sijoittelu.avain = '#$sijoitteluParametri'
          LEFT JOIN gen.gen_ohjausparametri_haku op_jarjestetyt
            ON op_jarjestetyt.haku_oid = hm.haku_oid AND op_jarjestetyt.avain = '#$jarjestetytHakutoiveetParametri'
          -- Suren tapaan jono on hakutoiveen merkitsevä jono julkaisusta riippumatta; tyyppi
          -- valintatapajono-koodiston suomenkielisenä nimenä.
          LEFT JOIN gen.gen_valintaperuste_valintatapajono vp
            ON vp.valintatapajono_id = merk_y.valintatapajono_id
          LEFT JOIN pub.pub_dim_koodisto_valintatapajono vtj
            ON vtj.koodiuri = vp.valintatapajono_tyyppi
          -- Käsittelymerkinnät ovat samalla tarkkuudella (hakemus, hakukohde) kuin hakutoive, ja
          -- niitä on 0 tai 1 per tyyppi -- joten suora liitos riittää, ei monista rivejä.
          -- requirement-rajaus kuuluu liitosehtoon eikä WHEREen: WHEREssä se muuttaisi
          -- ulkoliitoksen sisäliitokseksi ja pudottaisi merkinnättömät hakutoiveet pois.
          LEFT JOIN gen.gen_hakemus_kasittelymerkinnat km_kelp
            ON km_kelp.hakemus_oid   = ht.hakemus_oid
           AND km_kelp.hakukohde_oid = ht.hakukohde_oid
           AND km_kelp.requirement   = '#$eligibilityRequirement'
          LEFT JOIN gen.gen_hakemus_kasittelymerkinnat km_maksu
            ON km_maksu.hakemus_oid   = ht.hakemus_oid
           AND km_maksu.hakukohde_oid = ht.hakukohde_oid
           AND km_maksu.requirement   = '#$paymentRequirement'
          WHERE ht.hakemus_oid IN (#$hakemusOidList)
          -- Suren tapaan hakutoiveet hakutoivejärjestyksessä. Ilman järjestystä PostgreSQL voi palauttaa ne
          -- missä järjestyksessä tahansa.
          ORDER BY ht.hakemus_oid, ht.hakutoivenumero, ht.hakukohde_oid
          """.as[KKHakemusRow]

      LOG.debug(s"selectKKHakemuksetQuery: ${query.statements.head}")
      db.run(query, "selectKKHakemukset")
    }
  }

  /**
   * Hakemusten hakijoiden sitovat ja ehdolliset vastaanotot kaikissa hauissa yhden paikan sääntöä varten
   * (VTS:n newest_vastaanotot). Hakijan kaikki henkilö-oidit, joilla on sama oppijanumero, ovat sama henkilö
   * (VTS:n henkiloviitteet); aliaksiin kuuluu myös hakemuksen oma henkilö. Hakemukset tulevat selectKKHakijat kautta,
   * joka vaatii hakemuksen henkilölle gen_henkilo-rivin. Mukana ovat myös hakemuksen omat hakutoiveet, kuten
   * VTS:ssä. Vastaanotot tulevat gen_hakutoiveelta, joten passivoitujen hakemusten ja muulle kuin hakemusten
   * henkilö-oidille kirjatut vastaanotot puuttuvat.
   */
  def selectYpsVastaanotot(hakemusOids: Iterable[String]): Seq[YpsVastaanottoRow] = {
    if (hakemusOids.isEmpty) {
      Seq.empty
    } else {
      val hakemusOidList = RepositoryUtils.makeListOfValuesQueryStr(hakemusOids)
      val query          = sql"""
          SELECT DISTINCT h.hakemus_oid, ht.hakukohde_oid,
            -- Yhden paikan sääntö ja koulutuksen alkamiskausi (getYpsHakukohde) samassa järjestyksessä kuin
            -- selectHakemukset-kyselyssä.
            hu.tila                           AS haku_tila,
            hk.tila                           AS hakukohde_tila,
            hu.kohdejoukon_tarkenne_koodiuri  AS kohdejoukon_tarkenne,
            k.koulutustyyppi                  AS koulutustyyppi,
            k.johtaa_tutkintoon               AS yps_johtaa_tutkintoon,
            hk.koulutuksen_alkamiskausiuri    AS hakukohde_alkamiskausi_uri,
            hk.koulutuksen_alkamisvuosi       AS hakukohde_alkamisvuosi,
            hk.koulutuksen_alkamispaivamaara  AS hakukohde_alkamisaika,
            hu.koulutuksen_alkamiskausiuri    AS haku_alkamiskausi_uri,
            hu.koulutuksen_alkamisvuosi       AS haku_alkamisvuosi,
            hu.koulutuksen_tarkka_alkamisaika AS haku_alkamisaika,
            t.koulutuksen_alkamiskausiuri     AS toteutus_alkamiskausi_uri,
            t.koulutuksen_alkamisvuosi        AS toteutus_alkamisvuosi,
            t.koulutuksen_tarkka_alkamisaika  AS toteutus_alkamisaika
          FROM gen.gen_hakemus h
          INNER JOIN gen.gen_henkilo   hlo   ON hlo.henkilo_oid    = h.henkilo_oid
          INNER JOIN gen.gen_henkilo   alias ON alias.oppijanumero = hlo.oppijanumero
          INNER JOIN gen.gen_hakemus   muu   ON muu.henkilo_oid    = alias.henkilo_oid
          INNER JOIN gen.gen_hakutoive ht    ON ht.hakemus_oid     = muu.hakemus_oid
          LEFT JOIN gen.gen_hakukohde hk ON hk.hakukohde_oid = ht.hakukohde_oid
          LEFT JOIN gen.gen_haku      hu ON hu.haku_oid      = muu.haku_oid
          LEFT JOIN gen.gen_toteutus  t  ON t.toteutus_oid   = hk.toteutus_oid
          LEFT JOIN gen.gen_koulutus  k  ON k.koulutus_oid   = t.koulutus_oid
          WHERE h.hakemus_oid IN (#$hakemusOidList)
            AND ht.vastaanottotieto IN (#$vastaanottaneetSql)
          """.as[YpsVastaanottoRow]

      LOG.debug(s"selectYpsVastaanototQuery: ${query.statements.head}")
      db.run(query, "selectYpsVastaanotot")
    }
  }

  def selectYlioppilaat(hakemusOids: Set[String]): Seq[YlioppilasRow] = {
    if (hakemusOids.isEmpty) {
      Seq.empty
    } else {
      // A person may hold multiple gen_henkilo rows sharing the same oppijanumero (master)
      // but different henkilo_oid (linked aliases). Ylioppilas data may be attached to any
      // alias, so we resolve through gen_henkilo.oppijanumero before matching. GROUP BY +
      // BOOL_OR/MAX collapses the fan-out across aliases into one row per hakemus.
      val query = sql"""
      SELECT hakemus.hakemus_oid,
        BOOL_OR(yo.on_ylioppilas)  AS on_ylioppilas,
        MAX(yo.valmistumis_vuosi)  AS valmistumis_vuosi
      FROM gen.gen_hakemus hakemus
      INNER JOIN gen.gen_henkilo hlo_hak   ON hlo_hak.henkilo_oid    = hakemus.henkilo_oid
      INNER JOIN gen.gen_henkilo hlo_alias ON hlo_alias.oppijanumero = hlo_hak.oppijanumero
      INNER JOIN gen.gen_ylioppilas yo     ON yo.henkilo_oid = hlo_alias.henkilo_oid
      WHERE hakemus.hakemus_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(hakemusOids)})
      GROUP BY hakemus.hakemus_oid
      """.as[YlioppilasRow]

      LOG.debug(s"selectKKYlioppilaatQuery: ${query.statements.head}")
      db.run(query, "selectKKYlioppilaat")
    }
  }

  /**
   * Parametriksi kelpaa mikä tahansa henkilön oideista: joko oppijanumero (master) tai minkä
   * tahansa siihen linkitetyn aliaksen henkilo_oid. Annettu oid ratkaistaan siksi ensin
   * masteriksi, ja vasta sillä rajataan -- näin kaikki aliakset ja niille kirjatut hakemukset
   * tulevat mukaan riippumatta siitä, minkä oidin kutsuja antoi. Samalla henkilöllä voi olla
   * useita gen_henkilo-rivejä samalla oppijanumerolla mutta eri henkilo_oidilla, ks. myös
   * selectYlioppilaat.
   *
   * IN (SELECT ...) eikä liitos: liitos gen_henkiloon monistaisi rivit aliasten määrällä ja
   * vaatisi DISTINCTin. Molemmat disjunktit: henkilo_oid-osuma kattaa aliaksen, oppijanumero-osuma
   * masterin myös siinä tapauksessa ettei masterin omaa riviä löydy henkilo_oidina.
   */
  private def oppijanumeroSqlFragment(oppijanumero: Option[String]): String =
    oppijanumero
      .map(oid => s"""
          AND hlo.oppijanumero IN (
            SELECT hlo_pyydetty.oppijanumero FROM gen.gen_henkilo hlo_pyydetty
            WHERE hlo_pyydetty.henkilo_oid = '$oid' OR hlo_pyydetty.oppijanumero = '$oid'
          )""")
      .getOrElse("")

  private def hakuFilterSqlFragment(
    hakukohdeOids: Seq[String],
    organisaatioOids: Seq[String]
  ): String =
    Seq(
      Option.when(hakukohdeOids.nonEmpty)(
        s" AND ht.hakukohde_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(hakukohdeOids)})"
      ),
      Option.when(organisaatioOids.nonEmpty)(
        s" AND hk.jarjestyspaikka_oid IN (${RepositoryUtils.makeListOfValuesQueryStr(organisaatioOids)})"
      )
    ).flatten.mkString

  /**
   * Valintarajauksen esirajaus: kaikki hakutoiveet, joiden lopullinen tila voi täsmätä rajaukseen. Varsinainen
   * rajaus tehdään palvelussa lasketulla tilalla (ExternalKKHakijatService.matchesFilters). Valinta-tulos-servicen
   * säännöt tuottavat hyväksytyn tilan vain tallennetusta hyväksytystä jonon tilasta tai vastaanotosta ja
   * vastaanoton vain tallennetusta vastaanotosta; muut säännöt vain laskevat tilaa.
   */
  private def stateSqlFragment(v: Valintarajaus): String = v match {
    case Valintarajaus.HAKENEET   => ""
    case Valintarajaus.HYVAKSYTYT =>
      s"""
            AND (ht.vastaanottotieto IN ($vastaanottaneetSql)
                 OR EXISTS (SELECT 1 FROM gen.gen_valintarekisteri vr
                            WHERE vr.hakemus_oid   = ht.hakemus_oid
                              AND vr.hakukohde_oid = ht.hakukohde_oid
                              AND vr.valinnan_tila IN ($hyvaksytytValinnanTilatSql)))"""
    case Valintarajaus.VASTAANOTTANEET =>
      s" AND ht.vastaanottotieto IN ($vastaanottaneetSql)"
  }
}
