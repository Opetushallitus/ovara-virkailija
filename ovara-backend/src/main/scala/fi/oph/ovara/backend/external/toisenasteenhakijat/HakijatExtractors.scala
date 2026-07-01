package fi.oph.ovara.backend.external.toisenasteenhakijat

import fi.oph.ovara.backend.repository.Extractors
import fi.oph.ovara.backend.utils.ExtractorUtils.extractArray
import slick.jdbc.GetResult

class HakijatExtractors extends Extractors {
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
    val vuosi                        = r.nextIntOption()
    val kausi                        = normalizeKausi(r.nextStringOption())

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
      vuosi = vuosi,
      kausi = kausi,
      huoltaja1 = huoltaja1,
      huoltaja2 = huoltaja2,
      kiinnostunutAmmatillinen = kiinnostunutAmmatillinen,
      urheilijaKysymyksetLukio = urheilijanLisakysymyksetLukio,
      urheilijaKysymyksetAmm = urheilijanLisakysymyksetAmm
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
      koulutusKoodiurit = extractArray(r.nextStringOption())
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

  private val KausiPattern = """kausi_([sk])(?:#\d+)?""".r

  private def normalizeKausi(opt: Option[String]): Option[String] =
    opt.collect { case KausiPattern(arvo) => arvo.toUpperCase }

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
