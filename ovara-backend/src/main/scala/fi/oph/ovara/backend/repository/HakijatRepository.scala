package fi.oph.ovara.backend.repository

import fi.oph.ovara.backend.domain.ToisenAsteenHakija
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.springframework.stereotype.Component
import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api.*
import slick.sql.SqlStreamingAction

@Component
class HakijatRepository extends Extractors {
  def selectWithParams(
      kayttooikeusOrganisaatiot: List[String],
      haut: List[String],
      oppilaitokset: List[String],
      toimipisteet: List[String],
      hakukohteet: List[String],
      pohjakoulutukset: List[String],
      valintatieto: List[String],
      vastaanottotieto: List[String],
      harkinnanvaraisuudet: List[String],
      kaksoistutkintoKiinnostaa: Option[Boolean],
      urheilijatutkintoKiinnostaa: Option[Boolean],
      soraTerveys: Option[Boolean],
      soraAiempi: Option[Boolean],
      markkinointilupa: Option[Boolean],
      julkaisulupa: Option[Boolean]
  ): SqlStreamingAction[Vector[ToisenAsteenHakija], ToisenAsteenHakija, Effect] = {
    val hakuStr                     = RepositoryUtils.makeListOfValuesQueryStr(haut)
    val raportointiorganisaatiotStr = RepositoryUtils.makeListOfValuesQueryStr(kayttooikeusOrganisaatiot)

    val vastaanottotiedotAsDbValues        = RepositoryUtils.mapVastaanottotiedotToDbValues(vastaanottotieto)
    val valintatiedotAsDbValues            = RepositoryUtils.mapValintatiedotToDbValues(valintatieto)
    val harkinnanvaraisuudetWithSureValues = RepositoryUtils.enrichHarkinnanvaraisuudet(harkinnanvaraisuudet)
    val optionalHakukohdeQuery =
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "hk.hakukohde_oid", hakukohteet)
    val optionalPohjakoulutusQuery =
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "ht2.pohjakoulutus", pohjakoulutukset)
    val optionalValintatietoQuery =
      RepositoryUtils.makeOptionalListOfValuesQueryStr(
        "AND",
        "ht.valintatapajonot->0->>'valinnan_tila'",
        valintatiedotAsDbValues
      )
    val optionalVastaanottotietoQuery =
      RepositoryUtils.makeOptionalListOfValuesQueryStr("AND", "ht.vastaanottotieto", vastaanottotiedotAsDbValues)
    val optionalKaksoistutkintoQuery = RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean(
      "AND",
      "ht2.kaksoistutkinto_kiinnostaa",
      kaksoistutkintoKiinnostaa
    )
    val optionalUrheilijatutkintoQuery = RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean(
      "AND",
      "ht2.urheilijatutkinto_kiinnostaa",
      urheilijatutkintoKiinnostaa
    )
    val optionalSoraTerveysKyselyQuery =
      RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "ht2.sora_terveys", soraTerveys)
    val optionalSoraAiempiQuery =
      RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "ht2.sora_aiempi", soraAiempi)
    val optionalMarkkinointilupaQuery =
      RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "hlo.koulutusmarkkinointilupa", markkinointilupa)
    val optionalJulkaisulupaQuery =
      RepositoryUtils.makeEqualsQueryStrOfOptionalBoolean("AND", "hlo.valintatuloksen_julkaisulupa", julkaisulupa)
    val optionalHarkinnanvaraisuusQuery = RepositoryUtils.makeOptionalListOfValuesQueryStr(
      "AND",
      "ht2.harkinnanvaraisuuden_syy",
      harkinnanvaraisuudetWithSureValues
    )

    // Toisella asteella kirjoitushetkellä käytössä vain yksi valintatapajono, minkä takia varasija, kokonaispisteet ja hylk_tai_per_syy
    // haetaan jonon ensimmäisestä ja siis ainoasta valintatapajonosta.
    sql"""SELECT hlo.sukunimi, hlo.etunimet, hlo.turvakielto,
                 hlo.kansalaisuus_nimi, hlo.henkilo_oid, hlo.hakemus_oid, hk.oppilaitos_nimi, hk.toimipiste_nimi,
                 hk.hakukohde_nimi, ht.hakutoivenumero, ht2.kaksoistutkinto_kiinnostaa, ht2.urheilijatutkinto_kiinnostaa,
                 ht.valintatieto, ht.valintatapajonot->0->>'varasijan_numero' as varasija,
                 ht.valintatapajonot->0->>'pisteet' as kokonaispisteet, ht.valintatapajonot->0->>'valinnantilan_kuvauksen_teksti' as hylk_tai_per_syy,
                 ht.vastaanottotieto, ht.viimeinen_vastaanottopaiva, ht.ilmoittautumisen_tila, ht2.harkinnanvaraisuuden_syy,
                 ht2.sora_aiempi, ht2.sora_terveys, ht2.pohjakoulutus_nimi, hlo.koulutusmarkkinointilupa,
                 hlo.valintatuloksen_julkaisulupa, hlo.sahkoinenviestintalupa, hlo.lahiosoite, hlo.postinumero, hlo.postitoimipaikka
          FROM pub.pub_dim_henkilo hlo
          JOIN pub.pub_dim_hakutoive ht
          ON ht.henkilo_oid = hlo.henkilo_oid
          JOIN pub.pub_fct_raportti_hakijat_toinen_aste ht2
          ON ht.hakutoive_id = ht2.hakutoive_id
          JOIN pub.pub_dim_hakukohde hk
          ON ht.hakukohde_oid = hk.hakukohde_oid
          WHERE ht.haku_oid IN (#$hakuStr)
          AND hk.jarjestyspaikka_oid IN (#$raportointiorganisaatiotStr)
          #$optionalHakukohdeQuery
          #$optionalPohjakoulutusQuery
          #$optionalValintatietoQuery
          #$optionalVastaanottotietoQuery
          #$optionalKaksoistutkintoQuery
          #$optionalUrheilijatutkintoQuery
          #$optionalSoraTerveysKyselyQuery
          #$optionalSoraAiempiQuery
          #$optionalMarkkinointilupaQuery
          #$optionalJulkaisulupaQuery
          #$optionalHarkinnanvaraisuusQuery
          """.as[ToisenAsteenHakija]
  }
}
