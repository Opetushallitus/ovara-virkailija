package fi.oph.ovara.backend.external.toisenasteenhakijat

import org.json4s.jackson.JsonMethods
import org.json4s.JString
import org.json4s.jvalue2monadic

import java.time.OffsetDateTime
import scala.util.Try

private val urheilijaLukioLinjaCodes: Set[String] =
  Set("lukiolinjaterityinenkoulutustehtava_0105", "lukiopainotukset_0105")

private val LinjaWithoutVersion = """([^#]+)(?:#.*)?""".r

private val valintaTilaMapping: Map[String, String] = Map(
  "HYVAKSYTTY"                     -> "1",
  "HARKINNANVARAISESTI_HYVAKSYTTY" -> "1",
  "VARASIJALTA_HYVAKSYTTY"         -> "1",
  "VARALLA"                        -> "2",
  "HYLATTY"                        -> "3",
  "PERUNUT"                        -> "4",
  "PERUUNTUNUT"                    -> "4",
  "PERUUTETTU"                     -> "5"
)

private val vastaanotonTilaMapping: Map[String, String] = Map(
  "VASTAANOTTANUT_SITOVASTI"      -> "3",
  "EHDOLLISESTI_VASTAANOTTANUT"   -> "3",
  "PERUNUT"                       -> "4",
  "EI_VASTAANOTETTU_MAARA_AIKANA" -> "5",
  "PERUUTETTU"                    -> "6"
)

private val ilmoittautumisenTilaMapping: Map[String, String] = Map(
  "EI_TEHTY"              -> "1",
  "LASNA_KOKO_LUKUVUOSI"  -> "2",
  "POISSA_KOKO_LUKUVUOSI" -> "3",
  "EI_ILMOITTAUTUNUT"     -> "4",
  "LASNA_SYKSY"           -> "5",
  "POISSA_SYKSY"          -> "6",
  "LASNA"                 -> "7",
  "POISSA"                -> "8"
)

/**
 * Per-hakukohde info attached to the toinen aste yhteishaku hakemus row.
 *  Mirrors the JSON shape of `gen_hakemus_toinenaste_yhteishaku.hakukohteet[]`.
 */
case class HakemusHakukohde(
  oid: String,
  terveys: Option[Boolean],
  aiempiPeruminen: Option[Boolean],
  kiinnostunutKaksoistutkinnosta: Option[Boolean]
)

case class HakijaRow(
  oppijanumero: String,
  hakemusOid: String,
  sahkoposti: String,
  puhelin: String,
  lahiosoite: String,
  postinumero: String,
  postitoimipaikka: String,
  hakuOid: String,
  etunimet: Option[String],
  kutsumanimi: Option[String],
  sukunimi: Option[String],
  hetu: Option[String],
  asuinmaa: Option[String],
  kansalaisuudet: Seq[String],
  kotikunta: Option[String],
  sukupuoli: Option[Int],
  koulutusmarkkinointilupa: Option[Boolean],
  kiinnostunutOppisopimuksesta: Option[Boolean],
  sahkoinenviestintalupa: Option[Boolean],
  valintatuloksenJulkaisulupa: Option[Boolean],
  jatetty: Option[OffsetDateTime],
  muokattu: Option[OffsetDateTime],
  aidinkieli: Option[String],
  opetuskieli: Option[String],
  pohjakoulutus: Option[String],
  todistusvuosi: Option[String],
  lisapistekoulutus: Option[String],
  vuosi: Option[Int],
  kausi: Option[String],
  huoltaja1: Option[Huoltaja],
  huoltaja2: Option[Huoltaja],
  hakukohteetTiedot: Map[String, HakemusHakukohde],
  kiinnostunutAmmatillinen: Option[Boolean],
  urheilijaKysymyksetLukio: Option[UrheilijanLisakysymykset],
  urheilijaKysymyksetAmm: Option[UrheilijanLisakysymykset]
) {
  def asHakija(
    hakutoiveet: Seq[HakijaHakutoive],
    lahtokoulu: Option[LahtokouluRow]
  ): ToisenAsteenHakija =
    ToisenAsteenHakija(
      oppijanumero = oppijanumero,
      sahkoposti = sahkoposti,
      matkapuhelin = puhelin,
      lahiosoite = lahiosoite,
      postinumero = postinumero,
      postitoimipaikka = postitoimipaikka,
      hetu = hetu,
      sukunimi = sukunimi,
      etunimet = etunimet,
      kutsumanimi = kutsumanimi,
      maa = asuinmaa,
      kansalaisuudet = kansalaisuudet,
      kotikunta = kotikunta,
      sukupuoli = sukupuoli.map(_.toString),
      aidinkieli = aidinkieli,
      opetuskieli = opetuskieli,
      koulutusmarkkinointilupa = koulutusmarkkinointilupa,
      kiinnostunutoppisopimuksesta = kiinnostunutOppisopimuksesta,
      sahkoisenAsioinninLupa = sahkoinenviestintalupa,
      huoltaja1 = huoltaja1,
      huoltaja2 = huoltaja2,
      hakemus = HakijaHakemus(
        hakemusnumero = hakemusOid,
        hakutoiveet = hakutoiveet,
        vuosi = vuosi.map(_.toString),
        kausi = kausi,
        hakemuksenJattopaiva = jatetty,
        hakemuksenMuokkauspaiva = muokattu,
        lahtokoulu = lahtokoulu.flatMap(_.oppilaitosOid),
        lahtokoulunnimi = lahtokoulu.flatMap(_.oppilaitosNimi),
        luokka = lahtokoulu.flatMap(_.luokka),
        luokkataso = lahtokoulu.flatMap(_.suoritusTyyppi).flatMap(LahtokouluRow.suoritusTyyppiToLuokkataso),
        pohjakoulutus = pohjakoulutus,
        todistusvuosi = todistusvuosi,
        lisapistekoulutus = lisapistekoulutus,
        julkaisulupa = valintatuloksenJulkaisulupa
      )
    )
}

case class LahtokouluRow(
  hakemusOid: String,
  oppilaitosOid: Option[String],
  oppilaitosNimi: Option[String],
  luokka: Option[String],
  suoritusTyyppi: Option[String]
)

object LahtokouluRow {
  private val passthroughSuoritusTyyppi: Set[String] = Set(
    "AIKUISTEN_PERUSOPETUS",
    "PERUSOPETUKSEEN_VALMISTAVA_OPETUS",
    "TELMA",
    "TUVA",
    "VAPAA_SIVISTYSTYO"
  )

  def suoritusTyyppiToLuokkataso(s: String): Option[String] = s match {
    case "VUOSILUOKKA_7"                   => Some("7")
    case "VUOSILUOKKA_8"                   => Some("8")
    case "VUOSILUOKKA_9"                   => Some("9")
    case v if passthroughSuoritusTyyppi(v) => Some(v)
    case _                                 => None
  }
}

case class HakijaHakutoiveRow(
  hakemusOid: String,
  hakukohdeOid: String,
  hakutoivenumero: Int,
  jarjestyspaikkaOid: Option[String],
  oppilaitos: Option[String],
  hakukohteenLinjaJson: Option[String],
  jarjestaaUrheilijanAmmkoulutusta: Option[Boolean],
  koulutusKoodiurit: Seq[String],
  valintatieto: Option[String],
  vastaanottotieto: Option[String],
  ilmoittautumisenTila: Option[String],
  pisteet: Option[BigDecimal]
) {
  def asHakutoive(
    koodistot: Map[String, KoodistoArvo],
    urheilijanLisakysymyksetLukio: Option[UrheilijanLisakysymykset],
    urheilijanLisakysymyksetAmm: Option[UrheilijanLisakysymykset],
    kiinnostunutAmmatillinen: Option[Boolean],
    hakukohteetTiedot: Map[String, HakemusHakukohde]
  ): HakijaHakutoive = {
    val linja            = parseLinja(hakukohteenLinjaJson)
    val isUrheilijaLukio = linja.exists(urheilijaLukioLinjaCodes.contains)
    val isAmmUrheilija   = jarjestaaUrheilijanAmmkoulutusta.contains(true) && kiinnostunutAmmatillinen.getOrElse(false)

    // Prioriteettijärjestys: jos kyseessä urheilijalukio, käytetään lomakkeelta lukiopuolen kysymyksiä. Jos ei lukio, käytetään ammatillisia jos relevanttia.
    val urheilijanLisakysymykset =
      if (isUrheilijaLukio) urheilijanLisakysymyksetLukio
      else if (isAmmUrheilija) urheilijanLisakysymyksetAmm
      else None

    val hakukohdeTiedot = hakukohteetTiedot.get(hakukohdeOid)

    HakijaHakutoive(
      hakukohdeOid = hakukohdeOid,
      hakujno = hakutoivenumero,
      oppilaitos = oppilaitos,
      opetuspiste = jarjestyspaikkaOid,
      koulutus = koulutusKoodiurit.headOption.flatMap(koodistot.get),
      urheilijanammatillinenkoulutus =
        jarjestaaUrheilijanAmmkoulutusta.map(_ && kiinnostunutAmmatillinen.getOrElse(false)),
      urheilijanLisakysymykset = urheilijanLisakysymykset,
      terveys = hakukohdeTiedot.flatMap(_.terveys),
      aiempiperuminen = hakukohdeTiedot.flatMap(_.aiempiPeruminen),
      kaksoistutkinto = hakukohdeTiedot.flatMap(_.kiinnostunutKaksoistutkinnosta),
      valinta = valintatieto.flatMap(valintaTilaMapping.get),
      vastaanotto = vastaanottotieto.map(vastaanotonTilaMapping.get).getOrElse(Some("1")),
      lasnaolo = ilmoittautumisenTila.flatMap(ilmoittautumisenTilaMapping.get),
      yhteispisteet = pisteet
    )
  }

  /**
   * Extract the `linja` koodi from the hakukohteen_linja jsonb and drop any
   *  `#<version>` suffix so it can be compared against an unversioned koodi set.
   */
  private def parseLinja(jsonOpt: Option[String]): Option[String] =
    jsonOpt
      .flatMap(json => Try(JsonMethods.parse(json) \ "linja").toOption)
      .collect { case JString(LinjaWithoutVersion(base)) => base }
}
