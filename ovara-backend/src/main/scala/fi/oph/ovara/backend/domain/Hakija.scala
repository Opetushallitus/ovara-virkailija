package fi.oph.ovara.backend.domain

import java.time.LocalDate

abstract class HakijaBase {
  val turvakielto: Option[Boolean]
  val kansalaisuus: Kielistetty
  val oppijanumero: String
  val hakemusOid: String
  val hakukohteenNimi: Kielistetty
  val hakukohdeOid: String
  val prioriteetti: Int
  val kaksoistutkintoKiinnostaa: Option[Boolean]
  val urheilijatutkintoKiinnostaa: Option[Boolean]
  val valintatieto: String
  val varasija: Option[String]
  val kokonaispisteet: Option[String]
  val hylkTaiPerSyy: Kielistetty
  val vastaanottotieto: Option[String]
  val viimVastaanottopaiva: Option[LocalDate]
  val ilmoittautuminen: Option[String]
  val harkinnanvaraisuus: Option[String]
  val soraAiempi: Option[Boolean]
  val soraTerveys: Option[Boolean]
  val markkinointilupa: Option[Boolean]
  val julkaisulupa: Option[Boolean]
  val sahkoinenViestintaLupa: Option[Boolean]
  val lahiosoite: String
  val postinumero: String
  val postitoimipaikka: String
}

case class Hakija(
    hakijanSukunimi: String,
    hakijanEtunimi: String,
    turvakielto: Option[Boolean],
    kansalaisuus: Kielistetty,
    oppijanumero: String,
    hakemusOid: String,
    hakukohteenNimi: Kielistetty,
    hakukohdeOid: String,
    prioriteetti: Int,
    kaksoistutkintoKiinnostaa: Option[Boolean],
    urheilijatutkintoKiinnostaa: Option[Boolean],
    valintatieto: String,
    varasija: Option[String],
    kokonaispisteet: Option[String],
    hylkTaiPerSyy: Kielistetty,
    vastaanottotieto: Option[String],
    viimVastaanottopaiva: Option[LocalDate],
    ilmoittautuminen: Option[String],
    harkinnanvaraisuus: Option[String],
    soraAiempi: Option[Boolean],
    soraTerveys: Option[Boolean],
    markkinointilupa: Option[Boolean],
    julkaisulupa: Option[Boolean],
    sahkoinenViestintaLupa: Option[Boolean],
    lahiosoite: String,
    postinumero: String,
    postitoimipaikka: String
) extends HakijaBase

case class HakijaWithCombinedNimi(
    hakija: String,
    turvakielto: Option[Boolean],
    kansalaisuus: Kielistetty,
    oppijanumero: String,
    hakemusOid: String,
    hakukohteenNimi: Kielistetty,
    hakukohdeOid: String,
    prioriteetti: Int,
    kaksoistutkintoKiinnostaa: Option[Boolean],
    urheilijatutkintoKiinnostaa: Option[Boolean],
    valintatieto: String,
    varasija: Option[String],
    kokonaispisteet: Option[String],
    hylkTaiPerSyy: Kielistetty,
    vastaanottotieto: Option[String],
    viimVastaanottopaiva: Option[LocalDate],
    ilmoittautuminen: Option[String],
    harkinnanvaraisuus: Option[String],
    soraAiempi: Option[Boolean],
    soraTerveys: Option[Boolean],
    markkinointilupa: Option[Boolean],
    julkaisulupa: Option[Boolean],
    sahkoinenViestintaLupa: Option[Boolean],
    lahiosoite: String,
    postinumero: String,
    postitoimipaikka: String
) extends HakijaBase

object HakijaWithCombinedNimi {
  def apply(hakija: Hakija): HakijaWithCombinedNimi = {
    new HakijaWithCombinedNimi(
      hakija = s"${hakija.hakijanSukunimi}, ${hakija.hakijanEtunimi}",
      turvakielto = hakija.turvakielto,
      kansalaisuus = hakija.kansalaisuus,
      oppijanumero = hakija.oppijanumero,
      hakemusOid = hakija.hakemusOid,
      hakukohteenNimi = hakija.hakukohteenNimi,
      hakukohdeOid = hakija.hakukohdeOid,
      prioriteetti = hakija.prioriteetti,
      kaksoistutkintoKiinnostaa = hakija.kaksoistutkintoKiinnostaa,
      urheilijatutkintoKiinnostaa = hakija.urheilijatutkintoKiinnostaa,
      valintatieto = hakija.valintatieto,
      varasija = hakija.varasija,
      kokonaispisteet = hakija.kokonaispisteet,
      hylkTaiPerSyy = hakija.hylkTaiPerSyy,
      vastaanottotieto = hakija.vastaanottotieto,
      viimVastaanottopaiva = hakija.viimVastaanottopaiva,
      ilmoittautuminen = hakija.ilmoittautuminen,
      harkinnanvaraisuus = hakija.harkinnanvaraisuus,
      soraAiempi = hakija.soraAiempi,
      soraTerveys = hakija.soraTerveys,
      markkinointilupa = hakija.markkinointilupa,
      julkaisulupa = hakija.julkaisulupa,
      sahkoinenViestintaLupa = hakija.sahkoinenViestintaLupa,
      lahiosoite = hakija.lahiosoite,
      postinumero = hakija.postinumero,
      postitoimipaikka = hakija.postitoimipaikka
    )
  }
}
