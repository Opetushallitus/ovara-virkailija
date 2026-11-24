package fi.oph.ovara.backend.external.kkhakijat

import fi.oph.ovara.backend.repository.Extractors
import fi.oph.ovara.backend.utils.ExtractorUtils.extractArray
import fi.oph.ovara.backend.utils.GenericOvaraJsonFormats
import slick.jdbc.{GetResult, PositionedResult}

import scala.util.Try

class KKHakijatExtractors extends Extractors with GenericOvaraJsonFormats {
  implicit val getKKHakijaRow: GetResult[KKHakijaRow] = GetResult { r =>
    val oppijanumero                = r.nextString()
    val hakemusOid                  = r.nextString()
    val sahkoposti                  = r.nextString()
    val puhelin                     = r.nextString()
    val lahiosoite                  = r.nextStringOption().getOrElse("")
    val postinumero                 = r.nextStringOption().getOrElse("")
    val postitoimipaikka            = r.nextStringOption().getOrElse("")
    val hakuOid                     = r.nextString()
    val etunimet                    = r.nextStringOption()
    val kutsumanimi                 = r.nextStringOption()
    val sukunimi                    = r.nextStringOption()
    val hetu                        = r.nextStringOption()
    val asuinmaa                    = r.nextStringOption()
    val kansalaisuudet              = extractArray(r.nextStringOption())
    val kotikunta                   = r.nextStringOption()
    val sukupuoli                   = r.nextIntOption()
    val asiointikieli               = r.nextIntOption()
    val koulutusmarkkinointilupa    = r.nextBooleanOption()
    val valintatuloksenJulkaisulupa = r.nextBooleanOption()
    val jatetty                     = getOffsetDateTime(r)
    val muokattu                    = getOffsetDateTime(r)
    val pohjakoulutus               = extractArray(r.nextStringOption())
    val aidinkieli                  = normalizeKieliCode(r.nextStringOption())
    val syntymaaika                 = r.nextStringOption().flatMap(s => Try(java.time.LocalDate.parse(s)).toOption)
    val turvakielto                 = r.nextBooleanOption()
    val ensikertalainen             = r.nextBooleanOption()
    val hakuVuosi                   = r.nextIntOption()
    val hakuKausi                   = normalizeKausi(r.nextStringOption())

    // ONR:n master-henkilö; puuttuva master-oid tarkoittaa, ettei master-riviä ole.
    val masterOid = r.nextStringOption()
    val master    = MasterHenkiloRow(
      hetu = r.nextStringOption(),
      etunimet = r.nextStringOption(),
      sukunimi = r.nextStringOption(),
      sukupuoli = r.nextIntOption(),
      kansalaisuudet = extractArray(r.nextStringOption()),
      aidinkieli = normalizeKieliCode(r.nextStringOption()),
      syntymaaika = r.nextStringOption().flatMap(s => Try(java.time.LocalDate.parse(s)).toOption),
      turvakielto = r.nextBooleanOption()
    )

    KKHakijaRow(
      oppijanumero = oppijanumero,
      hakemusOid = hakemusOid,
      sahkoposti = sahkoposti,
      puhelin = puhelin,
      lahiosoite = lahiosoite,
      postinumero = postinumero,
      postitoimipaikka = postitoimipaikka,
      hakuOid = hakuOid,
      etunimet = etunimet,
      kutsumanimi = kutsumanimi,
      sukunimi = sukunimi,
      hetu = hetu,
      asuinmaa = asuinmaa,
      kansalaisuudet = kansalaisuudet,
      kotikunta = kotikunta,
      sukupuoli = sukupuoli,
      asiointikieli = asiointikieli,
      koulutusmarkkinointilupa = koulutusmarkkinointilupa,
      valintatuloksenJulkaisulupa = valintatuloksenJulkaisulupa,
      jatetty = jatetty,
      muokattu = muokattu,
      pohjakoulutus = pohjakoulutus,
      aidinkieli = aidinkieli,
      syntymaaika = syntymaaika,
      turvakielto = turvakielto,
      ensikertalainen = ensikertalainen,
      vuosi = hakuVuosi,
      kausi = hakuKausi,
      master = masterOid.map(_ => master)
    )
  }

  implicit val getKKHakemusRow: GetResult[KKHakemusRow] = GetResult { r =>
    KKHakemusRow(
      hakemusOid = r.nextString(),
      hakukohdeOid = r.nextString(),
      hakutoivenumero = r.nextInt(),
      organisaatioOid = r.nextStringOption(),
      jarjestyspaikkaOid = r.nextStringOption(),
      valintatieto = r.nextStringOption(),
      vastaanottotieto = r.nextStringOption(),
      ilmoittautumisenTila = r.nextStringOption(),
      lukuvuosimaksu = r.nextStringOption(),
      hakukohdeKkId = r.nextStringOption(),
      koulutusOid = r.nextStringOption(),
      koulutusKoodit = extractArray(r.nextStringOption()).flatMap(parseKoulutusKoodi),
      johtaaTutkintoon = r.nextBooleanOption(),
      toteutusAlkamisvuosi = r.nextIntOption(),
      toteutusAlkamiskausi = normalizeKausi(r.nextStringOption()),
      valinnanTila = r.nextStringOption(),
      valinnanAikaleima = getOffsetDateTime(r),
      pisteet = r.nextBigDecimalOption(),
      ehdollisestiHyvaksyttavissa = r.nextBooleanOption(),
      ehtoFI = r.nextStringOption(),
      ehtoSV = r.nextStringOption(),
      ehtoEN = r.nextStringOption(),
      valintatapajononTyyppi = r.nextStringOption(),
      valintatapajononNimi = r.nextStringOption(),
      valintatapajononTyyppiNimiFi = r.nextStringOption(),
      hKelpoisuusState = r.nextStringOption(),
      hKelpoisuusMaksuvelvollisuusState = r.nextStringOption(),
      merkitsevaJonoJulkaistavissa = r.nextBooleanOption(),
      hyvaksyttyHarkinnanvaraisesti = r.nextBooleanOption(),
      eiVarasijatayttoa = r.nextBooleanOption(),
      kaikkiJonotJulkaistu = r.nextBooleanOption(),
      ohjausparametrit = HaunOhjausparametrit(
        vastaanottoPaattyy = getOffsetDateTime(r),
        hakijakohtainenVastaanottoaikaPaivia = r.nextStringOption().flatMap(_.trim.toIntOption),
        valintaesitysHyvaksyttavissa = getOffsetDateTime(r),
        varasijasaannotVoimaan = getOffsetDateTime(r),
        kaikkiJonotSijoittelussa = getOffsetDateTime(r),
        sijoittelu = r.nextBoolean(),
        jarjestetytHakutoiveet = r.nextBoolean()
      ),
      yps = getYpsHakukohde(r)
    )
  }

  implicit val getYpsVastaanottoRow: GetResult[YpsVastaanottoRow] = GetResult { r =>
    YpsVastaanottoRow(
      hakemusOid = r.nextString(),
      hakukohdeOid = r.nextString(),
      yps = getYpsHakukohde(r)
    )
  }

  // selectHakemukset- ja selectYpsVastaanotot-kyselyjen YPS-sarakkeet samassa järjestyksessä
  // (ks. ExternalKKHakijatRepositoryTest).
  private def getYpsHakukohde(r: PositionedResult): YpsHakukohdeRow =
    YpsHakukohdeRow(
      hakuTila = r.nextStringOption(),
      hakukohdeTila = r.nextStringOption(),
      kohdejoukonTarkenne = r.nextStringOption(),
      koulutustyyppi = r.nextStringOption(),
      johtaaTutkintoon = r.nextBooleanOption(),
      hakukohteenAlkamiskausi = getAlkamiskausi(r),
      haunAlkamiskausi = getAlkamiskausi(r),
      toteutuksenAlkamiskausi = getAlkamiskausi(r)
    )

  private def getAlkamiskausi(r: PositionedResult): AlkamiskausiRow =
    AlkamiskausiRow(
      kausi = normalizeKausi(r.nextStringOption()),
      vuosi = r.nextIntOption(),
      alkamisaika = getOffsetDateTime(r)
    )

  implicit val getYlioppilasRow: GetResult[YlioppilasRow] = GetResult { r =>
    YlioppilasRow(
      hakemusOid = r.nextString(),
      onYlioppilas = r.nextBooleanOption(),
      valmistumisVuosi = r.nextIntOption()
    )
  }

  private def normalizeKieliCode(opt: Option[String]): Option[String] =
    opt
      .map(_.trim.stripPrefix("\"").stripSuffix("\"").toUpperCase)
      .filter(_.nonEmpty)

  private val KausiPattern = """kausi_([sk])(?:#\d+)?""".r

  private def normalizeKausi(opt: Option[String]): Option[String] =
    opt.collect { case KausiPattern(arvo) => arvo.toUpperCase }

  private val KoulutusKoodiPattern = """koulutus_(\d+)(?:#\d+)?""".r

  private def parseKoulutusKoodi(raw: String): Option[String] =
    raw match {
      case KoulutusKoodiPattern(digits) => Some(digits)
      case _                            => None
    }
}
