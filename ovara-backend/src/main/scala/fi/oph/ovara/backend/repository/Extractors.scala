package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.*
import fi.oph.ovara.backend.utils.ExtractorUtils.{
  extractAlkamiskausi,
  extractArray,
  extractDateOption,
  extractHakuaika,
  extractKielistetty,
  extractKoulutuksenAlkamisaika,
  extractMap,
  extractOpintojenlaajuus,
  extractValintatapajonot
}
import fi.oph.ovara.backend.utils.GenericOvaraJsonFormats
import org.json4s.jackson.Serialization.read
import slick.jdbc.*

import java.sql.Date
import java.time.LocalDate

trait Extractors extends GenericOvaraJsonFormats {
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
        hakukohteenNimi = extractKielistetty(r.nextStringOption()),
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

  implicit val getKorkeakouluKoulutuksetToteutuksetHakukohteetResult
      : GetResult[KorkeakouluKoulutusToteutusHakukohdeResult] = {
    GetResult(r => {
      KorkeakouluKoulutusToteutusHakukohdeResult(
        oppilaitosJaToimipiste = extractKielistetty(r.nextStringOption()),
        koulutuksenNimi = extractKielistetty(r.nextStringOption()),
        koulutusOid = r.nextString(),
        koulutuksenTila = r.nextStringOption(),
        koulutuskoodi = r.nextStringOption(),
        koulutuksenUlkoinenTunniste = r.nextStringOption(),
        tutkinnonTaso = r.nextIntOption(),
        opintojenLaajuus = extractOpintojenlaajuus(r.nextStringOption(), r.nextStringOption()),
        toteutuksenNimi = extractKielistetty(r.nextStringOption()),
        toteutusOid = r.nextString(),
        toteutuksenTila = r.nextStringOption(),
        toteutuksenUlkoinenTunniste = r.nextStringOption(),
        koulutuksenAlkamisaika = extractKoulutuksenAlkamisaika(
          r.nextStringOption(),
          r.nextStringOption(),
          r.nextStringOption(),
          r.nextStringOption()
        ),
        hakukohteenNimi = extractKielistetty(r.nextStringOption()),
        hakukohdeOid = r.nextString(),
        hakukohteenTila = r.nextStringOption(),
        hakukohteenUlkoinenTunniste = r.nextStringOption(),
        haunNimi = extractKielistetty(r.nextStringOption()),
        hakuaika = extractHakuaika(r.nextStringOption()),
        hakutapa = extractKielistetty(r.nextStringOption()),
        hakukohteenAloituspaikat = r.nextIntOption(),
        ensikertalaistenAloituspaikat = r.nextIntOption(),
        valintaperuste = extractKielistetty(r.nextStringOption())
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
      hylkaamisenTaiPeruuntumisenSyy = extractKielistetty(r.nextStringOption()),
      vastaanottotieto = r.nextStringOption(),
      viimVastaanottopaiva = extractDateOption(r.nextDateOption()),
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
      syntymaAika = extractDateOption(r.nextDateOption()),
      kansalaisuus = extractKielistetty(r.nextStringOption()),
      oppijanumero = r.nextString(),
      hakemusOid = r.nextString(),
      toimipiste = extractKielistetty(r.nextStringOption()),
      hakukohteenNimi = extractKielistetty(r.nextStringOption()),
      hakukelpoisuus = r.nextStringOption(),
      prioriteetti = r.nextInt(),
      valintatieto = r.nextStringOption(),
      ehdollisestiHyvaksytty = r.nextBooleanOption(),
      valintatiedonPvm = extractDateOption(r.nextDateOption()),
      valintatapajonot = extractValintatapajonot(r.nextStringOption()),
      vastaanottotieto = r.nextStringOption(),
      viimVastaanottopaiva = extractDateOption(r.nextDateOption()),
      ensikertalainen = r.nextBooleanOption(),
      ilmoittautuminen = r.nextStringOption(),
      pohjakoulutus = r.nextStringOption(),
      maksuvelvollisuus = r.nextStringOption(),
      hakemusmaksunTila = r.nextStringOption(),
      julkaisulupa = r.nextBooleanOption(),
      markkinointilupa = r.nextBooleanOption(),
      sahkoinenViestintalupa = r.nextBooleanOption(),
      lahiosoite = r.nextStringOption(),
      postinumero = r.nextStringOption(),
      postitoimipaikka = r.nextStringOption(),
      kotikunta = extractKielistetty(r.nextStringOption()),
      asuinmaa = extractKielistetty(r.nextStringOption()),
      puhelinnumero = r.nextStringOption(),
      sahkoposti = r.nextStringOption(),
      arvosanat = extractMap(r.nextStringOption())
    )
  )

  private def extractHakeneetHyvaksytytVastaanottaneetCommonFields(
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

  private def extractKkHakeneetHyvaksytytVastaanottaneetCommonFields(
      r: PositionedResult
  ): (Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) = {
    (
      r.nextInt(), // hakijat
      r.nextInt(), // ensisijaisia
      r.nextInt(), // ensikertalaisia
      r.nextInt(), // hyvaksytyt
      r.nextInt(), // vastaanottaneet
      r.nextInt(), // lasna
      r.nextInt(), // poissa
      r.nextInt(), // ilmYht
      r.nextInt(), // maksuvelvollisia
      r.nextInt(), // valinnanAloituspaikat
      r.nextInt(), // aloituspaikat
      r.nextInt(), // toive1
      r.nextInt(), // toive2
      r.nextInt(), // toive3
      r.nextInt(), // toive4
      r.nextInt(), // toive5
      r.nextInt()  // toive6
    )
  }

  implicit val getKkHakeneetHyvaksytytVastaanottaneetOrgNimellaResult
      : GetResult[KkHakeneetHyvaksytytVastaanottaneetOrganisaatioNimella] = GetResult { r =>
    val hakukohdeNimi    = extractKielistetty(r.nextStringOption())
    val organisaatioNimi = extractKielistetty(r.nextStringOption())
    val commonFields     = extractKkHakeneetHyvaksytytVastaanottaneetCommonFields(r)

    KkHakeneetHyvaksytytVastaanottaneetOrganisaatioNimella(
      otsikko = hakukohdeNimi,
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
      commonFields._16,
      commonFields._17
    )
  }

  implicit val getKkHakeneetHyvaksytytVastaanottaneetToimipisteittainResult
      : GetResult[KkHakeneetHyvaksytytVastaanottaneetToimipisteittain] = GetResult { r =>
    val toimipiste       = r.nextString()
    val organisaatioNimi = extractKielistetty(r.nextStringOption())
    val commonFields     = extractKkHakeneetHyvaksytytVastaanottaneetCommonFields(r)

    KkHakeneetHyvaksytytVastaanottaneetToimipisteittain(
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
      commonFields._16,
      commonFields._17
    )
  }

  implicit val getKkHakeneetHyvaksytytResult: GetResult[KkHakeneetHyvaksytytVastaanottaneetResult] = GetResult { r =>
    val otsikko      = extractKielistetty(r.nextStringOption())
    val commonFields = extractKkHakeneetHyvaksytytVastaanottaneetCommonFields(r)

    KkHakeneetHyvaksytytVastaanottaneetResult(
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
      commonFields._16,
      commonFields._17
    )
  }
}
