package fi.oph.ovara.backend.domain

import java.time.LocalDate

abstract class Hakija {
  val kansalaisuus: Kielistetty
  val oppijanumero: String
  val hakemusOid: String
  val toimipiste: Kielistetty
  val hakukohteenNimi: Kielistetty
  val prioriteetti: Int
  val valintatieto: Option[String]
  val vastaanottotieto: Option[String]
  val viimVastaanottopaiva: Option[LocalDate]
  val ilmoittautuminen: Option[String]
  val markkinointilupa: Option[Boolean]
  val julkaisulupa: Option[Boolean]
  val sahkoinenViestintalupa: Option[Boolean]
  val lahiosoite: Option[String]
  val postinumero: Option[String]
  val postitoimipaikka: Option[String]
}

case class ToisenAsteenHakija(
    hakijanSukunimi: String,
    hakijanEtunimi: String,
    turvakielto: Option[Boolean],
    kansalaisuus: Kielistetty,
    oppijanumero: String,
    hakemusOid: String,
    oppilaitos: Kielistetty,
    toimipiste: Kielistetty,
    hakukohteenNimi: Kielistetty,
    prioriteetti: Int,
    kaksoistutkintoKiinnostaa: Option[Boolean],
    urheilijatutkintoKiinnostaa: Option[Boolean],
    valintatieto: Option[String],
    varasija: Option[String],
    kokonaispisteet: Option[String],
    hylkTaiPerSyy: Kielistetty,
    vastaanottotieto: Option[String],
    viimVastaanottopaiva: Option[LocalDate],
    ilmoittautuminen: Option[String],
    harkinnanvaraisuus: Option[String],
    soraAiempi: Option[Boolean],
    soraTerveys: Option[Boolean],
    pohjakoulutus: Kielistetty,
    markkinointilupa: Option[Boolean],
    julkaisulupa: Option[Boolean],
    sahkoinenViestintalupa: Option[Boolean],
    lahiosoite: Option[String],
    postinumero: Option[String],
    postitoimipaikka: Option[String]
) extends Hakija

case class ToisenAsteenHakijaWithCombinedNimi(
    hakija: String,
    turvakielto: Option[Boolean],
    kansalaisuus: Kielistetty,
    oppijanumero: String,
    hakemusOid: String,
    oppilaitos: Kielistetty,
    toimipiste: Kielistetty,
    hakukohteenNimi: Kielistetty,
    prioriteetti: Int,
    kaksoistutkintoKiinnostaa: Option[Boolean],
    urheilijatutkintoKiinnostaa: Option[Boolean],
    valintatieto: Option[String],
    varasija: Option[String],
    kokonaispisteet: Option[String],
    hylkTaiPerSyy: Kielistetty,
    vastaanottotieto: Option[String],
    viimVastaanottopaiva: Option[LocalDate],
    ilmoittautuminen: Option[String],
    harkinnanvaraisuus: Option[String],
    soraAiempi: Option[Boolean],
    soraTerveys: Option[Boolean],
    pohjakoulutus: Kielistetty,
    markkinointilupa: Option[Boolean],
    julkaisulupa: Option[Boolean],
    sahkoinenViestintalupa: Option[Boolean],
    lahiosoite: Option[String],
    postinumero: Option[String],
    postitoimipaikka: Option[String]
) extends Hakija

object ToisenAsteenHakijaWithCombinedNimi {
  def apply(hakija: ToisenAsteenHakija): ToisenAsteenHakijaWithCombinedNimi = {
    new ToisenAsteenHakijaWithCombinedNimi(
      hakija = s"${hakija.hakijanSukunimi}, ${hakija.hakijanEtunimi}",
      turvakielto = hakija.turvakielto,
      kansalaisuus = hakija.kansalaisuus,
      oppijanumero = hakija.oppijanumero,
      hakemusOid = hakija.hakemusOid,
      oppilaitos = hakija.oppilaitos,
      toimipiste = hakija.toimipiste,
      hakukohteenNimi = hakija.hakukohteenNimi,
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
      pohjakoulutus = hakija.pohjakoulutus,
      markkinointilupa = hakija.markkinointilupa,
      julkaisulupa = hakija.julkaisulupa,
      sahkoinenViestintalupa = hakija.sahkoinenViestintalupa,
      lahiosoite = hakija.lahiosoite,
      postinumero = hakija.postinumero,
      postitoimipaikka = hakija.postitoimipaikka
    )
  }
}

case class KkHakija(
    hakijanSukunimi: String,
    hakijanEtunimi: String,
    hetu: Option[String],
    syntymaAika: Option[LocalDate],
    kansalaisuus: Kielistetty,
    oppijanumero: String,
    hakemusOid: String,
    toimipiste: Kielistetty,
    hakukohteenNimi: Kielistetty,
    hakukelpoisuus: Option[String],
    prioriteetti: Int,
    valintatieto: Option[String],
    ehdollisestiHyvaksytty: Option[Boolean],
    valintatiedonPvm: Option[LocalDate],
    valintatapajonot: List[Valintatapajono],
    vastaanottotieto: Option[String],
    viimVastaanottopaiva: Option[LocalDate],
    ensikertalainen: Option[Boolean],
    ilmoittautuminen: Option[String],
    pohjakoulutus: Option[String],
    maksuvelvollisuus: Option[String],
    hakemusmaksunTila: Option[String],
    julkaisulupa: Option[Boolean],
    markkinointilupa: Option[Boolean],
    sahkoinenViestintalupa: Option[Boolean],
    lahiosoite: Option[String],
    postinumero: Option[String],
    postitoimipaikka: Option[String],
    kotikunta: Kielistetty,
    asuinmaa: Kielistetty,
    puhelinnumero: Option[String],
    sahkoposti: Option[String],
    arvosanat: Map[String, String]
) extends Hakija

case class KkHakijaWithCombinedNimi(
    hakija: String,
    hetu: Option[String],
    syntymaAika: Option[LocalDate],
    kansalaisuus: Kielistetty,
    oppijanumero: String,
    hakemusOid: String,
    toimipiste: Kielistetty,
    hakukohteenNimi: Kielistetty,
    hakukelpoisuus: Option[String],
    prioriteetti: Int,
    valintatieto: Option[String],
    ehdollisestiHyvaksytty: Option[Boolean],
    valintatiedonPvm: Option[LocalDate],
    valintatapajonot: List[Valintatapajono],
    vastaanottotieto: Option[String],
    viimVastaanottopaiva: Option[LocalDate],
    ensikertalainen: Option[Boolean],
    ilmoittautuminen: Option[String],
    pohjakoulutus: Option[String],
    maksuvelvollisuus: Option[String],
    hakemusmaksunTila: Option[String],
    julkaisulupa: Option[Boolean],
    markkinointilupa: Option[Boolean],
    sahkoinenViestintalupa: Option[Boolean],
    lahiosoite: Option[String],
    postinumero: Option[String],
    postitoimipaikka: Option[String],
    kotikunta: Kielistetty,
    asuinmaa: Kielistetty,
    puhelinnumero: Option[String],
    sahkoposti: Option[String],
    arvosanat: Map[String, String]
) extends Hakija

object KkHakijaWithCombinedNimi {
  def apply(hakija: KkHakija): KkHakijaWithCombinedNimi = {
    new KkHakijaWithCombinedNimi(
      hakija = s"${hakija.hakijanSukunimi}, ${hakija.hakijanEtunimi}",
      hetu = hakija.hetu,
      syntymaAika = hakija.syntymaAika,
      kansalaisuus = hakija.kansalaisuus,
      oppijanumero = hakija.oppijanumero,
      hakemusOid = hakija.hakemusOid,
      toimipiste = hakija.toimipiste,
      hakukohteenNimi = hakija.hakukohteenNimi,
      hakukelpoisuus = hakija.hakukelpoisuus,
      prioriteetti = hakija.prioriteetti,
      valintatieto = hakija.valintatieto,
      ehdollisestiHyvaksytty = hakija.ehdollisestiHyvaksytty,
      valintatiedonPvm = hakija.valintatiedonPvm,
      valintatapajonot = hakija.valintatapajonot,
      vastaanottotieto = hakija.vastaanottotieto,
      viimVastaanottopaiva = hakija.viimVastaanottopaiva,
      ensikertalainen = hakija.ensikertalainen,
      ilmoittautuminen = hakija.ilmoittautuminen,
      pohjakoulutus = hakija.pohjakoulutus,
      maksuvelvollisuus = hakija.maksuvelvollisuus,
      hakemusmaksunTila = hakija.hakemusmaksunTila,
      julkaisulupa = hakija.julkaisulupa,
      markkinointilupa = hakija.markkinointilupa,
      sahkoinenViestintalupa = hakija.sahkoinenViestintalupa,
      lahiosoite = hakija.lahiosoite,
      postinumero = hakija.postinumero,
      postitoimipaikka = hakija.postitoimipaikka,
      kotikunta = hakija.kotikunta,
      asuinmaa = hakija.asuinmaa,
      puhelinnumero = hakija.puhelinnumero,
      sahkoposti = hakija.sahkoposti,
      arvosanat = hakija.arvosanat
    )
  }
}
