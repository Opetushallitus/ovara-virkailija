package fi.oph.ovara.backend.domain

sealed trait KoulutusToteutusHakukohde {
  val hakukohteenNimi: Kielistetty
  val hakukohdeOid: String
  val koulutuksenTila: Option[String]
  val toteutuksenTila: Option[String]
  val hakukohteenTila: Option[String]
  val aloituspaikat: Option[Int]
  val onValintakoe: Option[Boolean]
  val voiSuorittaaKaksoistutkinnon: Option[Boolean]
  val jarjestaaUrheilijanAmmKoulutusta: Option[Boolean]
}

case class KoulutusToteutusHakukohdeResult(
    hakukohteenNimi: Kielistetty,
    hakukohdeOid: String,
    koulutuksenTila: Option[String] = None,
    toteutuksenTila: Option[String] = None,
    hakukohteenTila: Option[String] = None,
    aloituspaikat: Option[Int] = None,
    onValintakoe: Option[Boolean] = None,
    voiSuorittaaKaksoistutkinnon: Option[Boolean] = None,
    jarjestaaUrheilijanAmmKoulutusta: Option[Boolean] = None
) extends KoulutusToteutusHakukohde

case class OrganisaationKoulutusToteutusHakukohde(
    organisaatio_oid: Option[String],
    koulutusToteutusHakukohde: KoulutusToteutusHakukohdeResult
)

case class KorkeakouluKoulutusToteutusHakukohdeResult(
    oppilaitosJaToimipiste: Kielistetty,
    koulutuksenNimi: Kielistetty,
    koulutusOid: String,
    koulutuksenTila: Option[String],
    koulutuskoodi: Option[String],
    koulutuksenUlkoinenTunniste: Option[String],
    opintojenLaajuus: Kielistetty,
    toteutuksenNimi: Kielistetty,
    toteutusOid: String,
    toteutuksenTila: Option[String],
    toteutuksenUlkoinenTunniste: Option[String],
    koulutuksenAlkamiskausiJaVuosi: Kielistetty
)
