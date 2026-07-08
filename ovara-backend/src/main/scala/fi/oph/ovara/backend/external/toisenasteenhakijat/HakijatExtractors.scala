package fi.oph.ovara.backend.external.toisenasteenhakijat

import fi.oph.ovara.backend.repository.Extractors
import fi.oph.ovara.backend.utils.ExtractorUtils.extractArray
import fi.oph.ovara.backend.utils.GenericOvaraJsonFormats
import org.json4s.jackson.JsonMethods
import org.json4s.jvalue2monadic
import org.json4s.{JArray, JBool, JObject, JString}
import slick.jdbc.GetResult

import scala.util.Try

class HakijatExtractors extends Extractors with GenericOvaraJsonFormats {
  implicit val getHakijaRow: GetResult[HakijaRow] = GetResult { r =>
    val oppijanumero                 = r.nextString()
    val hakemusOid                   = r.nextString()
    val sahkoposti                   = r.nextString()
    val puhelin                      = r.nextString()
    val lahiosoite                   = r.nextString()
    val postinumero                  = r.nextString()
    val postitoimipaikka             = r.nextString()
    val hakuOid                      = r.nextString()
    val etunimet                     = r.nextStringOption()
    val kutsumanimi                  = r.nextStringOption()
    val sukunimi                     = r.nextStringOption()
    val hetu                         = r.nextStringOption()
    val asuinmaa                     = r.nextStringOption()
    val kansalaisuudet               = extractArray(r.nextStringOption())
    val kotikunta                    = r.nextStringOption()
    val sukupuoli                    = r.nextIntOption()
    val koulutusmarkkinointilupa     = r.nextBooleanOption()
    val kiinnostunutOppisopimuksesta = r.nextBooleanOption()
    val sahkoinenviestintalupa       = r.nextBooleanOption()
    val valintatuloksenJulkaisulupa  = r.nextBooleanOption()
    val jatetty                      = getOffsetDateTime(r)
    val muokattu                     = getOffsetDateTime(r)
    val aidinkieli                   = normalizeKieliCode(r.nextStringOption())
    val opetuskieli                  = normalizeKieliCode(r.nextStringOption())
    val pohjakoulutus                = stripJsonQuotes(r.nextStringOption())
    val todistusvuosi                = stripJsonQuotes(r.nextStringOption())
    val hakukohdeVuosi               = r.nextIntOption()
    val hakukohdeKausi               = r.nextStringOption()
    val hakuVuosi                    = r.nextIntOption()
    val hakuKausi                    = r.nextStringOption()
    val toteutusVuosi                = r.nextIntOption()
    val toteutusKausi                = r.nextStringOption()

    val (vuosi, rawKausi) = pickVuosiKausiAtomically(
      (hakukohdeVuosi, hakukohdeKausi),
      (hakuVuosi, hakuKausi),
      (toteutusVuosi, toteutusKausi)
    )
    val kausi = normalizeKausi(rawKausi)

    val huoltaja1 = buildHuoltaja(
      etunimi = r.nextStringOption(),
      sukunimi = r.nextStringOption(),
      puhelin = r.nextStringOption(),
      sahkoposti = r.nextStringOption()
    )
    val huoltaja2 = buildHuoltaja(
      etunimi = r.nextStringOption(),
      sukunimi = r.nextStringOption(),
      puhelin = r.nextStringOption(),
      sahkoposti = r.nextStringOption()
    )

    val hakukohteetTiedot = parseHakukohteet(r.nextStringOption())

    val kiinnostunutAmmatillinen = r.nextBooleanOption()

    // SQL column order for both blocks:
    // laji, seura, liitto, sivulaji, keskiarvo, tamakausi, peruskoulu,
    // viimekausi, toissakausi, valmentaja_puh, valmentaja_nimi, valmentaja_email,
    // valmennusryhma_maajoukkue, valmennusryhma_piirijoukkue, valmennusryhma_seurajoukkue
    val urheilijanLisakysymyksetLukio = buildUrheilijanLisakysymykset(
      laji = r.nextStringOption(),
      seura = r.nextStringOption(),
      liitto = r.nextStringOption(),
      sivulaji = r.nextStringOption(),
      keskiarvo = r.nextStringOption(),
      tamakausi = r.nextStringOption(),
      peruskoulu = r.nextStringOption(),
      viimekausi = r.nextStringOption(),
      toissakausi = r.nextStringOption(),
      valmentajaPuh = r.nextStringOption(),
      valmentajaNimi = r.nextStringOption(),
      valmentajaEmail = r.nextStringOption(),
      maajoukkue = r.nextStringOption(),
      piirijoukkue = r.nextStringOption(),
      seurajoukkue = r.nextStringOption()
    )
    val urheilijanLisakysymyksetAmm = buildUrheilijanLisakysymykset(
      laji = r.nextStringOption(),
      seura = r.nextStringOption(),
      liitto = r.nextStringOption(),
      sivulaji = r.nextStringOption(),
      keskiarvo = r.nextStringOption(),
      tamakausi = r.nextStringOption(),
      peruskoulu = r.nextStringOption(),
      viimekausi = r.nextStringOption(),
      toissakausi = r.nextStringOption(),
      valmentajaPuh = r.nextStringOption(),
      valmentajaNimi = r.nextStringOption(),
      valmentajaEmail = r.nextStringOption(),
      maajoukkue = r.nextStringOption(),
      piirijoukkue = r.nextStringOption(),
      seurajoukkue = r.nextStringOption()
    )

    HakijaRow(
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
      koulutusmarkkinointilupa = koulutusmarkkinointilupa,
      kiinnostunutOppisopimuksesta = kiinnostunutOppisopimuksesta,
      sahkoinenviestintalupa = sahkoinenviestintalupa,
      valintatuloksenJulkaisulupa = valintatuloksenJulkaisulupa,
      jatetty = jatetty,
      muokattu = muokattu,
      aidinkieli = aidinkieli,
      opetuskieli = opetuskieli,
      pohjakoulutus = pohjakoulutus,
      todistusvuosi = todistusvuosi,
      vuosi = vuosi,
      kausi = kausi,
      huoltaja1 = huoltaja1,
      huoltaja2 = huoltaja2,
      hakukohteetTiedot = hakukohteetTiedot,
      kiinnostunutAmmatillinen = kiinnostunutAmmatillinen,
      urheilijaKysymyksetLukio = urheilijanLisakysymyksetLukio,
      urheilijaKysymyksetAmm = urheilijanLisakysymyksetAmm
    )
  }

  implicit val getLahtokouluRow: GetResult[LahtokouluRow] = GetResult { r =>
    LahtokouluRow(
      hakemusOid = r.nextString(),
      oppilaitosOid = r.nextStringOption(),
      oppilaitosNimi = r.nextStringOption(),
      luokka = r.nextStringOption(),
      suoritusTyyppi = r.nextStringOption()
    )
  }

  implicit val getHakijaHakutoiveRow: GetResult[HakijaHakutoiveRow] = GetResult { r =>
    HakijaHakutoiveRow(
      hakemusOid = r.nextString(),
      hakukohdeOid = r.nextString(),
      hakutoivenumero = r.nextInt(),
      jarjestyspaikkaOid = r.nextStringOption(),
      oppilaitos = r.nextStringOption(),
      hakukohteenLinjaJson = r.nextStringOption(),
      jarjestaaUrheilijanAmmkoulutusta = r.nextBooleanOption(),
      koulutusKoodiurit = extractArray(r.nextStringOption()),
      valintatieto = r.nextStringOption(),
      vastaanottotieto = r.nextStringOption(),
      ilmoittautumisenTila = r.nextStringOption()
    )
  }

  implicit val getHakijatKoodistoArvo: GetResult[KoodistoArvo] = GetResult { r =>
    KoodistoArvo(
      versioituUri = r.nextString(),
      koodiarvo = r.nextString(),
      koodistoUri = r.nextString(),
      koodistoVersio = r.nextInt(),
      nimi = getKielistetty(r)
    )
  }

  private def normalizeKieliCode(opt: Option[String]): Option[String] =
    opt
      .map(_.trim.stripPrefix("\"").stripSuffix("\"").toUpperCase)
      .filter(_.nonEmpty)

  private def stripJsonQuotes(opt: Option[String]): Option[String] =
    opt
      .map(_.trim.stripPrefix("\"").stripSuffix("\""))
      .filter(_.nonEmpty)

  private val KausiPattern = """kausi_([sk])(?:#\d+)?""".r

  private def normalizeKausi(opt: Option[String]): Option[String] =
    opt.collect { case KausiPattern(arvo) => arvo.toUpperCase }

  /** Parse the jsonb `hakukohteet` array into a Map keyed by `oid`. Malformed JSON → empty Map. */
  private def parseHakukohteet(jsonOpt: Option[String]): Map[String, HakemusHakukohde] = {
    def boolField(obj: JObject, name: String): Option[Boolean] =
      (obj \ name) match {
        case JBool(b) => Some(b)
        case _        => None
      }
    jsonOpt
      .flatMap(s => Try(JsonMethods.parse(s)).toOption)
      .collect { case JArray(items) => items }
      .getOrElse(Nil)
      .collect {
        case obj: JObject if (obj \ "oid").isInstanceOf[JString] =>
          val JString(oid) = (obj \ "oid"): @unchecked
          HakemusHakukohde(
            oid = oid,
            terveys = boolField(obj, "terveys"),
            aiempiPeruminen = boolField(obj, "aiempiPeruminen"),
            kiinnostunutKaksoistutkinnosta = boolField(obj, "kiinnostunutKaksoistutkinnosta")
          )
      }
      .map(h => h.oid -> h)
      .toMap
  }

  /**
   * Pick the first (vuosi, kausi) tuple where BOTH values are non-null.
   *  Returns (None, None) if no tuple qualifies. Atomic per source table —
   *  callers pass one tuple per source in priority order.
   */
  private def pickVuosiKausiAtomically(
    candidates: (Option[Int], Option[String])*
  ): (Option[Int], Option[String]) =
    candidates
      .find { case (v, k) => v.isDefined && k.isDefined }
      .getOrElse((None, None))

  private def buildHuoltaja(
    etunimi: Option[String],
    sukunimi: Option[String],
    puhelin: Option[String],
    sahkoposti: Option[String]
  ): Option[Huoltaja] = {
    val anyNonEmpty = Seq(etunimi, sukunimi, puhelin, sahkoposti).exists(_.exists(_.nonEmpty))
    if (!anyNonEmpty) None
    else
      Some(
        Huoltaja(
          etunimi = etunimi.getOrElse(""),
          sukunimi = sukunimi.getOrElse(""),
          puhelinnumero = puhelin.getOrElse(""),
          sahkoposti = sahkoposti.getOrElse("")
        )
      )
  }

  private def buildUrheilijanLisakysymykset(
    laji: Option[String],
    seura: Option[String],
    liitto: Option[String],
    sivulaji: Option[String],
    keskiarvo: Option[String],
    tamakausi: Option[String],
    peruskoulu: Option[String],
    viimekausi: Option[String],
    toissakausi: Option[String],
    valmentajaPuh: Option[String],
    valmentajaNimi: Option[String],
    valmentajaEmail: Option[String],
    maajoukkue: Option[String],
    piirijoukkue: Option[String],
    seurajoukkue: Option[String]
  ): Option[UrheilijanLisakysymykset] = {
    val all = Seq(
      laji,
      seura,
      liitto,
      sivulaji,
      keskiarvo,
      tamakausi,
      peruskoulu,
      viimekausi,
      toissakausi,
      valmentajaPuh,
      valmentajaNimi,
      valmentajaEmail,
      maajoukkue,
      piirijoukkue,
      seurajoukkue
    )
    if (all.forall(_.forall(_.isEmpty))) None
    else
      Some(
        UrheilijanLisakysymykset(
          peruskoulu = peruskoulu,
          keskiarvo = keskiarvo,
          tamakausi = tamakausi,
          viimekausi = viimekausi,
          toissakausi = toissakausi,
          sivulaji = sivulaji,
          valmennusryhma_seurajoukkue = seurajoukkue,
          valmennusryhma_piirijoukkue = piirijoukkue,
          valmennusryhma_maajoukkue = maajoukkue,
          valmentaja_nimi = valmentajaNimi,
          valmentaja_email = valmentajaEmail,
          valmentaja_puh = valmentajaPuh,
          laji = laji,
          liitto = liitto,
          seura = seura
        )
      )
  }
}
