package fi.oph.ovara.backend.external.toisenasteenhakijat

import fi.oph.ovara.backend.service.CommonService
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Service

import scala.util.Try

@Service
class ExternalToisenAsteenHakijatService(
  repository: ExternalToisenAsteenHakijatRepository,
  commonService: CommonService
) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ExternalToisenAsteenHakijatService])

  def getHakijat(
    hakuOid: String,
    hakukohdeOid: Option[String],
    organisaatioOid: Option[String],
    valintarajaus: Valintarajaus = Valintarajaus.HAKENEET,
    scope: KayttooikeusScope = KayttooikeusScope.paakayttaja
  ): Either[String, Seq[ToisenAsteenHakija]] = {
    Try {
      // Sekä rajapinnan organisaatioOid (rajaava parametri) että käyttäjän oikeudet vertaillaan hakukohteen
      // jarjestyspaikka_oidia vasten, joka on tyypillisesti toimipiste. Molemmat on siis
      // laajennettava lapsiorganisaatioihin, jotta koulutustoimija- tai oppilaitostason
      // valinta ja oikeus osuvat alempana oleviin järjestyspaikkoihin.
      val organisaatioOids =
        organisaatioOid.map(oid => commonService.getOrganisaatioidenJaLastenOidit(List(oid))).getOrElse(Nil)
      val expandedScope =
        if (scope.isPaakayttaja) scope
        else
          KayttooikeusScope.limited(commonService.getOrganisaatioidenJaLastenOidit(scope.allowedOrgOids.toList).toSet)

      val hakijaRows =
        repository.selectHakijat(hakuOid, hakukohdeOid, organisaatioOids, valintarajaus, expandedScope)
      if (hakijaRows.isEmpty) {
        Nil
      } else {
        val hakemusOids          = hakijaRows.map(_.hakemusOid).toSet
        val hakutoiveRows        = repository.selectHakutoiveet(hakemusOids)
        val lahtokoulutByHakemus =
          repository.selectLahtokoulut(hakemusOids).map(lk => lk.hakemusOid -> lk).toMap
        val yhteishakuTiedotByHakemus =
          repository.selectToisenAsteenYhteishakuTiedot(hakemusOids).map(y => y.hakemusOid -> y).toMap

        val koodiUrit = hakutoiveRows.flatMap(_.koulutusKoodiurit).toSet
        val koodistot =
          if (koodiUrit.isEmpty) Map.empty[String, KoodistoArvo]
          else repository.selectKoodistot(koodiUrit).map(k => k.versioituUri -> k).toMap

        val hakutoiveetByHakemus = hakutoiveRows.groupBy(_.hakemusOid)
        hakijaRows.flatMap { row =>
          val kaikkiToiveet = hakutoiveetByHakemus.getOrElse(row.hakemusOid, Seq.empty)
          val matchingRows  =
            kaikkiToiveet.filter(matchesFilters(_, hakukohdeOid, organisaatioOids, valintarajaus, expandedScope))
          if (matchingRows.isEmpty) None
          else {
            val yhteishakuTiedot = yhteishakuTiedotByHakemus.get(row.hakemusOid)
            val toiveet          = matchingRows.map(
              _.asHakutoive(
                koodistot,
                yhteishakuTiedot.flatMap(_.urheilijaKysymyksetLukio),
                yhteishakuTiedot.flatMap(_.urheilijaKysymyksetAmm),
                yhteishakuTiedot.flatMap(_.kiinnostunutAmmatillinen),
                yhteishakuTiedot.map(_.hakukohteetTiedot).getOrElse(Map.empty)
              )
            )
            Some(
              row.asHakija(
                toiveet,
                lahtokoulutByHakemus.get(row.hakemusOid),
                // Todo: vuosi ja kausi poimitaan haulta? Kts. Suoritusrekisterin nykytoteutus.
                vuosiKausiEhdokas(kaikkiToiveet)(_.hakukohdePresent, _.hakukohdeVuosi, _.hakukohdeKausi),
                vuosiKausiEhdokas(kaikkiToiveet)(_.toteutusPresent, _.toteutusVuosi, _.toteutusKausi),
                yhteishakuTiedot
              )
            )
          }
        }
      }
    }.toEither.left
      .map { exception =>
        LOG.error("Error fetching hakijat", exception)
        "virhe.tietokanta"
      }
  }

  /**
   * Hakemustason vuosi/kausi-ehdokas: pienin hakutoivenumero niistä hakutoiveista joilla lähderivi
   *  on olemassa. Vastaa poistettujen alikyselyiden `INNER JOIN ... ORDER BY hakutoivenumero LIMIT 1`
   *  -semantiikkaa: rivin puuttuminen ohittaa hakutoiveen, mutta olemassaolevan rivin NULL-arvo ei.
   *  hakukohdeOid toissijaisena järjestysperusteena, koska hakutoivenumero ei ole yksikäsitteinen.
   */
  private def vuosiKausiEhdokas(
    toiveet: Seq[HakijaHakutoiveRow]
  )(
    present: HakijaHakutoiveRow => Boolean,
    vuosi: HakijaHakutoiveRow => Option[Int],
    kausi: HakijaHakutoiveRow => Option[String]
  ): (Option[Int], Option[String]) =
    toiveet
      .filter(present)
      .sortBy(r => (r.hakutoivenumero, r.hakukohdeOid))
      .headOption
      .map(r => (vuosi(r), kausi(r)))
      .getOrElse((None, None))

  private def matchesFilters(
    row: HakijaHakutoiveRow,
    hakukohdeOid: Option[String],
    organisaatioOids: Seq[String], // Rajapinnan parametri lapsiorganisaatioineen
    valintarajaus: Valintarajaus,
    scope: KayttooikeusScope // Käyttäjän oikeudet lapsiorganisaatioineen
  ): Boolean = {
    val hakukohdeMatch = hakukohdeOid.forall(_ == row.hakukohdeOid)
    val orgMatch       = organisaatioOids.isEmpty || row.jarjestyspaikkaOid.exists(organisaatioOids.contains)
    val allowedMatch   = scope.isPaakayttaja || row.jarjestyspaikkaOid.exists(scope.allowedOrgOids.contains)
    val stateMatch     = valintarajaus match {
      case Valintarajaus.HAKENEET   => true
      case Valintarajaus.HYVAKSYTYT =>
        row.valintatieto.exists(HyvaksytytValintatiedot.contains)
      case Valintarajaus.VASTAANOTTANEET =>
        row.vastaanottotieto.contains("VASTAANOTTANUT_SITOVASTI")
    }
    hakukohdeMatch && orgMatch && allowedMatch && stateMatch
  }

  private val HyvaksytytValintatiedot: Set[String] =
    Set("HYVAKSYTTY", "HARKINNANVARAISESTI_HYVAKSYTTY", "VARASIJALTA_HYVAKSYTTY")
}
