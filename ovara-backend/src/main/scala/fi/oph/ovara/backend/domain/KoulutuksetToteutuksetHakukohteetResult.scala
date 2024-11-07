package fi.oph.ovara.backend.domain

case class KoulutuksetToteutuksetHakukohteetResult(
    hakukohdeOid: String,
    hakukohdeNimi: Kielistetty,
    koulutuksenTila: Option[String],
    toteutuksenTila: Option[String],
    hakukohteenTila: Option[String],
    aloituspaikat: Option[Int],
    onValintakoe: Option[Boolean]
)
