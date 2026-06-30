package fi.oph.ovara.backend.external.toisenasteenhakijat

import fi.oph.ovara.backend.repository.Extractors
import fi.oph.ovara.backend.utils.ExtractorUtils.extractArray
import slick.jdbc.GetResult

class HakijatExtractors extends Extractors {
  implicit val getHakijaRow: GetResult[HakijaRow] = GetResult { r =>
    HakijaRow(
      oppijanumero = r.nextString(),
      hakemusOid = r.nextString(),
      sahkoposti = r.nextString(),
      puhelin = r.nextString(),
      lahiosoite = r.nextString(),
      postinumero = r.nextString(),
      postitoimipaikka = r.nextString(),
      hakuOid = r.nextString(),
      etunimet = r.nextStringOption(),
      kutsumanimi = r.nextStringOption(),
      sukunimi = r.nextStringOption(),
      hetu = r.nextStringOption(),
      asuinmaa = r.nextStringOption(),
      kansalaisuudet = extractArray(r.nextStringOption()),
      kotikunta = r.nextStringOption(),
      sukupuoli = r.nextIntOption(),
      koulutusmarkkinointilupa = r.nextBooleanOption(),
      kiinnostunutOppisopimuksesta = r.nextBooleanOption(),
      sahkoinenviestintalupa = r.nextBooleanOption(),
      valintatuloksenJulkaisulupa = r.nextBooleanOption(),
      jatetty = getOffsetDateTime(r),
      muokattu = getOffsetDateTime(r),
      aidinkieli = normalizeKieliCode(r.nextStringOption()),
      opetuskieli = normalizeKieliCode(r.nextStringOption()),
      vuosi = r.nextIntOption(),
      kausi = normalizeKausi(r.nextStringOption())
    )
  }

  private def normalizeKieliCode(opt: Option[String]): Option[String] =
    opt
      .map(_.trim.stripPrefix("\"").stripSuffix("\"").toUpperCase)
      .filter(_.nonEmpty)

  private val KausiPattern = """kausi_([sk])(?:#\d+)?""".r

  private def normalizeKausi(opt: Option[String]): Option[String] =
    opt.collect { case KausiPattern(arvo) => arvo.toUpperCase }

  implicit val getHakijaHakutoiveRow: GetResult[HakijaHakutoiveRow] = GetResult { r =>
    HakijaHakutoiveRow(
      hakemusOid = r.nextString(),
      hakukohdeOid = r.nextString(),
      hakutoivenumero = r.nextInt(),
      jarjestyspaikkaOid = r.nextStringOption(),
      oppilaitos = r.nextStringOption(),
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
}
