package fi.oph.ovara.backend.valpas

import fi.oph.ovara.backend.repository.Extractors
import fi.oph.ovara.backend.utils.ExtractorUtils.extractArray
import slick.jdbc.GetResult

class ValpasExtractors extends Extractors {
  implicit val getHakemus: GetResult[HakemusRow] = GetResult { r =>
    HakemusRow(
      hakuOid = r.nextString(),
      haunAlku = getOffsetDateTime(r),
      haunLoppu = getOffsetDateTime(r),
      hakutapaKoodiuri = r.nextString(),
      hakutyyppiKoodiuri = r.nextString(),
      haunNimi = getKielistetty(r),
      oppijanumero = r.nextString(),
      hakemusOid = r.nextString(),
      muokattu = getOffsetDateTime(r),
      sahkoposti = r.nextString(),
      puhelin = r.nextString(),
      asuinmaa = r.nextString(),
      lahiosoite = r.nextString(),
      postinumero = r.nextString(),
      postitoimipaikka = r.nextString(),
      huoltajanNimi = r.nextStringOption(),
      huoltajanPuhelinnumero = r.nextStringOption(),
      huoltajanSahkoposti = r.nextStringOption()
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
      harkinnanvaraisuus = r.nextString()
    )
  }

  implicit val getKoodistoArvo: GetResult[KoodistoArvo] = GetResult { r =>
    KoodistoArvo(
      versioituUri = r.nextString(),
      koodiarvo = r.nextString(),
      koodistoUri = r.nextString(),
      koodistoVersio = r.nextInt(),
      nimi = getKielistetty(r),
      lyhytNimi = getKielistetty(r)
    )
  }
}
