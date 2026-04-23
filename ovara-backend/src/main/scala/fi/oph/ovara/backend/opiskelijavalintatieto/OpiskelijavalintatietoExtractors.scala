package fi.oph.ovara.backend.opiskelijavalintatieto

import fi.oph.ovara.backend.repository.Extractors
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
      ilmoituksenTila = r.nextStringOption()
    )
  )
}
