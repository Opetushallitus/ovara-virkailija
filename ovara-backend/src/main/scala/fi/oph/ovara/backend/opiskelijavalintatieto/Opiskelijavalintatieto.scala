package fi.oph.ovara.backend.opiskelijavalintatieto

import fi.oph.ovara.backend.domain.Kielistetty

case class Opiskelijavalintatieto(
  oppijanumero: String,
  hetu: String,
  syntymaaika: String,
  sukunimi: String,
  etunimet: String,
  hakemukset: Seq[Hakemus]
)

case class Hakemus(
  hakemusOid: String,
  haku: Nimetty,
  haunKohdejoukko: Option[String],
  hakutapa: Option[String],
  hakutoiveet: Seq[Hakutoive]
)

case class Hakutoive(
  hakukohde: Nimetty,
  tarjoaja: Option[Nimetty],
  koulutuksenAlkamiskausiUri: Option[String],
  koulutuksenAlkamisvuosi: Option[Int],
  valinnanTila: Option[String],
  vastaanotonTila: Option[String],
  ilmoittautumisenTila: Option[String],
  johtaaTutkintoon: Option[Boolean],
  nqf: Option[String],
  eqf: Option[String],
  opintojenLaajuus: Option[OpintojenLaajuus]
)

case class OpintojenLaajuus(
  yksikkoKoodiuri: Option[String],
  laajuus: Option[String],
  laajuusMin: Option[String],
  laajuusMax: Option[String]
)

case class Nimetty(oid: String, nimi: Kielistetty)

case class OppijaRow(
  oppijanumero: String,
  hetu: String,
  syntymaaika: String,
  sukunimi: String,
  etunimet: String
) {
  def asOpiskelijavalintatieto(hakemukset: Seq[Hakemus]): Opiskelijavalintatieto = Opiskelijavalintatieto(
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
  koulutuksenAlkamisvuosi: Option[Int],
  valinnanTila: Option[String],
  vastaanottoTila: Option[String],
  ilmoituksenTila: Option[String],
  johtaaTutkintoon: Option[Boolean],
  opintojenLaajuusYksikkoKoodiuri: Option[String],
  opintojenLaajuus: Option[String],
  opintojenLaajuusMin: Option[String],
  opintojenLaajuusMax: Option[String],
  eqf: Option[String],
  nqf: Option[String]
) {

  def asHakutoive: Hakutoive = Hakutoive(
    Nimetty(hakukohdeOid, hakukohdeNimi),
    tarjoajanOid.map(oid => Nimetty(oid, tarjoajanNimi)),
    koulutuksenAlkamiskausiuri,
    koulutuksenAlkamisvuosi,
    valinnanTila,
    vastaanottoTila,
    ilmoituksenTila,
    johtaaTutkintoon,
    nqf,
    eqf,
    Option
      .when(
        Seq(opintojenLaajuusYksikkoKoodiuri, opintojenLaajuus, opintojenLaajuusMin, opintojenLaajuusMax).exists(
          _.isDefined
        )
      )(
        OpintojenLaajuus(opintojenLaajuusYksikkoKoodiuri, opintojenLaajuus, opintojenLaajuusMin, opintojenLaajuusMax)
      )
  )

  def asHakemus(hakutoiveet: Seq[Hakutoive]): Hakemus = Hakemus(
    hakemusOid,
    Nimetty(hakuOid, hakuNimi),
    kohdejoukkoKoodiuri,
    hakutapakoodiuri,
    hakutoiveet
  )
}
