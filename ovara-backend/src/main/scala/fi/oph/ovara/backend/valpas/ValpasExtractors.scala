package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.repository.Extractors
import fi.oph.ovara.backend.utils.ExtractorUtils.extractArray
import slick.jdbc.GetResult
import org.json4s.jackson.Serialization.read

class ValpasExtractors extends Extractors with ValpasFormats {
  implicit val getHakemus: GetResult[HakemusRow] = GetResult { r =>
    HakemusRow(
      hakuOid = r.nextString(),
      hakuajat = getHakuajat(r),
      hakukierrosPaattyy = getOffsetDateTime(r),
      hakutapaKoodiuri = r.nextString(),
      haunNimi = getKielistetty(r),
      oppijanumero = r.nextString(),
      hakemusOid = r.nextString(),
      muokattu = getOffsetDateTime(r),
      sahkoposti = r.nextString(),
      puhelin = r.nextString(),
      asuinmaa = r.nextString(),
      lahiosoite = r.nextString(),
      postinumero = r.nextString(),
      postitoimipaikka = r.nextString()
    )
  }

  implicit val getHakutoiveRow: GetResult[HakutoiveRow] = GetResult { r =>
    HakutoiveRow(
      hakemusOid = r.nextString(),
      hakukohdeOid = r.nextString(),
      hakutoivenumero = r.nextInt(),
      hakukohdeNimi = getKielistetty(r),
      hakukohdeOrganisaatio = r.nextString(),
      organisaatioNimi = getKielistetty(r),
      koulutusOid = r.nextString(),
      koulutusNimi = getKielistetty(r),
      hakukohdeKoulutuskoodi = extractArray(r.nextStringOption()),
      vastaanottotieto = r.nextString(),
      ilmoittautumistila = r.nextString(),
      valintatila = r.nextString(),
      harkinnanvaraisuus = r.nextString(),
      valintatapajonoId = r.nextString(),
      alin_hyvaksytty_pistemaara = r.nextBigDecimal(),
      pisteet = r.nextBigDecimalOption(),
      varasijanNumero = r.nextIntOption()
    )
  }

  implicit val getKoodistoArvo: GetResult[KoodistoArvo] = GetResult { r =>
    KoodistoArvo(
      versioituUri = r.nextString(),
      koodiarvo = r.nextString(),
      koodistoUri = r.nextString(),
      koodistoVersio = r.nextInt(),
      nimi = getKielistetty(r)
    )
  }

  implicit val getHakuajat: GetResult[List[ValpasHakuaika]] = GetResult { r =>
    r.nextStringOption().map(read[List[ValpasHakuaika]]).getOrElse(List.empty)
  }
}
