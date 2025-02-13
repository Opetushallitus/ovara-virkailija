package fi.oph.ovara.backend.domain

import java.time.LocalDate

case class Hakija(
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
