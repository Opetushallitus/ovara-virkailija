package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.*
import fi.oph.ovara.backend.utils.GenericOvaraJsonFormats
import org.json4s.jackson.Serialization.read
import slick.jdbc.*

trait Extractors extends GenericOvaraJsonFormats {
  private def extractKielistetty(json: Option[String]): Kielistetty =
    json.map(read[Map[Kieli, String]]).getOrElse(Map())

  private def extractArray(json: Option[String]): List[String] = {
    json.map(read[List[String]]).getOrElse(List())
  }

  implicit val getHakuResult: GetResult[Haku] = GetResult(r =>
    Haku(
      haku_oid = r.nextString(),
      haku_nimi = extractKielistetty(r.nextStringOption())
    )
  )

  implicit val getOrganisaatioPerOrganisaatiotyyppiResult: GetResult[OrganisaatioPerOrganisaatiotyyppi] = GetResult(r =>
    OrganisaatioPerOrganisaatiotyyppi(
      organisaatio_oid = r.nextString(),
      organisaatio_nimi = extractKielistetty(r.nextStringOption()),
      organisaatiotyyppi = r.nextString()
    )
  )

  implicit val getOrganisaatioResult: GetResult[Organisaatio] = GetResult(r =>
    Organisaatio(
      organisaatio_oid = r.nextString(),
      organisaatio_nimi = extractKielistetty(r.nextStringOption()),
      organisaatiotyypit = extractArray(r.nextStringOption())
    )
  )

  implicit val getOrganisaatioParentChildResult: GetResult[OrganisaatioParentChild] = GetResult(r => {
    def extractOrganisaatio(orgOid: String, r: PositionedResult) =
      Organisaatio(
        orgOid,
        extractKielistetty(r.nextStringOption()),
        extractArray(r.nextStringOption())
      )

    val parentOid = r.nextString()
    val child_oid = r.nextString()

    OrganisaatioParentChild(
      parent_oid = parentOid,
      child_oid = child_oid,
      organisaatio = extractOrganisaatio(child_oid, r)
    )
  })

  implicit val getKoulutuksetToteutuksetHakukohteetResult: GetResult[KoulutuksetToteutuksetHakukohteetResult] = {
    GetResult(r =>
      KoulutuksetToteutuksetHakukohteetResult(
        hakukohdeNimi = extractKielistetty(r.nextStringOption()),
        hakukohdeOid = r.nextString(),
        koulutuksenTila = r.nextStringOption(),
        toteutuksenTila = r.nextStringOption(),
        hakukohteenTila = r.nextStringOption(),
        aloituspaikat = r.nextIntOption(),
        onValintakoe = r.nextBooleanOption(),
        voiSuorittaaKaksoistutkinnon = r.nextBooleanOption(),
        jarjestaaUrheilijanAmmKoulutusta = r.nextBooleanOption(),
        jarjestyspaikka_oid = r.nextStringOption(),
        organisaatio_oid = r.nextStringOption(),
        organisaatio_nimi = extractKielistetty(r.nextStringOption()),
        organisaatiotyypit = extractArray(r.nextStringOption())
      )
    )
  }

  implicit val getOrganisaatioHierarkiaResult: GetResult[OrganisaatioHierarkia] = GetResult(r =>
    OrganisaatioHierarkia(
      organisaatio_oid = r.nextString(),
      organisaatio_nimi = extractKielistetty(r.nextStringOption()),
      organisaatiotyypit = extractArray(r.nextStringOption()),
      parent_oids = extractArray(r.nextStringOption()),
      children = r.nextStringOption().map(read[List[OrganisaatioHierarkia]]).getOrElse(List())
    )
  )
}
