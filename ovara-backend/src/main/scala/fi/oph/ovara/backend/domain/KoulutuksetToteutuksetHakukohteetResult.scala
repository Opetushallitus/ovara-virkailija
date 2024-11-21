package fi.oph.ovara.backend.domain

case class KoulutuksetToteutuksetHakukohteetResult(
    hakukohdeNimi: Kielistetty,
    hakukohdeOid: String,
    koulutuksenTila: Option[String] = None,
    toteutuksenTila: Option[String] = None,
    hakukohteenTila: Option[String] = None,
    aloituspaikat: Option[Int] = None,
    onValintakoe: Option[Boolean] = None,
    voiSuorittaaKaksoistutkinnon: Option[Boolean] = None,
    jarjestaaUrheilijanAmmKoulutusta: Option[Boolean] = None
)
