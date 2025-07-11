package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.{KkHakija, Koodi}
import fi.oph.ovara.backend.repository.{CommonRepository, KkHakijatRepository, ReadOnlyDatabase}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, ExcelWriter}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}

import scala.util.{Failure, Success, Try}

@Component
@Service
class KkHakijatService(
    kkHakijatRepository: KkHakijatRepository,
    commonRepository: CommonRepository,
    userService: UserService,
    commonService: CommonService,
    lokalisointiService: LokalisointiService
) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[KkHakijatService]);

  @Autowired
  val db: ReadOnlyDatabase = null

  def get(
      haut: List[String],
      oppilaitokset: List[String],
      toimipisteet: List[String],
      hakukohteet: List[String],
      valintatiedot: List[String],
      vastaanottotiedot: List[String],
      hakukohderyhmat: List[String],
      kansalaisuusluokat: List[String],
      markkinointilupa: Option[Boolean],
      naytaYoArvosanat: Boolean,
      naytaHetu: Boolean,
      naytaPostiosoite: Boolean
  ): Either[String, XSSFWorkbook] = {
    val user          = userService.getEnrichedUserDetails
    val asiointikieli = user.asiointikieli.getOrElse("fi")

    val translations = lokalisointiService.getOvaraTranslations(asiointikieli)

    val authorities               = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getKayttooikeusOids(authorities)
    val isOphPaakayttaja          = AuthoritiesUtil.hasOPHPaakayttajaRights(kayttooikeusOrganisaatiot)

    val orgOidsForQuery = commonService.getAllowedOrgOidsFromOrgSelection(
      kayttooikeusOrganisaatioOids = kayttooikeusOrganisaatiot,
      toimipisteOids = toimipisteet,
      oppilaitosOids = oppilaitokset
    )

    val allowedHakukohderyhmat = if (isOphPaakayttaja) {
      hakukohderyhmat
    } else {
      kayttooikeusOrganisaatiot intersect hakukohderyhmat
    }

    Try {
      val queryResult = db.run(
        kkHakijatRepository.selectWithParams(
          kayttooikeusOrganisaatiot = orgOidsForQuery,
          hakukohderyhmat = allowedHakukohderyhmat,
          haut = haut,
          oppilaitokset = oppilaitokset,
          toimipisteet = toimipisteet,
          hakukohteet = hakukohteet,
          valintatiedot = valintatiedot,
          vastaanottotiedot = vastaanottotiedot,
          kansalaisuusluokat = kansalaisuusluokat,
          markkinointilupa = markkinointilupa
        ),
        "kkHakijatRepository.selectWithParams"
      )

      val sortedList = queryResult.sortBy(resultRow => (resultRow.hakijanSukunimi.toLowerCase, resultRow.hakijanEtunimi.toLowerCase(), resultRow.oppijanumero))

      val yokokeet = {
        if(Some(naytaYoArvosanat).getOrElse(false))
          db.run(commonRepository.selectDistinctYokokeet, "selectDistinctYokokeet")
        else
          Vector()
      }

      ExcelWriter.writeKkHakijatRaportti(
        sortedList,
        asiointikieli,
        translations,
        Some(naytaYoArvosanat),
        Some(naytaHetu),
        Some(naytaPostiosoite),
        yokokeet
      )
    } match {
      case Success(excelFile) => Right(excelFile)
      case Failure(exception) =>
        LOG.error("Error generating Excel report", exception)
        Left("virhe.tietokanta")
    }
  }
}
