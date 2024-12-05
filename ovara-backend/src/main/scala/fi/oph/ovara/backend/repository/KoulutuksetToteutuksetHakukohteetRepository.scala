package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.KoulutuksetToteutuksetHakukohteetResult
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.springframework.stereotype.Component
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
class KoulutuksetToteutuksetHakukohteetRepository extends Extractors {
  def selectWithParams(
      raportointiorganisaatiot: List[String],
      alkamiskausi: List[String],
      haku: List[String],
      koulutuksenTila: Option[String],
      toteutuksenTila: Option[String],
      hakukohteenTila: Option[String],
      valintakoe: Option[Boolean]
  ): SqlStreamingAction[Vector[KoulutuksetToteutuksetHakukohteetResult], KoulutuksetToteutuksetHakukohteetResult, Effect] = {
    val alkamiskaudetAndHenkKohtSuunnitelma = RepositoryUtils.extractAlkamisvuosiKausiAndHenkkohtSuunnitelma(alkamiskausi)
    val alkamiskaudet = alkamiskaudetAndHenkKohtSuunnitelma._1
    val henkilokohtainenSuunnitelma = alkamiskaudetAndHenkKohtSuunnitelma._2
    val raportointiorganisaatiotStr = RepositoryUtils.makeListOfValuesQueryStr(raportointiorganisaatiot)

    sql"""select hk.hakukohde_nimi,
                 hk.hakukohde_oid,
                 k.tila as koulutuksen_tila,
                 t.tila as toteutuksen_tila,
                 hk.tila as hakukohteen_tila,
                 hk.aloituspaikat,
                 hk.on_valintakoe,
                 hk.toinen_aste_onko_kaksoistutkinto,
                 hk.jarjestaa_urheilijan_ammkoulutusta
          from pub.pub_dim_haku h
          join pub.pub_dim_hakukohde hk
          on h.haku_oid = hk.haku_oid
          join pub.pub_dim_toteutus t
          on hk.toteutus_oid = t.toteutus_oid
          join pub.pub_dim_koulutus k
          on k.koulutus_oid = t.koulutus_oid
          WHERE h.haku_oid in (#${RepositoryUtils.makeListOfValuesQueryStr(haku)})
          and (t.organisaatio_oid in (#$raportointiorganisaatiotStr)
               or hk.jarjestyspaikka_oid in (#$raportointiorganisaatiotStr))
          #${RepositoryUtils.makeAlkamiskaudetAndHenkkohtSuunnitelmaQuery(alkamiskaudetAndHenkKohtSuunnitelma)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "k.tila", koulutuksenTila)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "t.tila", toteutuksenTila)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "hk.tila", hakukohteenTila)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "hk.on_valintakoe", valintakoe)}
          """.as[KoulutuksetToteutuksetHakukohteetResult]
  }
}
