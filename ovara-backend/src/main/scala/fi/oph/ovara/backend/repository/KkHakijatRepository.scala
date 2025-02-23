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
      haut: List[String],
      oppilaitokset: List[String],
      toimipisteet: List[String],
      hakukohteet: List[String],
      valintatieto: List[String],
      vastaanottotieto: List[String],
      markkinointilupa: Option[Boolean],
      naytaYoArvosanat: Boolean
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
    val optionalMarkkinointilupaQuery =
      RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "hlo.koulutusmarkkinointilupa", markkinointilupa)

    //TODO: palautetaan yo-arvosanat
    sql"""SELECT hlo.sukunimi, hlo.etunimet, hlo.hetu, hlo.syntymaaika,
                 hlo.kansalaisuus_nimi, hlo.henkilo_oid, hlo.hakemus_oid, hk.toimipiste_nimi,
                 hk.hakukohde_nimi, hk.hakukohde_oid, ht.hakutoivenumero,
                 ht.valintatapajonot->0->>'valinnan_tila' AS valinnan_tila,
                 ht.vastaanottotieto, ht.viimeinen_vastaanottopaiva, e.isensikertalainen AS ensikertalainen,
                 ht.ilmoittautumisen_tila, hlo.koulutusmarkkinointilupa,
                 hlo.valintatuloksen_julkaisulupa, hlo.sahkoinenviestintalupa,
                 hlo.lahiosoite, hlo.postinumero, hlo.postitoimipaikka
          FROM pub.pub_dim_henkilo hlo
          JOIN pub.pub_dim_hakutoive ht
          ON ht.henkilo_oid = hlo.henkilo_oid
          JOIN pub.pub_dim_hakukohde hk
          ON ht.hakukohde_oid = hk.hakukohde_oid
          LEFT JOIN pub.pub_dim_ensikertalainen e
          ON ht.henkilo_oid = e.henkilooid AND ht.haku_oid = e.hakuoid
          WHERE ht.haku_oid IN (#$hakuStr)
          AND hk.jarjestyspaikka_oid IN (#$raportointiorganisaatiotStr)
          #$optionalHakukohdeQuery
          #$optionalValintatietoQuery
          #$optionalVastaanottotietoQuery
          #$optionalMarkkinointilupaQuery
          """.as[KkHakija]
  }
}
