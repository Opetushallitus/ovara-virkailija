package fi.oph.ovara.backend.yos

import fi.oph.ovara.backend.domain.{
  Fi,
  KKPaatettavaOpiskeluoikeusEntity,
  KKSitovastiVastaanottanut,
  KkPaatettavaOpiskeluoikeus,
  YosHenkilo
}
import fi.oph.ovara.backend.raportointi.dto.{
  buildKkPaatettavatOpiskeluoikeudetParamsForExcel,
  KkPaatettavatOpiskeluoikeudetParams
}
import fi.oph.ovara.backend.repository.{KkPaatettavatOpiskeluoikeudetRepository, ReadOnlyDatabase}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, CommonExcelParams, ExcelWriter}
import fi.oph.ovara.backend.yos.YosPredicate
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}

import java.time.{LocalDate, LocalDateTime}
import scala.util.{Failure, Success, Try}

@Service
class YosService(
  kkPaatettavatOpiskeluoikeudetRepository: KkPaatettavatOpiskeluoikeudetRepository,
  @Autowired db: ReadOnlyDatabase = null
) {

  val LOG: Logger = LoggerFactory.getLogger(classOf[YosService])

  def getPaattyvatOpiskeluOikeudet(
    orgOids: List[String],
    params: KkPaatettavatOpiskeluoikeudetParams
  ): List[KkPaatettavaOpiskeluoikeus] = {
    val opiskeluoikeudet         = getOpiskeluoikeudet(orgOids, params)
    val sitovastiVastaanottaneet = getSitovastivastaanottaneet(opiskeluoikeudet)
    val yossiinKuuluvat: List[(KKPaatettavaOpiskeluoikeusEntity, KKSitovastiVastaanottanut)] = opiskeluoikeudet
      .map(o => {
        sitovastiVastaanottaneet
          .find(v =>
            v.oppijanumero.equals(o.opiskelijaAvain)
              && YosPredicate.onkoOikeusKoulutusAsteenMukaanYosinPiirissa(o, v)
          )
          .map(v => (o, v))
      })
      .filter(_.isDefined)
      .map(_.get)
    val yossiinKuuluvatHenkilot = getYossinPiiriinKuuluvatHenkilot(yossiinKuuluvat, params)
    yossiinKuuluvat
      .map((o, v) =>
        yossiinKuuluvatHenkilot
          .find(h => h.oppijanumero.equals(o.opiskelijaAvain))
          .map(h =>
            KkPaatettavaOpiskeluoikeus(
              oppijanumero = v.oppijanumero,
              hetu = h.hetu,
              syntymaAika = h.syntymaAika.orNull,
              sukunimi = h.sukunimi,
              etunimet = h.etunimet,
              kutsumanimi = h.kutsumanimi,
              opiskelijaAvain = o.opiskelijaAvain,
              opiskeluoikeusAvain = o.opiskeluoikeusAvain,
              opiskeluoikeudenNimi = o.opiskeluoikeudenNimi,
              opiskeluoikeudenPaattymispvm = Some(LocalDate.of(2026, 12, 31)),
              opiskeluoikeudenViimeisinTila = o.opiskeluoikeudenViimeisinTila,
              hakemusOid = v.hakemusOid,
              hakuOid = v.hakuOid,
              hakuNimi = v.haunNimi,
              hakukohdeOid = v.hakukohdeOid,
              hakukohdeNimi = v.hakukohdeNimi,
              oppilaitosOid = v.oppilaitosOid,
              oppilaitosNimi = v.oppilaitosNimi,
              vastaanottoAjankohta = v.vastaanottoAjankohta.get,
              koulutusluokitusKoodit = "12345",
              uudenOpiskeluoikeudenAlkamispvm = LocalDate.of(2026, 9, 1)
            )
          )
      )
      .filter(_.isDefined)
      .map(_.get)
  }

  private def getOpiskeluoikeudet(
    orgOids: List[String],
    params: KkPaatettavatOpiskeluoikeudetParams
  ): List[KKPaatettavaOpiskeluoikeusEntity] = {
    val opiskeluoikeudet = db
      .run(
        kkPaatettavatOpiskeluoikeudetRepository
          .opiskeluoikeudetQuery(orgOids, params.oppijanumero, params.opiskeluoikeudenTila),
        "opiskeluoikeudetQuery"
      )
      .filter(o => o.koulutusaste.isDefined)
      .toList
    LOG.info(s"Löytyi ${opiskeluoikeudet.size} kpl opiskeluoikeutta parametreilla $params")
    opiskeluoikeudet
  }

  private def getSitovastivastaanottaneet(
    opiskeluoikeudet: List[KKPaatettavaOpiskeluoikeusEntity]
  ): List[KKSitovastiVastaanottanut] = {
    val henkiloOids              = opiskeluoikeudet.map(o => o.opiskelijaAvain).distinct
    val sitovastiVastaanottaneet =
      if (henkiloOids.isEmpty) List.empty
      else
        db
          .run(
            kkPaatettavatOpiskeluoikeudetRepository.vastaanottaneetQuery(henkiloOids),
            "sitovastiVastaanottaneetQuery"
          )
          .filter(v => v.koulutusasteet.nonEmpty)
          .toList
    LOG.info(
      s"Löytyi ${sitovastiVastaanottaneet.size} sitovaa vastaanottoa opiskeluoikeuksien henkilo oideille ${henkiloOids.size} kpl"
    )
    sitovastiVastaanottaneet
  }

  private def getYossinPiiriinKuuluvatHenkilot(
    yossiinKuuluvat: List[(KKPaatettavaOpiskeluoikeusEntity, KKSitovastiVastaanottanut)],
    params: KkPaatettavatOpiskeluoikeudetParams
  ): List[YosHenkilo] = {
    val yossiinKuuluvatHenkiloOidit = yossiinKuuluvat.map((o, _) => o.opiskelijaAvain).distinct
    val yossiinKuuluvatHenkilot     =
      if (yossiinKuuluvatHenkiloOidit.isEmpty) List.empty
      else
        db
          .run(
            kkPaatettavatOpiskeluoikeudetRepository.henkilotQuery(yossiinKuuluvatHenkiloOidit, params),
            "yosHenkilotQuery"
          )
          .toList
    LOG.info(
      s"Löytyi ${yossiinKuuluvatHenkiloOidit.size} kpl henkiloa, joille löytyi ${yossiinKuuluvatHenkilot.size} kpl henkilöä parametreilla $params"
    )
    yossiinKuuluvatHenkilot
  }
}
