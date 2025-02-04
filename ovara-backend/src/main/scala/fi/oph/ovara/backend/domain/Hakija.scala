package fi.oph.ovara.backend.domain

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
    valintatieto: String,
    vastaanottotieto: Option[String],
    soraAiempi: Option[Boolean],
    soraTerveys: Option[Boolean],
    markkinointilupa: Option[Boolean],
    julkaisulupa: Option[Boolean],
    sahkoinenViestintaLupa: Option[Boolean],
    lahiosoite: String,
    postinumero: String,
    postitoimipaikka: String
)
