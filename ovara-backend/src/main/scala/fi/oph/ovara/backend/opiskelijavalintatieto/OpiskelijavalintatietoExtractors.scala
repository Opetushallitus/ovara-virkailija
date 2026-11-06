package fi.oph.ovara.backend.opiskelijavalintatieto

import fi.oph.ovara.backend.repository.Extractors
import fi.oph.ovara.backend.utils.ExtractorUtils.extractArray
import slick.jdbc.GetResult

trait OpiskelijavalintatietoExtractors extends Extractors {
  implicit val getOppijaRow: GetResult[OppijaRow] = GetResult(r =>
    OppijaRow(
      oppijanumero = r.nextString(),
      hetu = r.nextString(),
      syntymaaika = r.nextString(),
      sukunimi = r.nextString(),
      etunimet = r.nextString()
    )
  )

  implicit val getHakemus: GetResult[HakemusRow] = GetResult(r =>
    HakemusRow(
      oppijanumero = r.nextString(),
      hakemusOid = r.nextString(),
      hakuOid = r.nextString(),
      hakuNimi = getKielistetty(r),
      kohdejoukkoKoodiuri = r.nextStringOption(),
      hakutapakoodiuri = r.nextStringOption(),
      hakukohdeOid = r.nextString(),
      hakukohdeNimi = getKielistetty(r),
      tarjoajanOid = r.nextStringOption(),
      tarjoajanNimi = getKielistetty(r),
      koulutuksenAlkamiskausiuri = r.nextStringOption(),
      koulutuksenAlkamisvuosi = r.nextIntOption(),
      valinnanTila = r.nextStringOption(),
      vastaanottoTila = r.nextStringOption(),
      ilmoituksenTila = r.nextStringOption(),
      johtaaTutkintoon = r.nextBooleanOption(),
      opintojenLaajuusYksikkoKoodiuri = r.nextStringOption(),
      opintojenLaajuus = r.nextBigDecimalOption(),
      opintojenLaajuusMin = r.nextBigDecimalOption(),
      opintojenLaajuusMax = r.nextBigDecimalOption(),
      eqf = extractArray(r.nextStringOption()),
      nqf = extractArray(r.nextStringOption())
    )
  )
}
