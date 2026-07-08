package fi.oph.ovara.backend.external.toisenasteenhakijat

import fi.oph.ovara.backend.external.toisenasteenhakijat.ExternalToisenAsteenHakijatTestData.*
import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import slick.jdbc.H2Profile.api.*

import java.time.format.DateTimeFormatter

trait ExternalToisenAsteenHakijatTestUtils {
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
      "Insert test henkilö"
    )
  }

  /** Insert an additional gen_henkilo row that links `henkiloOid` to the same master `oppijanumero`. */
  def insertHenkiloAlias(oppijanumero: String, henkiloOid: String): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_henkilo VALUES ($oppijanumero, $henkiloOid, NULL)""",
      "Insert test henkilö alias"
    )
  }

  def insertHaku(
    hakuOid: String = HAKU_OID,
    kohdejoukkoKoodiuri: String = "haunkohdejoukko_11#1",
    koulutuksenAlkamisvuosi: Option[Int] = None,
    koulutuksenAlkamiskausiuri: Option[String] = None
  ): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_haku VALUES(
          $hakuOid,
          'hakutapa_03#1',
          'Yhteishaku',
          'Gemensamma',
          'Joint application',
          $kohdejoukkoKoodiuri,
          $koulutuksenAlkamisvuosi,
          $koulutuksenAlkamiskausiuri
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

  def insertSupaTieto(
    hakemusOid: String = HAKEMUS_OID,
    avain: String,
    arvo: Option[String]
  ): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_supa_tieto VALUES($hakemusOid, $avain, $arvo)""",
      "Insert test supa_tieto"
    )
  }

  def insertOpetuskieli(
    hakemusOid: String = HAKEMUS_OID,
    arvo: Option[String] = Some(OPETUSKIELI_RAW)
  ): Unit = insertSupaTieto(hakemusOid, "perusopetuksen_kieli", arvo)

  def insertPohjakoulutus(hakemusOid: String = HAKEMUS_OID, arvo: Option[String]): Unit =
    insertSupaTieto(hakemusOid, "POHJAKOULUTUS", arvo)

  def insertTodistusvuosi(hakemusOid: String = HAKEMUS_OID, arvo: Option[String]): Unit =
    insertSupaTieto(hakemusOid, "PK_PAATTOTODISTUSVUOSI", arvo)

  def insertHakukohde(
    hakukohdeOid: String = HAKUKOHDE_OID,
    toteutusOid: String = TOTEUTUS_OID,
    jarjestyspaikkaOid: String = ORGANISAATIO_OID,
    organisaatioOid: Option[String] = Some(ORGANISAATIO_OID),
    koulutuksenAlkamisvuosi: Option[Int] = Some(KOULUTUKSEN_ALKAMISVUOSI),
    koulutuksenAlkamiskausiuri: Option[String] = Some(KOULUTUKSEN_ALKAMISKAUSIURI),
    hakukohteenLinjaJson: Option[String] = None,
    jarjestaaUrheilijanAmmkoulutusta: Option[Boolean] = Some(false)
  ): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_hakukohde VALUES(
          $hakukohdeOid,
          $toteutusOid,
          $jarjestyspaikkaOid,
          $organisaatioOid,
          'Elokuvaleikkaus',
          'Filmklippning',
          'Film Editing',
          $koulutuksenAlkamisvuosi,
          $koulutuksenAlkamiskausiuri,
          $hakukohteenLinjaJson,
          $jarjestaaUrheilijanAmmkoulutusta)""",
      "Insert test hakukohde"
    )
  }

  def insertHakemusToinenAsteYhteishaku(
    hakemusOid: String = HAKEMUS_OID,
    huoltaja1Etunimi: Option[String] = Some(HUOLTAJA1.etunimi),
    huoltaja1Sukunimi: Option[String] = Some(HUOLTAJA1.sukunimi),
    huoltaja1Matkapuhelin: Option[String] = Some(HUOLTAJA1.puhelinnumero),
    huoltaja1Email: Option[String] = Some(HUOLTAJA1.sahkoposti),
    huoltaja2Etunimi: Option[String] = Some(HUOLTAJA2.etunimi),
    huoltaja2Sukunimi: Option[String] = Some(HUOLTAJA2.sukunimi),
    huoltaja2Matkapuhelin: Option[String] = Some(HUOLTAJA2.puhelinnumero),
    huoltaja2Email: Option[String] = Some(HUOLTAJA2.sahkoposti),
    hakukohteetJson: Option[String] = Some(TOINENASTE_HAKUKOHTEET_JSON),
    kiinnostunutUrheilijanAmmatillisestaKoulutuksesta: Option[Boolean] = Some(false),
    urli: Option[UrheilijanLisakysymykset] = Some(lukioKysymykset),
    uram: Option[UrheilijanLisakysymykset] = Some(ammatillisetKysymykset)
  ): Unit = {
    val urli_ = urli.getOrElse(UrheilijanLisakysymykset())
    val uram_ = uram.getOrElse(UrheilijanLisakysymykset())
    db.run(
      sqlu"""INSERT INTO gen.gen_hakemus_toinenaste_yhteishaku VALUES(
          $hakemusOid,
          $huoltaja1Etunimi,
          $huoltaja1Sukunimi,
          $huoltaja1Matkapuhelin,
          $huoltaja1Email,
          $huoltaja2Etunimi,
          $huoltaja2Sukunimi,
          $huoltaja2Matkapuhelin,
          $huoltaja2Email,
          $hakukohteetJson,
          $kiinnostunutUrheilijanAmmatillisestaKoulutuksesta,
          ${urli_.laji},
          ${urli_.seura},
          ${urli_.liitto},
          ${urli_.sivulaji},
          ${urli_.keskiarvo},
          ${urli_.tamakausi},
          ${urli_.peruskoulu},
          ${urli_.viimekausi},
          ${urli_.toissakausi},
          ${urli_.valmentaja_puh},
          ${urli_.valmentaja_nimi},
          ${urli_.valmentaja_email},
          ${urli_.valmennusryhma_maajoukkue},
          ${urli_.valmennusryhma_piirijoukkue},
          ${urli_.valmennusryhma_seurajoukkue},
          ${uram_.laji},
          ${uram_.seura},
          ${uram_.liitto},
          ${uram_.sivulaji},
          ${uram_.keskiarvo},
          ${uram_.tamakausi},
          ${uram_.peruskoulu},
          ${uram_.viimekausi},
          ${uram_.toissakausi},
          ${uram_.valmentaja_puh},
          ${uram_.valmentaja_nimi},
          ${uram_.valmentaja_email},
          ${uram_.valmennusryhma_maajoukkue},
          ${uram_.valmennusryhma_piirijoukkue},
          ${uram_.valmennusryhma_seurajoukkue})""",
      "Insert test hakemus_toinenaste_yhteishaku"
    )
  }

  def insertOrganisaatio(
    organisaatioOid: String = ORGANISAATIO_OID,
    oppilaitosnumero: Option[String] = Some(OPPILAITOSNUMERO),
    nimiFi: Option[String] = None,
    nimiSv: Option[String] = None
  ): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_organisaatio VALUES($organisaatioOid, $oppilaitosnumero, $nimiFi, $nimiSv)""",
      "Insert test organisaatio"
    )
  }

  def insertHenkiloLahtokoulu(
    henkiloOid: String = OPPIJANUMERO,
    tila: Option[String] = Some("KESKEN"),
    luokka: Option[String] = None,
    oppilaitosOid: Option[String] = None,
    suoritusTyyppi: Option[String] = None,
    arvosanaPuuttuu: Option[Boolean] = Some(false),
    suorituksenAlku: Option[java.time.LocalDate] = None,
    suorituksenLoppu: Option[java.time.LocalDate] = None,
    valmistumisvuosi: Option[Int] = None
  ): Unit = {
    val alkuStr  = suorituksenAlku.map(_.toString)
    val loppuStr = suorituksenLoppu.map(_.toString)
    db.run(
      sqlu"""INSERT INTO gen.gen_henkilo_lahtokoulu VALUES(
          $henkiloOid,
          $tila,
          $luokka,
          $oppilaitosOid,
          $suoritusTyyppi,
          $arvosanaPuuttuu,
          $alkuStr,
          $loppuStr,
          $valmistumisvuosi)""",
      "Insert test henkilo_lahtokoulu"
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
      "Insert test hakutoive"
    )
  }

  def insertToteutusJaKoulutus(
    koulutuksenAlkamisvuosi: Option[Int] = None,
    koulutuksenAlkamiskausiuri: Option[String] = None
  ): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_toteutus VALUES(
          $TOTEUTUS_OID,
          $KOULUTUS_OID,
          $koulutuksenAlkamisvuosi,
          $koulutuksenAlkamiskausiuri)""",
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
              oppijanumero text NOT NULL,
              henkilo_oid  text NOT NULL PRIMARY KEY,
              aidinkieli   text
          );

          CREATE TABLE gen.gen_supa_tieto (
              hakemus_oid text NOT NULL,
              avain       text NOT NULL,
              arvo        text
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
              hakukohde_nimi_fi text,
              hakukohde_nimi_sv text,
              hakukohde_nimi_en text,
              koulutuksen_alkamisvuosi integer,
              koulutuksen_alkamiskausiuri text,
              hakukohteen_linja jsonb,
              jarjestaa_urheilijan_ammkoulutusta boolean
          );

          CREATE TABLE gen.gen_hakemus_toinenaste_yhteishaku (
              hakemus_oid text NOT NULL PRIMARY KEY,
              huoltaja1_etunimi text,
              huoltaja1_sukunimi text,
              huoltaja1_matkapuhelin text,
              huoltaja1_email text,
              huoltaja2_etunimi text,
              huoltaja2_sukunimi text,
              huoltaja2_matkapuhelin text,
              huoltaja2_email text,
              hakukohteet jsonb,
              kiinnostunut_urheilijan_ammatillisesta_koulutuksesta boolean,
              urh_laji text,
              urh_seura text,
              urh_liitto text,
              urh_sivulaji text,
              urh_keskiarvo text,
              urh_tamakausi text,
              urh_peruskoulu text,
              urh_viimekausi text,
              urh_toissakausi text,
              urh_valmentaja_puh text,
              urh_valmentaja_nimi text,
              urh_valmentaja_email text,
              urh_valmennusryhma_maajoukkue text,
              urh_valmennusryhma_piirijoukkue text,
              urh_valmennusryhma_seurajoukkue text,
              urh_amm_laji text,
              urh_amm_seura text,
              urh__amm_liitto text,
              urh_amm_sivulaji text,
              urh_amm_keskiarvo text,
              urh_amm_tamakausi text,
              urh_amm_peruskoulu text,
              urh_amm_viimekausi text,
              urh_amm_toissakausi text,
              urh_amm_valmentaja_puh text,
              urh_amm_valmentaja_nimi text,
              urh_amm_valmentaja_email text,
              urh_amm_valmennusryhma_maajoukkue text,
              urh_amm_valmennusryhma_piirijoukkue text,
              urh_amm_valmennusryhma_seurajoukkue text
          );

          CREATE TABLE gen.gen_organisaatio(
              organisaatio_oid text NOT NULL PRIMARY KEY,
              oppilaitosnumero text,
              nimi_fi text,
              nimi_sv text
          );

          CREATE TABLE gen.gen_henkilo_lahtokoulu(
              henkilo_oid text NOT NULL,
              tila text,
              luokka text,
              oppilaitos_oid text,
              suoritus_tyyppi text,
              arvosana_puuttuu boolean,
              suorituksen_alku date,
              suorituksen_loppu date,
              valmistumisvuosi integer
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
              koulutus_oid text,
              koulutuksen_alkamisvuosi integer,
              koulutuksen_alkamiskausiuri text
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
