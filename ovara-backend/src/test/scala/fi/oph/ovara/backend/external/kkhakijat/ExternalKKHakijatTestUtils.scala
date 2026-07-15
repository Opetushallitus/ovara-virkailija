package fi.oph.ovara.backend.external.kkhakijat

import fi.oph.ovara.backend.external.kkhakijat.ExternalKKHakijatTestData.*
import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import slick.jdbc.H2Profile.api.*

import java.time.format.DateTimeFormatter

trait ExternalKKHakijatTestUtils {
  private val SECONDS_TZ_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

  val db: ReadOnlyDatabase

  /** Minimal fixture: init schema + a hakija with default hakemus/hakukohde/hakutoive. */
  def seedMinimalHakija(): Unit = {
    initSchema()
    insertHakemus()
    insertHakukohde()
    insertHakutoive()
  }

  def insertHenkilo(
    oppijanumero: String = OPPIJANUMERO,
    aidinkieli: Option[String] = Some(AIDINKIELI_RAW)
  ): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_henkilo VALUES ($oppijanumero, $oppijanumero, $aidinkieli)""",
      "Insert test kk-henkilö"
    )
  }

  /** Insert an additional gen_henkilo row that links `henkiloOid` to the same master `oppijanumero`. */
  def insertHenkiloAlias(oppijanumero: String, henkiloOid: String): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_henkilo VALUES ($oppijanumero, $henkiloOid, NULL)""",
      "Insert test kk-henkilö alias"
    )
  }

  def insertSupaTieto(
    hakemusOid: String = HAKEMUS_OID,
    avain: String,
    arvo: Option[String]
  ): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_supa_tieto VALUES($hakemusOid, $avain, $arvo)""",
      "Insert test kk-supa_tieto"
    )
  }

  def insertYlioppilas(
    henkiloOid: String = OPPIJANUMERO,
    onYlioppilas: Boolean = true,
    valmistumisVuosi: Option[Int] = None
  ): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_ylioppilas VALUES ($henkiloOid, $onYlioppilas, $valmistumisVuosi)""",
      "Insert test kk-ylioppilas"
    )
  }

  def insertHaku(
    hakuOid: String = HAKU_OID,
    kohdejoukkoKoodiuri: String = "haunkohdejoukko_12#1",
    koulutuksenAlkamisvuosi: Option[Int] = None,
    koulutuksenAlkamiskausiuri: Option[String] = None
  ): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_haku VALUES(
          $hakuOid,
          'hakutapa_01#1',
          'Yhteishaku',
          'Gemensamma',
          'Joint application',
          $kohdejoukkoKoodiuri,
          $koulutuksenAlkamisvuosi,
          $koulutuksenAlkamiskausiuri
          )""",
      "Insert test kk-haku"
    )
  }

  def insertHakemus(
    oppijanumero: String = OPPIJANUMERO,
    hakemusOid: String = HAKEMUS_OID,
    hakuOid: String = HAKU_OID,
    insertHenkilo: Boolean = true,
    insertHaku: Boolean = true,
    etunimet: Option[String] = Some(ETUNIMET),
    kutsumanimi: Option[String] = Some(KUTSUMANIMI),
    sukunimi: Option[String] = Some(SUKUNIMI),
    hetu: Option[String] = Some(HETU),
    kotikunta: Option[String] = Some(KOTIKUNTA),
    sukupuoli: Option[Int] = Some(SUKUPUOLI),
    asiointikieli: Option[Int] = None,
    kansalaisuusJson: Option[String] = Some(KANSALAISUUS_JSON),
    koulutusmarkkinointilupa: Option[Boolean] = Some(KOULUTUSMARKKINOINTILUPA),
    kiinnostunutOppisopimuksesta: Option[Boolean] = Some(KIINNOSTUNUT_OPPISOPIMUKSESTA),
    sahkoinenviestintalupa: Option[Boolean] = Some(SAHKOINENVIESTINTALUPA),
    valintatuloksenJulkaisulupa: Option[Boolean] = Some(VALINTATULOKSEN_JULKAISULUPA),
    jatetty: Option[java.time.OffsetDateTime] = Some(JATETTY),
    muokattu: Option[java.time.OffsetDateTime] = Some(MUOKATTU)
  ): Unit = {
    if (insertHenkilo) this.insertHenkilo(oppijanumero)
    if (insertHaku) this.insertHaku(hakuOid)

    val jatettyStr  = jatetty.map(_.format(SECONDS_TZ_FORMATTER))
    val muokattuStr = muokattu.map(_.format(SECONDS_TZ_FORMATTER))

    db.run(
      sqlu"""INSERT INTO gen.gen_hakemus VALUES(
          $hakemusOid,
          $hakuOid,
          $oppijanumero,
          $LAHIOSOITE,
          $POSTINUMERO,
          $HELSINKI,
          $SUOMI_KOODI,
          $EMAIL,
          $MATKAPUHELIN,
          $etunimet,
          $kutsumanimi,
          $sukunimi,
          $hetu,
          $kotikunta,
          $sukupuoli,
          $asiointikieli,
          $kansalaisuusJson,
          $koulutusmarkkinointilupa,
          $kiinnostunutOppisopimuksesta,
          $sahkoinenviestintalupa,
          $valintatuloksenJulkaisulupa,
          $jatettyStr,
          $muokattuStr)""",
      "Insert test kk-hakemus"
    )
  }

  def insertHakukohde(
    hakukohdeOid: String = HAKUKOHDE_OID,
    toteutusOid: String = TOTEUTUS_OID,
    jarjestyspaikkaOid: String = ORGANISAATIO_OID,
    organisaatioOid: Option[String] = Some(ORGANISAATIO_OID),
    koulutuksenAlkamisvuosi: Option[Int] = Some(KOULUTUKSEN_ALKAMISVUOSI),
    koulutuksenAlkamiskausiuri: Option[String] = Some(KOULUTUKSEN_ALKAMISKAUSIURI)
  ): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_hakukohde VALUES(
          $hakukohdeOid,
          $toteutusOid,
          $jarjestyspaikkaOid,
          $organisaatioOid,
          $koulutuksenAlkamisvuosi,
          $koulutuksenAlkamiskausiuri)""",
      "Insert test kk-hakukohde"
    )
  }

  def insertValintarekisteri(
    hakemusOid: String = HAKEMUS_OID,
    hakukohdeOid: String = HAKUKOHDE_OID,
    valintatapajonoId: String = "vtj-1",
    maksunTila: Option[String] = None,
    valinnanTila: Option[String] = None,
    hyvaksyttyjajulkaistu: Option[java.time.OffsetDateTime] = None,
    julkaistavissa: Option[Boolean] = Some(true),
    varasijanNumero: Option[Int] = None,
    prioriteetti: Option[Int] = None,
    pisteet: Option[BigDecimal] = None
  ): Unit = {
    val hyvaksyttyStr = hyvaksyttyjajulkaistu.map(_.format(SECONDS_TZ_FORMATTER))
    db.run(
      sqlu"""INSERT INTO gen.gen_valintarekisteri (
          hakukohde_oid, valintatapajono_id, hakemus_oid, maksun_tila,
          valinnan_tila, hyvaksyttyjajulkaistu,
          julkaistavissa, varasijan_numero, prioriteetti, pisteet
        ) VALUES (
          $hakukohdeOid, $valintatapajonoId, $hakemusOid, $maksunTila,
          $valinnanTila, $hyvaksyttyStr,
          $julkaistavissa, $varasijanNumero, $prioriteetti, ${pisteet.map(_.doubleValue)}
        )""",
      "Insert test kk-valintarekisteri"
    )
  }

  def insertToteutus(
    toteutusOid: String = TOTEUTUS_OID,
    koulutusOid: String = KOULUTUS_OID,
    koulutuksenAlkamisvuosi: Option[Int] = None,
    koulutuksenAlkamiskausiuri: Option[String] = None
  ): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_toteutus VALUES(
          $toteutusOid,
          $koulutusOid,
          $koulutuksenAlkamisvuosi,
          $koulutuksenAlkamiskausiuri)""",
      "Insert test kk-toteutus"
    )
  }

  def insertKoulutus(
    koulutusOid: String = KOULUTUS_OID,
    ulkoinenTunniste: Option[String] = None
  ): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_koulutus VALUES($koulutusOid, $ulkoinenTunniste)""",
      "Insert test kk-koulutus"
    )
  }

  def insertHakutoive(
    hakemusOid: String = HAKEMUS_OID,
    hakukohdeOid: String = HAKUKOHDE_OID,
    hakutoivenumero: Int = 1,
    valintatieto: Option[String] = Some(VALINTATIETO),
    vastaanottotieto: Option[String] = Some(VASTAANOTTOTIETO),
    ilmoittautumisenTila: Option[String] = Some(ILMOITTAUTUMISEN_TILA)
  ): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_hakutoive VALUES(
          $hakemusOid,
          $hakukohdeOid,
          $hakutoivenumero,
          $valintatieto,
          $vastaanottotieto,
          $ilmoittautumisenTila)""",
      "Insert test kk-hakutoive"
    )
  }

  def initSchema(): Unit = {
    val query = sqlu"""
          CREATE DOMAIN IF NOT EXISTS JSONB AS TEXT;
          CREATE SCHEMA gen;

          CREATE TABLE gen.gen_hakemus (
              hakemus_oid                    text NOT NULL PRIMARY KEY,
              haku_oid                       text,
              henkilo_oid                    text,
              lahiosoite                     text,
              postinumero                    text,
              postitoimipaikka               text,
              asuinmaa                       character varying,
              sahkoposti                     text,
              puhelin                        text,
              etunimet                       text,
              kutsumanimi                    text,
              sukunimi                       text,
              hetu                           text,
              kotikunta                      text,
              sukupuoli                      integer,
              asiointikieli                  integer,
              kansalaisuus                   jsonb,
              koulutusmarkkinointilupa       boolean,
              kiinnostunut_oppisopimuksesta  boolean,
              sahkoinenviestintalupa         boolean,
              valintatuloksen_julkaisulupa   boolean,
              jatetty                        timestamp with time zone,
              muokattu                       timestamp with time zone
          );

          CREATE TABLE gen.gen_henkilo (
              oppijanumero text NOT NULL,
              henkilo_oid  text NOT NULL PRIMARY KEY,
              aidinkieli   text
          );

          CREATE TABLE gen.gen_haku (
              haku_oid text NOT NULL PRIMARY KEY,
              hakutapakoodiuri text,
              haku_nimi_fi text,
              haku_nimi_sv text,
              haku_nimi_en text,
              kohdejoukko_koodiuri text,
              koulutuksen_alkamisvuosi integer,
              koulutuksen_alkamiskausiuri text
          );

          CREATE TABLE gen.gen_hakutoive(
              hakemus_oid text NOT NULL,
              hakukohde_oid text,
              hakutoivenumero bigint,
              valintatieto text,
              vastaanottotieto text,
              ilmoittautumisen_tila text
          );

          CREATE TABLE gen.gen_hakukohde(
              hakukohde_oid text NOT NULL PRIMARY KEY,
              toteutus_oid text,
              jarjestyspaikka_oid text,
              organisaatio_oid text,
              koulutuksen_alkamisvuosi integer,
              koulutuksen_alkamiskausiuri text
          );

          CREATE TABLE gen.gen_toteutus(
              toteutus_oid text NOT NULL PRIMARY KEY,
              koulutus_oid text,
              koulutuksen_alkamisvuosi integer,
              koulutuksen_alkamiskausiuri text
          );

          CREATE TABLE gen.gen_koulutus(
              koulutus_oid text NOT NULL PRIMARY KEY,
              ulkoinen_tunniste text
          );

          CREATE TABLE gen.gen_valintarekisteri(
              hakukohde_oid text,
              valintatapajono_id text,
              hakemus_oid text,
              maksun_tila text,
              valinnan_tila text,
              hyvaksyttyjajulkaistu timestamp with time zone,
              julkaistavissa boolean,
              varasijan_numero integer,
              prioriteetti integer,
              pisteet double precision
          );

          CREATE TABLE gen.gen_ylioppilas(
              henkilo_oid text,
              on_ylioppilas boolean,
              valmistumis_vuosi integer
          );

          CREATE TABLE gen.gen_supa_tieto (
              hakemus_oid text NOT NULL,
              avain       text NOT NULL,
              arvo        text
          );
          """
    db.run(query, "Init KK Hakijat test schema")
  }
}
