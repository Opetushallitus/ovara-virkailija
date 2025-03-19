package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.*
import fi.oph.ovara.backend.utils.ExtractorUtils.extractValintatapajonot
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

  implicit val getHakukohderyhmaResult: GetResult[Hakukohderyhma] = GetResult(r =>
    Hakukohderyhma(
      hakukohderyhma_oid = r.nextString(),
      hakukohderyhma_nimi = extractKielistetty(r.nextStringOption())
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

  def getNextDateOption(r: PositionedResult) = {
    r.nextDateOption() match {
      case Some(date) => Some(date.toLocalDate)
      case None       => None
    }
  }

  implicit val getToisenAsteenHakijaResult: GetResult[ToisenAsteenHakija] = GetResult(r =>
    ToisenAsteenHakija(
      hakijanSukunimi = r.nextString(),
      hakijanEtunimi = r.nextString(),
      turvakielto = r.nextBooleanOption(),
      kansalaisuus = extractKielistetty(r.nextStringOption()),
      oppijanumero = r.nextString(),
      hakemusOid = r.nextString(),
      oppilaitos = extractKielistetty(r.nextStringOption()),
      toimipiste = extractKielistetty(r.nextStringOption()),
      hakukohteenNimi = extractKielistetty(r.nextStringOption()),
      prioriteetti = r.nextInt(),
      kaksoistutkintoKiinnostaa = r.nextBooleanOption(),
      urheilijatutkintoKiinnostaa = r.nextBooleanOption(),
      valintatieto = r.nextStringOption(),
      varasija = r.nextStringOption(),
      kokonaispisteet = r.nextStringOption(),
      hylkTaiPerSyy = extractKielistetty(r.nextStringOption()),
      vastaanottotieto = r.nextStringOption(),
      viimVastaanottopaiva = getNextDateOption(r),
      ilmoittautuminen = r.nextStringOption(),
      harkinnanvaraisuus = r.nextStringOption(),
      soraAiempi = r.nextBooleanOption(),
      soraTerveys = r.nextBooleanOption(),
      pohjakoulutus = extractKielistetty(r.nextStringOption()),
      julkaisulupa = r.nextBooleanOption(),
      markkinointilupa = r.nextBooleanOption(),
      sahkoinenViestintalupa = r.nextBooleanOption(),
      lahiosoite = r.nextStringOption(),
      postinumero = r.nextStringOption(),
      postitoimipaikka = r.nextStringOption()
    )
  )

  implicit val getKkHakijaResult: GetResult[KkHakija] = GetResult(r =>
    KkHakija(
      hakijanSukunimi = r.nextString(),
      hakijanEtunimi = r.nextString(),
      hetu = r.nextStringOption(),
      syntymaAika = getNextDateOption(r),
      kansalaisuus = extractKielistetty(r.nextStringOption()),
      oppijanumero = r.nextString(),
      hakemusOid = r.nextString(),
      toimipiste = extractKielistetty(r.nextStringOption()),
      hakukohteenNimi = extractKielistetty(r.nextStringOption()),
      hakukelpoisuus = r.nextStringOption(),
      prioriteetti = r.nextInt(),
      valintatieto = r.nextStringOption(),
      ehdollisestiHyvaksytty = r.nextBooleanOption(),
      valintatiedonPvm = getNextDateOption(r),
      valintatapajonot = extractValintatapajonot(r.nextStringOption()),
      vastaanottotieto = r.nextStringOption(),
      viimVastaanottopaiva = getNextDateOption(r),
      ensikertalainen = r.nextBooleanOption(),
      ilmoittautuminen = r.nextStringOption(),
      pohjakoulutus = r.nextStringOption(),
      maksuvelvollisuus = r.nextStringOption(),
      julkaisulupa = r.nextBooleanOption(),
      markkinointilupa = r.nextBooleanOption(),
      sahkoinenViestintalupa = r.nextBooleanOption(),
      lahiosoite = r.nextStringOption(),
      postinumero = r.nextStringOption(),
      postitoimipaikka = r.nextStringOption(),
      kotikunta = extractKielistetty(r.nextStringOption()),
      puhelinnumero = r.nextStringOption(),
      sahkoposti = r.nextStringOption()
    )
  )

  def extractHakeneetHyvaksytytVastaanottaneetCommonFields(
      r: PositionedResult
  ): (Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) = {
    (
      r.nextInt(), // hakijat
      r.nextInt(), // ensisijaisia
      r.nextInt(), // varasija
      r.nextInt(), // hyvaksytyt
      r.nextInt(), // vastaanottaneet
      r.nextInt(), // lasna
      r.nextInt(), // poissa
      r.nextInt(), // ilmYht
      r.nextInt(), // aloituspaikat
      r.nextInt(), // toive1
      r.nextInt(), // toive2
      r.nextInt(), // toive3
      r.nextInt(), // toive4
      r.nextInt(), // toive5
      r.nextInt(), // toive6
      r.nextInt()  // toive7
    )
  }

  implicit val getHakeneetHyvaksytytVastaanottaneetHakukohteittainResult
      : GetResult[HakeneetHyvaksytytVastaanottaneetHakukohteittain] = GetResult { r =>
    val hakukohdeNimi    = extractKielistetty(r.nextStringOption())
    val organisaatioNimi = extractKielistetty(r.nextStringOption())
    val commonFields     = extractHakeneetHyvaksytytVastaanottaneetCommonFields(r)

    HakeneetHyvaksytytVastaanottaneetHakukohteittain(
      hakukohdeNimi = hakukohdeNimi,
      organisaatioNimi = organisaatioNimi,
      commonFields._1,
      commonFields._2,
      commonFields._3,
      commonFields._4,
      commonFields._5,
      commonFields._6,
      commonFields._7,
      commonFields._8,
      commonFields._9,
      commonFields._10,
      commonFields._11,
      commonFields._12,
      commonFields._13,
      commonFields._14,
      commonFields._15,
      commonFields._16
    )
  }

  implicit val getHakeneetHyvaksytytVastaanottaneetToimipisteittainResult
      : GetResult[HakeneetHyvaksytytVastaanottaneetToimipisteittain] = GetResult { r =>
    val toimipiste       = r.nextString()
    val organisaatioNimi = extractKielistetty(r.nextStringOption())
    val commonFields     = extractHakeneetHyvaksytytVastaanottaneetCommonFields(r)

    HakeneetHyvaksytytVastaanottaneetToimipisteittain(
      toimipiste = toimipiste,
      organisaatioNimi = organisaatioNimi,
      commonFields._1,
      commonFields._2,
      commonFields._3,
      commonFields._4,
      commonFields._5,
      commonFields._6,
      commonFields._7,
      commonFields._8,
      commonFields._9,
      commonFields._10,
      commonFields._11,
      commonFields._12,
      commonFields._13,
      commonFields._14,
      commonFields._15,
      commonFields._16
    )
  }

  implicit val getHakeneetHyvaksytytVastaanottaneetResult: GetResult[HakeneetHyvaksytytVastaanottaneetResult] =
    GetResult { r =>
      val otsikko      = extractKielistetty(r.nextStringOption())
      val commonFields = extractHakeneetHyvaksytytVastaanottaneetCommonFields(r)

      HakeneetHyvaksytytVastaanottaneetResult(
        otsikko = otsikko,
        commonFields._1,
        commonFields._2,
        commonFields._3,
        commonFields._4,
        commonFields._5,
        commonFields._6,
        commonFields._7,
        commonFields._8,
        commonFields._9,
        commonFields._10,
        commonFields._11,
        commonFields._12,
        commonFields._13,
        commonFields._14,
        commonFields._15,
        commonFields._16
      )
    }

}
