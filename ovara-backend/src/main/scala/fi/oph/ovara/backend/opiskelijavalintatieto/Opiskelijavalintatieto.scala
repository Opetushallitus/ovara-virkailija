package fi.oph.ovara.backend.opiskelijavalintatieto

import fi.oph.ovara.backend.domain.Kielistetty

case class Opiskelijavalintatieto(
    oppijanumero: String,
    hetu: String,
    syntymaaika: String,
    sukunimi: String,
    etunimet: String,
    hakemukset: Iterable[Hakemus]
)

case class Hakemus(
    hakemusOid: String,
    haku: Nimetty,
    haunKohdejoukko: Option[String],
    hakutapa: Option[String],
    hakutoiveet: Iterable[Hakutoive]
)

case class Hakutoive(
    hakukohde: Nimetty,
    tarjoaja: Option[Nimetty],
    koulutuksenAlkamiskausiUri: Option[String],
    koulutuksenAlkamisvuosi: Option[String],
    valinnanTila: Option[String],
    vastaanotonTila: Option[String],
    ilmoittautumisenTila: Option[String]
)

case class Nimetty(oid: String, nimi: Kielistetty)

case class OppijaRow(
    oppijanumero: String,
    hetu: String,
    syntymaaika: String,
    sukunimi: String,
    etunimet: String
) {
  def asOpiskelijavalintatieto(hakemukset: Iterable[Hakemus]): Opiskelijavalintatieto = Opiskelijavalintatieto(
    oppijanumero,
    hetu,
    syntymaaika,
    sukunimi,
    etunimet,
    hakemukset
  )
}

case class HakemusRow(
    oppijanumero: String,
    hakemusOid: String,
    hakuOid: String,
    hakuNimi: Kielistetty,
    kohdejoukkoKoodiuri: Option[String],
    hakutapakoodiuri: Option[String],
    hakukohdeOid: String,
    hakukohdeNimi: Kielistetty,
    tarjoajanOid: Option[String],
    tarjoajanNimi: Kielistetty,
    koulutuksenAlkamiskausiuri: Option[String],
    koulutuksenAlkamisvuosi: Option[String],
    valinnanTila: Option[String],
    vastaanottoTila: Option[String],
    ilmoituksenTila: Option[String]
) {

  def asHakutoive: Hakutoive = Hakutoive(
    Nimetty(hakukohdeOid, hakukohdeNimi),
    tarjoajanOid.map(oid => Nimetty(oid, tarjoajanNimi)),
    koulutuksenAlkamiskausiuri,
    koulutuksenAlkamisvuosi,
    valinnanTila,
    vastaanottoTila,
    ilmoituksenTila
  )

  def asHakemus(hakutoiveet: Seq[Hakutoive]): Hakemus = Hakemus(
    hakemusOid,
    Nimetty(hakuOid, hakuNimi),
    kohdejoukkoKoodiuri,
    hakutapakoodiuri,
    hakutoiveet
  )
}
