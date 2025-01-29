package fi.oph.ovara.backend.domain

case class Hakija(
    hakijanNimi: String,
    turvakielto: Option[Boolean],
    kansalaisuus: Kielistetty,
    henkiloOid: String,
    hakemusOid: String,
    hakukohteenNimi: Kielistetty,
    prioriteetti: Int,
    kaksoistutkintoKiinnostaa: Option[Boolean],
    valinnanTila: String,
    soraAiempi: Option[String],
    soraTerveys: Option[String],
    markkinointilupa: Option[Boolean],
    julkaisulupa: Option[Boolean],
    sahkoinenViestintaLupa: Option[Boolean],
    lahiosoite: String,
    postinumero: String,
    postitoimipaikka: String
)
