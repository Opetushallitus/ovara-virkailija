package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.KkHakija
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.springframework.stereotype.Component
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
class KkHakijatRepository extends Extractors {
  def selectWithParams(
      kayttooikeusOrganisaatiot: List[String],
      hakukohderyhmat: List[String],
      haut: List[String],
      oppilaitokset: List[String],
      toimipisteet: List[String],
      hakukohteet: List[String],
      valintatieto: List[String],
      vastaanottotieto: List[String],
      kansalaisuus: List[String],
      markkinointilupa: Option[Boolean]
  ): SqlStreamingAction[Vector[KkHakija], KkHakija, Effect] = {
    val hakuStr                     = RepositoryUtils.makeListOfValuesQueryStr(haut)
    val raportointiorganisaatiotStr = RepositoryUtils.makeListOfValuesQueryStr(kayttooikeusOrganisaatiot)

    val vastaanottotiedotAsDbValues = RepositoryUtils.mapVastaanottotiedotToDbValues(vastaanottotieto)
    val valintatiedotAsDbValues     = RepositoryUtils.mapValintatiedotToDbValues(valintatieto)
    val optionalHakukohdeQuery =
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "hk.hakukohde_oid", hakukohteet)
    val optionalValintatietoQuery =
      RepositoryUtils.makeOptionalListOfValuesQueryStr(
        "AND",
        "ht.valintatapajonot->0->>'valinnan_tila'",
        valintatiedotAsDbValues
      )
    val optionalVastaanottotietoQuery =
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "ht.vastaanottotieto", vastaanottotiedotAsDbValues)

    val optionalKansalaisuusQuery =
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "hlo.kansalaisuusluokka", kansalaisuus)
    val optionalMarkkinointilupaQuery =
      RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "hlo.koulutusmarkkinointilupa", markkinointilupa)

    val optionalJarjestyspaikkaQuery =
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "hk.jarjestyspaikka_oid", kayttooikeusOrganisaatiot)
    val optionalHakukohderyhmaQuery =
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "hkr_hk.hakukohderyhma_oid", hakukohderyhmat)

    //TODO: palautetaan yo-arvosanat
    sql"""SELECT DISTINCT hlo.sukunimi, hlo.etunimet, hlo.hetu, hlo.syntymaaika,
                 hlo.kansalaisuus_nimi, hlo.henkilo_oid, hlo.hakemus_oid, hk.toimipiste_nimi,
                 hk.hakukohde_nimi, kkh.hakukelpoisuus, ht.hakutoivenumero, ht.valintatieto,
                 ht.ehdollisesti_hyvaksytty, ht.valintatiedon_pvm, ht.vastaanottotieto,
                 ht.viimeinen_vastaanottopaiva, e.isensikertalainen AS ensikertalainen,
                 ht.ilmoittautumisen_tila, hlo.valintatuloksen_julkaisulupa,
                 hlo.koulutusmarkkinointilupa, hlo.sahkoinenviestintalupa,
                 hlo.lahiosoite, hlo.postinumero, hlo.postitoimipaikka
          FROM pub.pub_dim_henkilo hlo
          JOIN pub.pub_dim_hakutoive ht
          ON ht.henkilo_oid = hlo.henkilo_oid
          JOIN pub.pub_dim_hakukohde hk
          ON ht.hakukohde_oid = hk.hakukohde_oid
          LEFT JOIN pub.pub_dim_ensikertalainen e
          ON ht.henkilo_oid = e.henkilooid AND ht.haku_oid = e.hakuoid
          JOIN pub.pub_fct_raportti_hakijat_kk kkh
          ON ht.hakutoive_id = kkh.hakutoive_id
          JOIN pub.pub_dim_hakukohderyhma_ja_hakukohteet hkr_hk
          ON hk.hakukohde_oid = hkr_hk.hakukohde_oid
          WHERE ht.haku_oid IN (#$hakuStr)
          #$optionalJarjestyspaikkaQuery
          #$optionalHakukohderyhmaQuery
          #$optionalHakukohdeQuery
          #$optionalValintatietoQuery
          #$optionalVastaanottotietoQuery
          #$optionalKansalaisuusQuery
          #$optionalMarkkinointilupaQuery
          """.as[KkHakija]
  }
}
