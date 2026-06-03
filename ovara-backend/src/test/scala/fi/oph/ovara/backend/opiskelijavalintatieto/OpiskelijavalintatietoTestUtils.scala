package fi.oph.ovara.backend.opiskelijavalintatieto

import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import slick.jdbc.H2Profile.api.*

trait OpiskelijavalintatietoTestUtils {
  val db: ReadOnlyDatabase

  val OPPIJANUMERO     = "1.2.246.562.24.9"
  val HAKEMUS_OID      = "1.2.246.562.11.580"
  val HAKU_OID         = "1.2.246.562.29.001"
  val HAKUKOHDE_OID    = "1.2.246.562.20.012"
  val ORGANISAATIO_OID = "1.2.246.562.10.486"

  def initAndInsert(): Unit = {
    initSchema()
    insertHenkilo()
    insertHakemusData()
  }

  def initSchema(): Unit = {
    db.run(sqlu"""DROP ALL OBJECTS""", "Drop everything")
    db.run(sqlu"""CREATE SCHEMA gen""", "Create gen schema")
    db.run(
      sqlu"""CREATE TABLE gen.gen_henkilo (oppijanumero text, henkilo_oid text, etunimet text, sukunimi text, hetu text, syntymaaika date)""",
      "Create henkilo table"
    )
    db.run(
      sqlu"""CREATE TABLE gen.gen_hakutoive (hakemus_oid text, hakukohde_oid text, haku_oid text, henkilo_oid text, valintatieto text, vastaanottotieto text, ilmoittautumisen_tila text)""",
      "Create hakutoive table"
    )
    db.run(
      sqlu"""CREATE TABLE gen.gen_hakukohde (hakukohde_oid text, haku_oid text, hakukohde_nimi_fi text, hakukohde_nimi_sv text, hakukohde_nimi_en text, jarjestyspaikka_oid text, koulutuksen_alkamiskausiuri text, koulutuksen_alkamisvuosi integer)""",
      "Create hakukohde table"
    )
    db.run(
      sqlu"""CREATE TABLE gen.gen_haku (haku_oid text, haku_nimi_fi text, haku_nimi_sv text, haku_nimi_en text, kohdejoukko_koodiuri text, hakutapakoodiuri text, koulutuksen_alkamiskausiuri text, koulutuksen_alkamisvuosi integer)""",
      "Create hakukohde table"
    )
    db.run(
      sqlu"""CREATE TABLE gen.gen_organisaatio (organisaatio_oid text, nimi_fi text, nimi_sv text)""",
      "Create hakukohde table"
    )
  }

  def insertHenkilo(oppijanumero: String = OPPIJANUMERO): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_henkilo VALUES ($oppijanumero, $oppijanumero, 'Toivo Taneli' , 'Testinen', '080872W943L', '1977-01-15')""",
      "Insert test henkilo"
    )
  }

  def insertHakemusData(): Unit = {
    db.run(
      sqlu"""INSERT INTO gen.gen_haku VALUES ($HAKU_OID, 'Korkeakoulujen yhteishaku', 'Högskolornas gemensamma', 'Joint application to higher education', 'haunkohdejoukko_12#1', 'hakutapa_01#1', 'kausi_k#1', 2023)""",
      "Insert test haku"
    )
    db.run(
      sqlu"""INSERT INTO gen.gen_hakukohde VALUES ($HAKUKOHDE_OID, $HAKU_OID, 'Maisterihaku', 'Magisteransökan', 'Master''s Admission', $ORGANISAATIO_OID, 'kausi_s#1', 2022)""",
      "Insert test hakukohde"
    )
    db.run(
      sqlu"""INSERT INTO gen.gen_hakutoive VALUES ($HAKEMUS_OID, $HAKUKOHDE_OID, $HAKU_OID, $OPPIJANUMERO, null, null, null)""",
      "Insert test hakutoive"
    )
    db.run(
      sqlu"""INSERT INTO gen.gen_organisaatio VALUES ($ORGANISAATIO_OID, 'Bio- ja ympäristötieteellinen tiedekunta', 'Bio- och miljövetenskapliga fakulteten')""",
      "Insert test organisaatio"
    )
  }

}
