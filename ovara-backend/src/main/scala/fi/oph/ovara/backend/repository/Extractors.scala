package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.{Haku, Kieli, Kielistetty, KoulutuksetToteutuksetHakukohteetResult, Organisaatio, OrganisaatioParentChild}
import fi.oph.ovara.backend.utils.GenericOvaraJsonFormats
import org.json4s.jackson.Serialization.read
import slick.jdbc.GetResult

trait Extractors extends GenericOvaraJsonFormats {
  private def extractKielistetty(json: Option[String]): Kielistetty = json.map(read[Map[Kieli, String]]).getOrElse(Map())

  implicit val getHakuResult: GetResult[Haku] = GetResult(r =>
    Haku(
      haku_oid = r.nextString(),
      haku_nimi = extractKielistetty(r.nextStringOption())
    )
  )

  implicit val getOrganisaatioResult: GetResult[Organisaatio] = GetResult(r =>
    Organisaatio(
      organisaatio_oid = r.nextString(),
      organisaatio_nimi = extractKielistetty(r.nextStringOption()),
      organisaatiotyyppi = r.nextString()
    )
  )

  implicit val getOrganisaatioParentChildResult: GetResult[OrganisaatioParentChild] = GetResult(r =>
    OrganisaatioParentChild(
      parent_oid = r.nextString(),
      child_oid = r.nextString(),
    )
  )

  implicit val getKoulutuksetToteutuksetHakukohteetResult: GetResult[KoulutuksetToteutuksetHakukohteetResult] =
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
        jarjestaaUrheilijanAmmKoulutusta = r.nextBooleanOption()
      )
    )
}
