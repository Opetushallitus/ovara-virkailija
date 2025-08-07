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
  val sahkoinenViestintalupa: Option[Boolean]
  val lahiosoite: Option[String]
  val postinumero: Option[String]
  val postitoimipaikka: Option[String]
  val sahkoposti: Option[String]
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
    valintatapajonokohtainenTila: Kielistetty,
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
    postitoimipaikka: Option[String],
    sahkoposti: Option[String]
) extends Hakija

case class KkHakija(
    hakijanSukunimi: String,
    hakijanEtunimi: String,
    turvakielto: Option[Boolean],
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


