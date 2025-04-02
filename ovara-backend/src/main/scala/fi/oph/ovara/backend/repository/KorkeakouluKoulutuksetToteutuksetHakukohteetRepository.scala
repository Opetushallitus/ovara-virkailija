package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.KorkeakouluKoulutusToteutusHakukohdeResult
import fi.oph.ovara.backend.utils.RepositoryUtils
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
      haku: List[String],
      koulutuksenTila: Option[String],
      toteutuksenTila: Option[String],
      hakukohteenTila: Option[String]
  ): SqlStreamingAction[Vector[
    KorkeakouluKoulutusToteutusHakukohdeResult
  ], KorkeakouluKoulutusToteutusHakukohdeResult, Effect] = {
    val raportointiorganisaatiotStr = RepositoryUtils.makeListOfValuesQueryStr(selectedKayttooikeusOrganisaatiot)
    val query = sql"""SELECT hk.organisaatio_nimi,
                 k.koulutus_nimi,
                 k.koulutus_oid,
                 k.tila AS koulutuksen_tila,
                 k.koulutus_koodi,
                 k.ulkoinen_tunniste,
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
          WHERE h.haku_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(haku)})
          AND (hk.jarjestyspaikka_oid IN (#$raportointiorganisaatiotStr))
          #${RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "k.tila", koulutuksenTila)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "t.tila", toteutuksenTila)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "hk.tila", hakukohteenTila)}
          """.as[KorkeakouluKoulutusToteutusHakukohdeResult]

    LOG.debug(s"selectWithParams: ${query.statements.head}")

    query
  }
}
