package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.repository.{HakijatRepository, OvaraDatabase}
import fi.oph.ovara.backend.utils.{AuthoritiesUtil, ExcelWriter}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.{Component, Service}

@Component
@Service
class HakijatService(
    hakijatRepository: HakijatRepository,
    userService: UserService,
    commonService: CommonService,
    lokalisointiService: LokalisointiService
) {
  val LOG: Logger = LoggerFactory.getLogger(classOf[HakijatService]);

  @Autowired
  val db: OvaraDatabase = null

  def get(
      haku: List[String],
      oppilaitokset: List[String],
      toimipisteet: List[String],
      hakukohteet: List[String],
      vastaanottotieto: List[String],
      markkinointilupa: Option[Boolean],
      julkaisulupa: Option[Boolean]
  ): XSSFWorkbook = {
    val user          = userService.getEnrichedUserDetails
    val asiointikieli = user.asiointikieli.getOrElse("fi")

    val translations = lokalisointiService.getOvaraTranslations(asiointikieli)

    val authorities               = user.authorities
    val kayttooikeusOrganisaatiot = AuthoritiesUtil.getOrganisaatiot(authorities)

    val orgOidsForQuery = commonService.getAllowedOrgsFromOrgSelection(
      kayttooikeusOrganisaatioOids = kayttooikeusOrganisaatiot,
      toimipisteOids = toimipisteet,
      oppilaitosOids = oppilaitokset
    )

    val query = hakijatRepository.selectWithParams(
      kayttooikeusOrganisaatiot = orgOidsForQuery,
      haut = haku,
      oppilaitokset = oppilaitokset,
      toimipisteet = toimipisteet,
      hakukohteet = hakukohteet,
      vastaanottotieto = vastaanottotieto,
      markkinointilupa = markkinointilupa,
      julkaisulupa = julkaisulupa
    )

    val queryResult     = db.run(query, "hakijatRepository.selectWithParams")
    val groupedByHloOid = queryResult.groupBy(_.oppijanumero)

    ExcelWriter.writeHakijatRaportti(
      groupedByHloOid,
      asiointikieli,
      translations
    )
  }
}
