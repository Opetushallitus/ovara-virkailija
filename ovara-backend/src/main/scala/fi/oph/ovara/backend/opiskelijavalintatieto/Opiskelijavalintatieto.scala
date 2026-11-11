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
  nqf: Seq[Int],
  eqf: Seq[Int],
  opintojenLaajuus: Option[OpintojenLaajuus]
)

case class OpintojenLaajuus(
  yksikkoKoodiuri: Option[String],
  laajuus: Option[BigDecimal],
  laajuusMin: Option[BigDecimal],
  laajuusMax: Option[BigDecimal]
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
  opintojenLaajuus: Option[BigDecimal],
  opintojenLaajuusMin: Option[BigDecimal],
  opintojenLaajuusMax: Option[BigDecimal],
  eqf: Seq[Int],
  nqf: Seq[Int]
) {

  def asHakutoive: Hakutoive = Hakutoive(
    hakukohde = Nimetty(hakukohdeOid, hakukohdeNimi),
    tarjoaja = tarjoajanOid.map(oid => Nimetty(oid, tarjoajanNimi)),
    koulutuksenAlkamiskausiUri = koulutuksenAlkamiskausiuri,
    koulutuksenAlkamisvuosi = koulutuksenAlkamisvuosi,
    valinnanTila = valinnanTila,
    vastaanotonTila = vastaanottoTila,
    ilmoittautumisenTila = ilmoituksenTila,
    johtaaTutkintoon = johtaaTutkintoon,
    nqf = nqf,
    eqf = eqf,
    opintojenLaajuus = Option
      .when(
        Seq(opintojenLaajuusYksikkoKoodiuri, opintojenLaajuus, opintojenLaajuusMin, opintojenLaajuusMax).exists(
          _.isDefined
        )
      )(
        OpintojenLaajuus(
          yksikkoKoodiuri = opintojenLaajuusYksikkoKoodiuri,
          laajuus = opintojenLaajuus,
          laajuusMin = opintojenLaajuusMin,
          laajuusMax = opintojenLaajuusMax
        )
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
