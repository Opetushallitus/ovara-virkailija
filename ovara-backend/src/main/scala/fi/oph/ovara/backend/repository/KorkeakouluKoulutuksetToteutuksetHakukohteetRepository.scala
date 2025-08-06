package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.KorkeakouluKoulutusToteutusHakukohdeResult
import fi.oph.ovara.backend.utils.RepositoryUtils
import fi.oph.ovara.backend.utils.RepositoryUtils.{buildTutkinnonTasoFilters, makeEqualsQueryStrOfOptional, makeHakukohderyhmaQueryWithKayttooikeudet, makeOptionalHakukohderyhmatSubSelectQueryStr, makeOptionalJarjestyspaikkaQuery}
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.{Component, Repository}
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
@Repository
class KorkeakouluKoulutuksetToteutuksetHakukohteetRepository extends Extractors {
  val LOG: Logger = LoggerFactory.getLogger(classOf[KorkeakouluKoulutuksetToteutuksetHakukohteetRepository])

  def selectWithParams(
      selectedKayttooikeusOrganisaatiot: List[String],
      isOrganisaatioRajain: Boolean,
      kayttooikeusHakukohderyhmat: List[String],
      hakukohderyhmat: List[String],
      haut: List[String],
      koulutuksenTila: Option[String],
      toteutuksenTila: Option[String],
      hakukohteenTila: Option[String],
      tutkinnonTasot: List[String]
  ): SqlStreamingAction[Vector[
    KorkeakouluKoulutusToteutusHakukohdeResult
  ], KorkeakouluKoulutusToteutusHakukohdeResult, Effect] = {

    val organisaatioKayttooikeusQueryStr =
      if (isOrganisaatioRajain) {
        // jos organisaatio on valittu, ei huomioida käyttäjän organisaatioiden ulkopuolisia hakukohderyhmiä
        makeOrganisaatioHakukohderyhmaSubselect(selectedKayttooikeusOrganisaatiot, List.empty)
      } else {
        makeOrganisaatioHakukohderyhmaSubselect(selectedKayttooikeusOrganisaatiot, kayttooikeusHakukohderyhmat)
      }

    val optionalHakukohderyhmaSubSelect = makeOptionalHakukohderyhmatSubSelectQueryStr(hakukohderyhmat)
    val tutkinnonTasoQueryStr = buildTutkinnonTasoFilters(tutkinnonTasot, "hk")

    val query = sql"""SELECT hk.organisaatio_nimi,
                 k.koulutus_nimi,
                 k.koulutus_oid,
                 k.tila AS koulutuksen_tila,
                 k.koulutus_koodi,
                 k.ulkoinen_tunniste,
                 k.kk_tutkinnon_taso,
                 k.opintojenlaajuus,
                 k.laajuusyksikko_nimi,
                 t.toteutus_nimi,
                 t.toteutus_oid,
                 t.tila,
                 t.ulkoinen_tunniste,
                 hk.koulutuksen_alkamiskausi_koodiuri,
                 hk.koulutuksen_alkamisvuosi,
                 hk.koulutuksen_alkamiskausi,
                 kausi.koodinimi,
                 hk.hakukohde_nimi,
                 hk.hakukohde_oid,
                 hk.tila,
                 hk.ulkoinen_tunniste,
                 haku_ja_hakuaika.haku_nimi,
                 haku_ja_hakuaika.haku_oid,
                 haku_ja_hakuaika.hakuaika,
                 haku_ja_hakuaika.hakutapa_nimi,
                 hk.hakukohteen_aloituspaikat,
                 hk.aloituspaikat_ensikertalaisille,
                 hk.valintaperuste_nimi
          FROM pub.pub_dim_haku h
          JOIN pub.pub_dim_hakukohde hk
          ON h.haku_oid = hk.haku_oid
          JOIN pub.pub_dim_toteutus t
          ON hk.toteutus_oid = t.toteutus_oid
          JOIN pub.pub_dim_koulutus k
          ON k.koulutus_oid = t.koulutus_oid
          JOIN pub.pub_dim_organisaatio o
          ON jarjestyspaikka_oid = o.organisaatio_oid
          LEFT JOIN pub.pub_dim_koodisto_kausi kausi
          ON hk.koulutuksen_alkamiskausi_koodiuri = kausi.versioitu_koodiuri
          JOIN (
            SELECT *, jsonb_array_elements(pdh.hakuajat) AS hakuaika
            FROM pub.pub_dim_haku pdh
            ) AS haku_ja_hakuaika
          ON haku_ja_hakuaika.haku_oid = hk.haku_oid
          WHERE h.haku_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(haut)})
          #$organisaatioKayttooikeusQueryStr
          #$optionalHakukohderyhmaSubSelect
          #${makeEqualsQueryStrOfOptional("AND", "k.tila", koulutuksenTila)}
          #${makeEqualsQueryStrOfOptional("AND", "t.tila", toteutuksenTila)}
          #${makeEqualsQueryStrOfOptional("AND", "hk.tila", hakukohteenTila)}
          #$tutkinnonTasoQueryStr
          """.as[KorkeakouluKoulutusToteutusHakukohdeResult]

    LOG.debug(s"selectWithParams: ${query.statements.head}")

    query
  }

  private def makeOrganisaatioHakukohderyhmaSubselect(
                                                       kayttooikeusOrgOids: List[String],
                                                       kayttooikeusHakukohderyhmaOids: List[String],
                                                       hakukohdeTablename: String = "hk",
                                                       operator: String = "AND"
                                                     ): String = {
    val kayttooikeusHakukohderyhmaEhto = makeOptionalHakukohderyhmatSubSelectQueryStr(
      hakukohderyhmat = kayttooikeusHakukohderyhmaOids,
      hakukohdeTablename = hakukohdeTablename,
      operator = "" // lisätään operaattori vasta lopuksi
    )

    val organisaatioEhto = makeOptionalJarjestyspaikkaQuery(
      selectedKayttooikeusOrganisaatiot = kayttooikeusOrgOids,
      tablename = hakukohdeTablename,
      operator = "" // lisätään operaattori vasta lopuksi
    )

    (kayttooikeusHakukohderyhmaEhto.trim, organisaatioEhto.trim) match {
      case ("", "") => ""
      case (hakukohderyhmaEhto, "") => s"$operator $hakukohderyhmaEhto"
      case ("", organisaatioEhto) => s"$operator $organisaatioEhto"
      case (hakukohderyhmaEhto, organisaatioEhto) => s"$operator ($hakukohderyhmaEhto OR $organisaatioEhto)"
    }
  }
}
