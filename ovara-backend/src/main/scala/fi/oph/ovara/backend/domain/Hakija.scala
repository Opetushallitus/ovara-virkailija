package fi.oph.ovara.backend.domain

import java.time.LocalDate

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
    harkinnanvaraisuus: Option[String],
    soraAiempi: Option[Boolean],
    soraTerveys: Option[Boolean],
    markkinointilupa: Option[Boolean],
    julkaisulupa: Option[Boolean],
    sahkoinenViestintaLupa: Option[Boolean],
    lahiosoite: String,
    postinumero: String,
    postitoimipaikka: String
)

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
    harkinnanvaraisuus: Option[String],
    soraAiempi: Option[Boolean],
    soraTerveys: Option[Boolean],
    markkinointilupa: Option[Boolean],
    julkaisulupa: Option[Boolean],
    sahkoinenViestintaLupa: Option[Boolean],
    lahiosoite: String,
    postinumero: String,
    postitoimipaikka: String
)

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
