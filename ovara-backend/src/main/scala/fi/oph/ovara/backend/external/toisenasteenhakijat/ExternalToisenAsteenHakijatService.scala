package fi.oph.ovara.backend.external.toisenasteenhakijat

import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Service

import scala.util.Try

@Service
class ExternalToisenAsteenHakijatService(repository: ExternalToisenAsteenHakijatRepository) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ExternalToisenAsteenHakijatService])

  def getHakijat(
    hakuOid: String,
    hakukohdeOid: Option[String],
    organisaatioOid: Option[String],
    valintarajaus: Valintarajaus = Valintarajaus.HAKENEET,
    scope: KayttooikeusScope = KayttooikeusScope.paakayttaja
  ): Either[String, Seq[ToisenAsteenHakija]] = {
    Try {
      val hakijaRows =
        repository.selectHakijat(hakuOid, hakukohdeOid, organisaatioOid, valintarajaus)
      if (hakijaRows.isEmpty) {
        Nil
      } else {
        val hakemusOids          = hakijaRows.map(_.hakemusOid).toSet
        val hakutoiveRows        = repository.selectHakutoiveet(hakemusOids)
        val lahtokoulutByHakemus =
          repository.selectLahtokoulut(hakemusOids).map(lk => lk.hakemusOid -> lk).toMap

        val koodiUrit = hakutoiveRows.flatMap(_.koulutusKoodiurit).toSet
        val koodistot =
          if (koodiUrit.isEmpty) Map.empty[String, KoodistoArvo]
          else repository.selectKoodistot(koodiUrit).map(k => k.versioituUri -> k).toMap

        val hakutoiveetByHakemus = hakutoiveRows.groupBy(_.hakemusOid)
        hakijaRows.flatMap { row =>
          val matchingRows = hakutoiveetByHakemus
            .getOrElse(row.hakemusOid, Seq.empty)
            .filter(matchesFilters(_, hakukohdeOid, organisaatioOid, valintarajaus, scope))
          if (matchingRows.isEmpty) None
          else {
            val toiveet = matchingRows.map(
              _.asHakutoive(
                koodistot,
                row.urheilijaKysymyksetLukio,
                row.urheilijaKysymyksetAmm,
                row.kiinnostunutAmmatillinen,
                row.hakukohteetTiedot
              )
            )
            Some(row.asHakija(toiveet, lahtokoulutByHakemus.get(row.hakemusOid)))
          }
        }
      }
    }.toEither.left
      .map { exception =>
        LOG.error("Error fetching hakijat", exception)
        "virhe.tietokanta"
      }
  }

  private def matchesFilters(
    row: HakijaHakutoiveRow,
    hakukohdeOid: Option[String],
    organisaatioOid: Option[String], // Rajapinnan parametri
    valintarajaus: Valintarajaus,
    scope: KayttooikeusScope // Käyttäjän oikeudet
  ): Boolean = {
    val hakukohdeMatch = hakukohdeOid.forall(_ == row.hakukohdeOid)
    val orgMatch       = organisaatioOid.forall(o => row.jarjestyspaikkaOid.contains(o))
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
