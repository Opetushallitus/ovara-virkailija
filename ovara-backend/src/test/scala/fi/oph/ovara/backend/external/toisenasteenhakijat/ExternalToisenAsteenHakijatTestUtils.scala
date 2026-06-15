package fi.oph.ovara.backend.external.toisenasteenhakijat

import fi.oph.ovara.backend.external.toisenasteenhakijat.ExternalToisenAsteenHakijatTestData.*
import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import slick.jdbc.H2Profile.api.*

import java.time.format.DateTimeFormatter

trait ExternalToisenAsteenHakijatTestUtils {
  private val SECONDS_TZ_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

  val db: ReadOnlyDatabase

  def insertHenkilo(oppijanumero: String = OPPIJANUMERO): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_henkilo VALUES ($oppijanumero, $oppijanumero)""",
      "Insert test henkilö"
    )
  }

  def insertHaku(
    hakuOid: String = HAKU_OID,
    kohdejoukkoKoodiuri: String = "haunkohdejoukko_11#1"
  ): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_haku VALUES(
          $hakuOid,
          'hakutapa_03#1',
          'Yhteishaku',
          'Gemensamma',
          'Joint application',
          $kohdejoukkoKoodiuri
          )""",
      "Insert test haku"
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
          $kansalaisuusJson,
          $koulutusmarkkinointilupa,
          $kiinnostunutOppisopimuksesta,
          $sahkoinenviestintalupa,
          $valintatuloksenJulkaisulupa,
          $jatettyStr,
          $muokattuStr)""",
      "Insert test hakemus"
    )
  }

  def insertHakukohde(
    hakukohdeOid: String = HAKUKOHDE_OID,
    toteutusOid: String = TOTEUTUS_OID,
    jarjestyspaikkaOid: String = ORGANISAATIO_OID
  ): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_hakukohde VALUES(
          $hakukohdeOid,
          $toteutusOid,
          $jarjestyspaikkaOid,
          'Elokuvaleikkaus',
          'Filmklippning',
          'Film Editing')""",
      "Insert test hakukohde"
    )
  }

  def insertHakutoive(
    hakemusOid: String = HAKEMUS_OID,
    hakukohdeOid: String = HAKUKOHDE_OID,
    hakutoivenumero: Int = 1
  ): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_hakutoive VALUES(
          $hakemusOid,
          $hakukohdeOid,
          $hakutoivenumero)""",
      "Insert test hakutoive"
    )
  }

  def insertToteutusJaKoulutus(): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_toteutus VALUES($TOTEUTUS_OID, $KOULUTUS_OID)""",
      "Insert test toteutus"
    )
    db.run(
      sqlu"""INSERT INTO gen.gen_koulutus VALUES(
            $KOULUTUS_OID,
            'Kulttuurituottaja',
            'Kulturproducent',
            'Kulttuurituottaja',
            JSON '["#$KOULUTUS_KOODIURI"]')""",
      "Insert test koulutus"
    )
    db.run(
      sqlu"""INSERT INTO gen.gen_koodi VALUES
             ($KOULUTUS_KOODIURI, 'koulutus', '621702', 12,
              'Kulttuurituottaja', 'Kulturproducent',
              'Bachelor of Culture and Arts, Cultural Manager')""",
      "Insert test koodi"
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
              kansalaisuus                   jsonb,
              koulutusmarkkinointilupa       boolean,
              kiinnostunut_oppisopimuksesta  boolean,
              sahkoinenviestintalupa         boolean,
              valintatuloksen_julkaisulupa   boolean,
              jatetty                        timestamp with time zone,
              muokattu                       timestamp with time zone
          );

          CREATE TABLE gen.gen_henkilo (
              oppijanumero text NOT NULL PRIMARY KEY,
              henkilo_oid  text
          );

          CREATE TABLE gen.gen_haku (
              haku_oid text NOT NULL PRIMARY KEY,
              hakutapakoodiuri text,
              haku_nimi_fi text,
              haku_nimi_sv text,
              haku_nimi_en text,
              kohdejoukko_koodiuri text
          );

          CREATE TABLE gen.gen_hakutoive(
              hakemus_oid text NOT NULL,
              hakukohde_oid text,
              hakutoivenumero bigint
          );

          CREATE TABLE gen.gen_hakukohde(
              hakukohde_oid text NOT NULL PRIMARY KEY,
              toteutus_oid text,
              jarjestyspaikka_oid text,
              hakukohde_nimi_fi text,
              hakukohde_nimi_sv text,
              hakukohde_nimi_en text
          );

          CREATE TABLE gen.gen_koulutus(
              koulutus_oid text NOT NULL PRIMARY KEY,
              koulutus_nimi_fi text,
              koulutus_nimi_sv text,
              koulutus_nimi_en text,
              koulutukset_koodiuri jsonb
          );

          CREATE TABLE gen.gen_toteutus(
              toteutus_oid text NOT NULL PRIMARY KEY,
              koulutus_oid text
          );

          CREATE TABLE gen.gen_koodi(
              versioitu_koodiuri text,
              koodistouri text,
              koodiarvo text,
              koodiversio integer,
              nimi_fi text,
              nimi_sv text,
              nimi_en text
          );
          """
    db.run(query, "Init Hakijat test schema")
  }
}
