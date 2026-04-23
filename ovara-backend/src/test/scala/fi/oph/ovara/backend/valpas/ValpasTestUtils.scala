package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import slick.jdbc.H2Profile.api.*

import java.time.OffsetDateTime

trait ValpasTestUtils {
  val db: ReadOnlyDatabase

  val OPPIJANUMERO       = "1.2.246.562.24.9"
  val HAKEMUS_OID        = "1.2.246.562.11.580"
  val HAKU_OID           = "1.2.246.562.29.001"
  val HAKUKOHDE_OID      = "1.2.246.562.20.012"
  val TOTEUTUS_OID       = "1.2.246.562.17.122"
  val KOULUTUS_OID       = "1.2.246.562.13.022"
  val ORGANISAATIO_OID   = "1.2.246.562.10.486"
  val VALINTATAPAJONO_ID = "16799"

  def insertHakemus(): Unit = {
    val tomorrow = OffsetDateTime.now().plusDays(2)
    val yesterday = OffsetDateTime.now().minusDays(1)

    db.run(sqlu"""INSERT INTO gen.gen_henkilo values($OPPIJANUMERO, $OPPIJANUMERO)""", "Insert test haku")
    db.run(
      sqlu"""INSERT INTO gen.gen_hakemus values(
          $HAKEMUS_OID,
          $HAKU_OID,
          $OPPIJANUMERO,
          'Katu 1',
          '00100',
          'Helsinki',
          '246',
          'oppija@example.test',
          '+358401234567',
          '2025-08-13T14:52:14+03')""",
      "Insert test hakemus"
    )
    db.run(
      sqlu"""INSERT INTO gen.gen_haku values(
          $HAKU_OID,
          'hakutapa_03#1',
          'Yhteishaku',
          'Gemensamma',
          'Joint application',
          'haunkohdejoukko_11#1',
          '[{"alkaa": "2024-08-31T23:59", "paattyy": "2027-08-31T23:59"},{"alkaa": "2026-08-31T23:59", "paattyy": "2027-09-31T23:59"},{"alkaa": "2022-08-31T23:59", "paattyy": "2023-08-31T23:59"}]'
          )""",
      "Insert test haku"
    )

    db.run(
      sqlu"""INSERT INTO gen.gen_koodi values
         ('maatjavaltiot2_246#2', 'maatjavaltiot2', '246', 2, 'Suomi', 'Finland', 'Finland'),
         ('hakutapa_03#1', 'hakutapa', '03', 1, 'Jatkuva haku', 'Kontinuerlig ansökan', 'Rolling admission (upper secondary level)'),
         ('haunkohdejoukko_11#1', 'haunkohdejoukko', '11', 1, 'Perusopetuksen jälkeisen koulutuksen yhteishaku', 'Gemensam ansökan till utbildning efter grundläggande utbildning', null)
         """,
      "Insert test koodit"
    )

    db.run(
      sqlu"""INSERT INTO gen.gen_ohjausparametri values($HAKU_OID, $HAKUKIERROS_PAATTYY, ${tomorrow.toString}, null)""",
      "Insert test hkp-ohjausparametri"
    )
    db.run(
      sqlu"""INSERT INTO gen.gen_ohjausparametri values($HAKU_OID, $VALINTATULOSTEN_JULKISTAMINEN_HAKIJOILLE, null, ${yesterday.toString})""",
      "Insert test vtjh-ohjausparametri"
    )
  }

  def insertHakutoive(): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_hakukohde values(
          $HAKUKOHDE_OID,
          $TOTEUTUS_OID,
          $ORGANISAATIO_OID,
          'Elokuvaleikkaus',
          'Filmklippning',
          'Film Editing')""",
      "Insert test hakukohde"
    )
    db.run(
      sqlu"""INSERT INTO gen.gen_hakutoive values(
          $HAKEMUS_OID,
          $HAKUKOHDE_OID,
          3,
          null,
          null,
          null,
          'EI_HARKINNANVARAINEN')""",
      "Insert test hakutoive"
    )

    db.run(
      sqlu"""INSERT INTO gen.gen_organisaatio values(
             $ORGANISAATIO_OID,
             'Porin toimipaikka',
             'Björneborg verksamhetspunkt')""",
      "Insert test organisaatio"
    )

    db.run(sqlu"""INSERT INTO gen.gen_toteutus values($TOTEUTUS_OID, $KOULUTUS_OID)""", "Insert test toteutus")

    db.run(
      sqlu"""INSERT INTO gen.gen_koulutus values(
            $KOULUTUS_OID,
            'Kulttuurituottaja',
            'Kulturproducent',
            'Kulttuurituottaja',
            JSON '["koulutus_621702#12"]')""",
      "Insert test koulutus"
    )

    db.run(
      sqlu"""INSERT INTO gen.gen_koodi values
             ('koulutus_621702#12' , 'koulutus','621702',  12, 'Kulttuurituottaja' , 'Kulturproducent', 'Bachelor of Culture and Arts, Cultural Manager')""",
      "Insert test koodi"
    )

    db.run(
      sqlu"""INSERT INTO gen.gen_valintarekisteri values($VALINTATAPAJONO_ID, $HAKEMUS_OID, $HAKUKOHDE_OID, 23.7, 4, true)""",
      "Insert test valintarekisteri"
    )

    db.run(
      sqlu"""INSERT INTO gen.gen_valintarekisteri_valintatapajono values($VALINTATAPAJONO_ID, 21.1)""",
      "Insert test valintarekisteri valintatapajono"
    )
  }

  def initSchema(): Unit = {
    val query = sqlu"""
          CREATE DOMAIN IF NOT EXISTS JSONB AS TEXT;
          CREATE SCHEMA gen;

          CREATE TABLE gen.gen_hakemus (
              hakemus_oid      text NOT NULL PRIMARY KEY,
              haku_oid         text,
              henkilo_oid      text,
              lahiosoite       text,
              postinumero      text,
              postitoimipaikka text,
              asuinmaa         character varying,
              sahkoposti       text,
              puhelin          text,
              muokattu         timestamp with time zone
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
              kohdejoukko_koodiuri text,
              hakuajat jsonb
          );

          CREATE TABLE gen.gen_hakutoive(
              hakemus_oid text NOT NULL PRIMARY KEY,
              hakukohde_oid text,
              hakutoivenumero bigint,
              vastaanottotieto text,
              ilmoittautumisen_tila text,
              valintatieto text,
              harkinnanvaraisuuden_syy text
          );

          CREATE TABLE gen.gen_hakukohde(
              hakukohde_oid text NOT NULL PRIMARY KEY,
              toteutus_oid text,
              jarjestyspaikka_oid text,
              hakukohde_nimi_fi text,
              hakukohde_nimi_sv text,
              hakukohde_nimi_en text
          );

          CREATE TABLE gen.gen_organisaatio(
              organisaatio_oid text NOT NULL PRIMARY KEY,
              nimi_fi text,
              nimi_sv text
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

          CREATE TABLE gen.gen_ohjausparametri(
              haku_oid text,
              avain text,
              aikaleima timestamp with time zone,
              aikaleima_alkaa timestamp with time zone
          );

          CREATE TABLE gen.gen_valintarekisteri(
            valintatapajono_id text,
            hakemus_oid text,
            hakukohde_oid text,
            pisteet double precision,
            varasijan_numero integer,
            julkaistavissa boolean
          );

          CREATE TABLE gen.gen_valintarekisteri_valintatapajono(
            valintatapajono_id text,
            alin_hyvaksytty_pistemaara double precision
          );

          """
    db.run(query, "Init Valpas test schema")
  }

}
