package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import fi.oph.ovara.backend.valpas.ValpasFactory.*
import slick.jdbc.H2Profile.api.*

trait ValpasTestUtils {
  val db: ReadOnlyDatabase

  def insertHenkilo(oppijanumero: String = OPPIJANUMERO): Unit = {
    db.run(sqlu"""INSERT INTO gen.gen_henkilo VALUES ($oppijanumero, $oppijanumero)""", "Insert test henkilö")
  }

  def insertHakemus(
    aktiivinen: Boolean = true,
    oppijanumero: String = OPPIJANUMERO,
    hakemusOid: String = HAKEMUS_OID,
    hakuOid: String = HAKU_OID
  ): Unit = {
    insertHenkilo(oppijanumero)

    db.run(
      sqlu"""INSERT INTO gen.gen_hakemus values(
          $hakemusOid,
          $hakuOid,
          $oppijanumero,
          $LAHIOSOITE,
          $POSTINUMERO,
          $HELSINKI,
          $SUOMI_KOODI,
          $EMAIL,
          $MATKAPUHELIN,
          $HAKEMUKSEN_MUOKKAUSAIKA)""",
      "Insert test hakemus"
    )
    db.run(
      sqlu"""INSERT INTO gen.gen_haku values(
          $hakuOid,
          'hakutapa_03#1',
          'Yhteishaku',
          'Gemensamma',
          'Joint application',
          'haunkohdejoukko_11#1',
          '[{"alkaa": "2024-08-31T23:59", "paattyy": "2027-08-31T23:59"},{"alkaa": "2026-08-31", "paattyy": "2027-09-30"},{"alkaa": "2022-08-31T23:59", "paattyy": "2023-08-31T23:59"}]'
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

    val kierrosPaattyy =
      if (aktiivinen) TOMORROW.toString
      else YESTERDAY.toString

    db.run(
      sqlu"""INSERT INTO gen.gen_ohjausparametri_haku values($hakuOid, $HAKUKIERROS_PAATTYY, $kierrosPaattyy, null)""",
      "Insert test hkp-ohjausparametri"
    )
    db.run(
      sqlu"""INSERT INTO gen.gen_ohjausparametri_haku values($hakuOid, $VALINTATULOSTEN_JULKISTAMINEN_HAKIJOILLE, null, ${YESTERDAY.toString})""",
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
          'VASTAANOTTANUT_SITOVASTI',
          'LASNA',
          'HYVAKSYTTY',
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
            JSON '["#$KOULUTUS_KOODIURI"]')""",
      "Insert test koulutus"
    )

    db.run(
      sqlu"""INSERT INTO gen.gen_koodi values
             ($KOULUTUS_KOODIURI , 'koulutus','621702',  12, 'Kulttuurituottaja' , 'Kulturproducent', 'Bachelor of Culture and Arts, Cultural Manager')""",
      "Insert test koodi"
    )

    insertValinnanTulos()
  }

  def insertValinnanTulos(valintatapajonoId: String = VALINTATAPAJONO_ID): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_valintarekisteri values($valintatapajonoId, $HAKEMUS_OID, $HAKUKOHDE_OID, 23.7, 4, true)""",
      "Insert test valintarekisteri"
    )

    db.run(
      sqlu"""INSERT INTO gen.gen_valintarekisteri_valintatapajono values($valintatapajonoId, 21.1)""",
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

          CREATE TABLE gen.gen_ohjausparametri_haku(
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
