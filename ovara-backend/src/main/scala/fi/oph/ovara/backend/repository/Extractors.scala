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
  
  private def mapKielistetty(nimiFi: String, nimiSv: String, nimiEn: String): Kielistetty = {
    Map(
      Fi -> nimiFi,
      Sv -> nimiSv,
      En -> nimiEn
    )
  }

  implicit val getOpetuskieliResult: GetResult[Opetuskieli] = GetResult(r =>
    Opetuskieli(
      koodiarvo = r.nextString(),
      nimi = mapKielistetty(r.nextString(), r.nextString(), r.nextString())
    )
  )
  
  implicit val getHakuResult: GetResult[Haku] = GetResult(r =>
    Haku(
      haku_oid = r.nextString(),
      haku_nimi = extractKielistetty(r.nextStringOption())
    )
  )

  implicit val getKoodiResult: GetResult[Koodi] = GetResult(r =>
    Koodi(
      koodiarvo = r.nextString(),
      koodinimi = extractKielistetty(r.nextStringOption())
    )
  )

  implicit val getHakukohdeResult: GetResult[Hakukohde] = GetResult(r =>
    Hakukohde(
      hakukohde_oid = r.nextString(),
      hakukohde_nimi = extractKielistetty(r.nextStringOption())
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
    val childOid  = r.nextString()

    OrganisaatioParentChild(
      parent_oid = parentOid,
      child_oid = childOid,
      organisaatio = extractOrganisaatio(childOid, r)
    )
  })

  implicit val getKoulutuksetToteutuksetHakukohteetResult: GetResult[OrganisaationKoulutusToteutusHakukohde] = {
    GetResult(r => {
      val kth = KoulutusToteutusHakukohdeResult(
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
      val organisaatioOid = r.nextStringOption()

      OrganisaationKoulutusToteutusHakukohde(
        organisaatio_oid = organisaatioOid,
        koulutusToteutusHakukohde = kth
      )
    })
  }

  implicit val getOrganisaatioHierarkiaResult: GetResult[OrganisaatioHierarkia] = GetResult(r =>
    OrganisaatioHierarkia(
      organisaatio_oid = r.nextString(),
      organisaatio_nimi = extractKielistetty(r.nextStringOption()),
      organisaatiotyypit = extractArray(r.nextStringOption()),
      oppilaitostyyppi = r.nextStringOption(),
      tila = r.nextString(),
      parent_oids = extractArray(r.nextStringOption()),
      children = r.nextStringOption().map(read[List[OrganisaatioHierarkia]]).getOrElse(List())
    )
  )

  implicit val getHakijaResult: GetResult[Hakija] = GetResult(r =>
    Hakija(
      hakijanSukunimi = r.nextString(),
      hakijanEtunimi = r.nextString(),
      turvakielto = r.nextBooleanOption(),
      kansalaisuus = extractKielistetty(r.nextStringOption()),
      oppijanumero = r.nextString(),
      hakemusOid = r.nextString(),
      oppilaitos = extractKielistetty(r.nextStringOption()),
      toimipiste = extractKielistetty(r.nextStringOption()),
      hakukohteenNimi = extractKielistetty(r.nextStringOption()),
      hakukohdeOid = r.nextString(),
      prioriteetti = r.nextInt(),
      kaksoistutkintoKiinnostaa = r.nextBooleanOption(),
      urheilijatutkintoKiinnostaa = r.nextBooleanOption(),
      valintatieto = r.nextString(),
      varasija = r.nextStringOption(),
      kokonaispisteet = r.nextStringOption(),
      hylkTaiPerSyy = extractKielistetty(r.nextStringOption()),
      vastaanottotieto = r.nextStringOption(),
      viimVastaanottopaiva = {
        r.nextDateOption() match {
          case Some(date) => Some(date.toLocalDate)
          case None       => None
        }
      },
      ilmoittautuminen = r.nextStringOption(),
      harkinnanvaraisuus = r.nextStringOption(),
      soraAiempi = r.nextBooleanOption(),
      soraTerveys = r.nextBooleanOption(),
      pohjakoulutus = extractKielistetty(r.nextStringOption()),
      markkinointilupa = r.nextBooleanOption(),
      julkaisulupa = r.nextBooleanOption(),
      sahkoinenViestintaLupa = r.nextBooleanOption(),
      lahiosoite = r.nextString(),
      postinumero = r.nextString(),
      postitoimipaikka = r.nextString()
    )
  )
}
