package fi.oph.ovara.backend.service

import fi.oph.ovara.backend.domain.KoulutuksetToteutuksetHakukohteetResult
import fi.oph.ovara.backend.repository.{KoulutuksetToteutuksetHakukohteetRepository, OvaraDatabase}
import fi.oph.ovara.backend.utils.AuthoritiesUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class KoulutuksetToteutuksetHakukohteetService(
    koulutuksetToteutuksetHakukohteetRepository: KoulutuksetToteutuksetHakukohteetRepository
) {

  @Autowired
  val db: OvaraDatabase = null

  def get(
      alkamiskausi: List[String],
      haku: List[String],
      koulutuksenTila: Option[String],
      toteutuksenTila: Option[String],
      hakukohteenTila: Option[String],
      valintakoe: Option[Boolean]
  ): Vector[KoulutuksetToteutuksetHakukohteetResult] = {
    val authentication           = SecurityContextHolder.getContext.getAuthentication
    val authorities              = authentication.getAuthorities
    val raportointiOrganisaatiot = AuthoritiesUtil.getRaportointiOrganisaatiot(authorities)
    db.run(
      koulutuksetToteutuksetHakukohteetRepository.selectWithParams(
        raportointiOrganisaatiot,
        alkamiskausi,
        haku,
        koulutuksenTila,
        toteutuksenTila,
        hakukohteenTila,
        valintakoe
      )
    )
  }
}
