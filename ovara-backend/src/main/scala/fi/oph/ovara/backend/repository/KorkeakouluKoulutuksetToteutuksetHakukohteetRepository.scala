package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.KorkeakouluKoulutusToteutusHakukohdeResult
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.springframework.stereotype.Component
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
class KorkeakouluKoulutuksetToteutuksetHakukohteetRepository extends Extractors {
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
    sql"""SELECT hk.organisaatio_nimi,
                 k.koulutus_nimi,
                 k.koulutus_oid,
                 k.tila AS koulutuksen_tila,
                 k.koulutus_koodi,
                 k.ulkoinen_tunniste,
                 k.opintojenlaajuus,
                 k.laajuusyksikko_nimi,
                 t.toteutus_nimi,
                 t.toteutus_oid
          FROM pub.pub_dim_haku h
          JOIN pub.pub_dim_hakukohde hk
          ON h.haku_oid = hk.haku_oid
          JOIN pub.pub_dim_toteutus t
          ON hk.toteutus_oid = t.toteutus_oid
          JOIN pub.pub_dim_koulutus k
          ON k.koulutus_oid = t.koulutus_oid
          JOIN pub.pub_dim_organisaatio o
          ON jarjestyspaikka_oid = o.organisaatio_oid
          WHERE h.haku_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(haku)})
          AND (hk.jarjestyspaikka_oid IN (#$raportointiorganisaatiotStr))
          #${RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "k.tila", koulutuksenTila)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "t.tila", toteutuksenTila)}
          #${RepositoryUtils.makeEqualsQueryStrOfOptional("AND", "hk.tila", hakukohteenTila)}
          """.as[KorkeakouluKoulutusToteutusHakukohdeResult]
  }
}
