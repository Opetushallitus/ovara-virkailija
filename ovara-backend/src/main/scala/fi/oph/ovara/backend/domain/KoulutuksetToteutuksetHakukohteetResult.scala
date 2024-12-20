package fi.oph.ovara.backend.domain

sealed trait KoulutuksetToteutuksetHakukohteet {
  val hakukohdeNimi: Kielistetty
  val hakukohdeOid: String
  val koulutuksenTila: Option[String]
  val toteutuksenTila: Option[String]
  val hakukohteenTila: Option[String]
  val aloituspaikat: Option[Int]
  val onValintakoe: Option[Boolean]
  val voiSuorittaaKaksoistutkinnon: Option[Boolean]
  val jarjestaaUrheilijanAmmKoulutusta: Option[Boolean]
}

case class KoulutuksetToteutuksetHakukohteetResult(
    hakukohdeNimi: Kielistetty,
    hakukohdeOid: String,
    koulutuksenTila: Option[String] = None,
    toteutuksenTila: Option[String] = None,
    hakukohteenTila: Option[String] = None,
    aloituspaikat: Option[Int] = None,
    onValintakoe: Option[Boolean] = None,
    voiSuorittaaKaksoistutkinnon: Option[Boolean] = None,
    jarjestaaUrheilijanAmmKoulutusta: Option[Boolean] = None,
    jarjestyspaikka_oid: Option[String] = None,
    organisaatio_oid: Option[String],
    organisaatio_nimi: Kielistetty,
    organisaatiotyypit: List[String]
) extends KoulutuksetToteutuksetHakukohteet

case class OrganisaationKoulutuksetToteutuksetHakukohteet(
    organisaatio: Option[Organisaatio],
    koulutuksetToteutuksetHakukohteet: Vector[KoulutuksetToteutuksetHakukohteetResult]
)
