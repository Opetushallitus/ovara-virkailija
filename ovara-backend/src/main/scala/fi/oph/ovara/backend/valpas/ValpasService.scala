package fi.oph.ovara.backend.valpas

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Service

import scala.util.Try

@Service
class ValpasService(repository: ValpasRepository, mapper: ObjectMapper) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ValpasService])

  def getValpasTiedot(oppijanumerot: List[String]): Either[String, Seq[Hakemus]] = {
    Try {
      repository.selectHakemukset(oppijanumerot)
    }.map {
      case hakemukset if hakemukset.isEmpty =>
        LOG.info("ASDF hakemukset.isEmpty")
        Nil
      case hakemukset =>
        LOG.info(s"ASDF hakemukset: ${mapper.writeValueAsString(hakemukset)}")
        val hakemusOids = hakemukset.map(_.hakemusOid).toSet

        val hakutoiveRows = repository.selectHakutoiveet(hakemusOids)

        val koodiurit = Set.concat(
          hakemukset.toSet.map(_.hakutapaKoodiuri),
          hakemukset.map(_.hakutyyppiKoodiuri),
          hakemukset.map(h => ValpasService.toMaaKoodiUri(h.asuinmaa)),
          hakutoiveRows.flatMap(_.hakukohdeKoulutuskoodi)
        )
        LOG.info(s"ASDF koodiurit: ${mapper.writeValueAsString(koodiurit)}")
        val koodistot = repository.selectKoodistot(koodiurit).map(k => k.versioituUri -> k).toMap
        LOG.info(s"ASDF koodistot: ${mapper.writeValueAsString(koodistot)}")

        val hakutoiveet = hakutoiveRows.groupBy(_.hakemusOid).view.mapValues(vals => vals.map(_.asHakutoive(koodistot)))
        hakemukset.map(h => h.asHakemus(koodistot, hakutoiveet.getOrElse(h.hakemusOid, Seq.empty)))
    }.toEither.left.map { exception =>
      LOG.error("Error fetching Valpas information", exception)
      "virhe.tietokanta"
    }
  }
}

object ValpasService {
  private val MAAT_KOODIURI: String = "maatjavaltiot2"
  private val MAAT_KOODIVERSIO: Int = 2

  def toMaaKoodiUri(koodiarvo: String) = s"${MAAT_KOODIURI}_$koodiarvo#$MAAT_KOODIVERSIO"
}
