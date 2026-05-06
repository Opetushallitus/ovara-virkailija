package fi.oph.ovara.backend.valpas

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Service

import scala.util.Try

@Service
class ValpasService(repository: ValpasRepository, mapper: ObjectMapper) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ValpasService])

  def getValpasTiedot(oppijanumerot: List[String], vainAktiiviset: Boolean): Either[String, Seq[Hakemus]] = {
    Try {
      warnAboutMissingAktiivisuus(repository.selectHakemukset(oppijanumerot))
    }.map {
      case hakemukset if hakemukset.isEmpty =>
        LOG.info(s"Ei hakemuksia oppijanumeroilla $oppijanumerot")
        Nil
      case hakemusRows if vainAktiiviset =>
        combineWithHakutoiveet(hakemusRows.filter(_.isAktiivinen.contains(true)))
      case hakemusRows => combineWithHakutoiveet(hakemusRows)
    }.toEither.left.map { exception =>
      LOG.error("Error fetching Valpas information", exception)
      "virhe.tietokanta"
    }
  }

  private def combineWithHakutoiveet(hakemusRows: Seq[HakemusRow]): Seq[Hakemus] = {
    val hakemusOids = hakemusRows.map(_.hakemusOid).toSet

    val hakutoiveRows = repository.selectHakutoiveet(hakemusOids)
    validateSingleValintatapajono(hakutoiveRows)

    val koodiurit = Set.concat(
      hakemusRows.toSet.map(_.hakutapaKoodiuri),
      hakemusRows.map(h => ValpasService.toMaaKoodiUri(h.asuinmaa)),
      hakutoiveRows.flatMap(_.hakukohdeKoulutuskoodi)
    )
    val koodistot = repository.selectKoodistot(koodiurit).map(k => k.versioituUri -> k).toMap

    val hakutoiveet = hakutoiveRows.groupMap(_.hakemusOid)(_.asHakutoive(koodistot))
    hakemusRows.map(h => h.asHakemus(koodistot, hakutoiveet.getOrElse(h.hakemusOid, Seq.empty)))
  }

  private def warnAboutMissingAktiivisuus(hakemusRows: Seq[HakemusRow]): Seq[HakemusRow] = {
    hakemusRows.filter(_.isAktiivinen.isEmpty).foreach { h =>
      LOG.warn(
        "Haun aktiivisuutta ei pystytty päättelemään. " +
          s"Haku = ${h.hakuOid}, " +
          s"Hakukierros päättyy (PH_HKP) = ${h.hakukierrosPaattyy}, " +
          s"hakuajat (haun) = ${mapper.writeValueAsString(h.hakuajat)}"
      )
    }
    hakemusRows
  }

  private def validateSingleValintatapajono(hakutoiveRows: Seq[HakutoiveRow]): Unit = {
    val violating = hakutoiveRows
      .groupMap(r => (r.hakemusOid, r.hakukohdeOid))(_.valintatapajonoId)
      .filter { case (_, valintatapajonot) =>
        valintatapajonot.size > 1
      }

    if (violating.nonEmpty) {
      violating.foreach { case ((hakemusOid, hakukohdeOid), valintatapajonot) =>
        LOG.error(
          "Hakutoive kuului moneen valintatapajonoon, mutta peruskoulutuksen jälkeisessä yhteishaussa pitäisi olla vain yksi. " +
            s"Hakukohde = $hakukohdeOid, hakemus = $hakemusOid, valintatapajonot = ${valintatapajonot.mkString(",")}"
        )
      }
      throw DataConstraintException("Hakutoive kuului useaan valintatapajonoon.")
    }
  }
}

object ValpasService {
  private val MAAT_KOODIURI: String = "maatjavaltiot2"
  private val MAAT_KOODIVERSIO: Int = 2

  def toMaaKoodiUri(koodiarvo: String) = s"${MAAT_KOODIURI}_$koodiarvo#$MAAT_KOODIVERSIO"
}

case class DataConstraintException(msg: String) extends RuntimeException(msg)
