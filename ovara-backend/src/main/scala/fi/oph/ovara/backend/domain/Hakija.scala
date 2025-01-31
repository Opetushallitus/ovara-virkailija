package fi.oph.ovara.backend.domain

case class Hakija(
    hakijanNimi: String,
    turvakielto: Option[Boolean],
    kansalaisuus: Kielistetty,
    henkiloOid: String,
    hakemusOid: String,
    hakukohteenNimi: Kielistetty,
    hakukohdeOid: String,
    prioriteetti: Int,
    kaksoistutkintoKiinnostaa: Option[Boolean],
    valinnanTila: String,
    soraAiempi: Option[Boolean],
    soraTerveys: Option[Boolean],
    markkinointilupa: Option[Boolean],
    julkaisulupa: Option[Boolean],
    sahkoinenViestintaLupa: Option[Boolean],
    lahiosoite: String,
    postinumero: String,
    postitoimipaikka: String
)
