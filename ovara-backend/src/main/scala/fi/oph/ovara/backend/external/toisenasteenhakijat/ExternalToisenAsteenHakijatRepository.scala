package fi.oph.ovara.backend.external.toisenasteenhakijat

import fi.oph.ovara.backend.repository.ReadOnlyDatabase
import fi.oph.ovara.backend.utils.RepositoryUtils
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.stereotype.Repository
import slick.jdbc.PostgresProfile.api.actionBasedSQLInterpolation

@Repository
class ExternalToisenAsteenHakijatRepository(db: ReadOnlyDatabase) extends HakijatExtractors {
  val LOG: Logger = LoggerFactory.getLogger(classOf[ExternalToisenAsteenHakijatRepository])

  private val ataruOidLength = 35

  def selectHakijat(
    hakuOid: String,
    hakukohdeOid: Option[String],
    organisaatioOid: Option[String]
  ): Seq[HakijaRow] = {
    val query = (hakukohdeOid, organisaatioOid) match {
      case (Some(hk), _) =>
        sql"""
        SELECT hlo.oppijanumero,
          hakemus.hakemus_oid,
          hakemus.sahkoposti,
          hakemus.puhelin,
          hakemus.lahiosoite,
          hakemus.postinumero,
          hakemus.postitoimipaikka,
          haku.haku_oid
        FROM gen.gen_henkilo hlo
        INNER JOIN gen.gen_hakemus hakemus ON hakemus.henkilo_oid = hlo.henkilo_oid
        INNER JOIN gen.gen_haku    haku    ON hakemus.haku_oid    = haku.haku_oid
        WHERE haku.haku_oid = $hakuOid
        AND length(hakemus.hakemus_oid) = #$ataruOidLength
        AND haku.kohdejoukko_koodiuri LIKE 'haunkohdejoukko_11%'
        AND EXISTS (
          SELECT 1 FROM gen.gen_hakutoive ht
          WHERE ht.hakemus_oid = hakemus.hakemus_oid
          AND ht.hakukohde_oid = $hk
        )
        """.as[HakijaRow]
      case (_, Some(org)) =>
        sql"""
        SELECT hlo.oppijanumero,
          hakemus.hakemus_oid,
          hakemus.sahkoposti,
          hakemus.puhelin,
          hakemus.lahiosoite,
          hakemus.postinumero,
          hakemus.postitoimipaikka,
          haku.haku_oid
        FROM gen.gen_henkilo hlo
        INNER JOIN gen.gen_hakemus hakemus ON hakemus.henkilo_oid = hlo.henkilo_oid
        INNER JOIN gen.gen_haku    haku    ON hakemus.haku_oid    = haku.haku_oid
        WHERE haku.haku_oid = $hakuOid
        AND length(hakemus.hakemus_oid) = #$ataruOidLength
        AND haku.kohdejoukko_koodiuri LIKE 'haunkohdejoukko_11%'
        AND EXISTS (
          SELECT 1 FROM gen.gen_hakutoive ht
          INNER JOIN gen.gen_hakukohde hk ON ht.hakukohde_oid = hk.hakukohde_oid
          WHERE ht.hakemus_oid = hakemus.hakemus_oid
          AND hk.jarjestyspaikka_oid = $org
        )
        """.as[HakijaRow]
      case _ =>
        // Controller-level validation guarantees this branch is unreachable.
        return Seq.empty
    }

    LOG.debug(s"selectHakijatQuery: ${query.statements.head}")
    db.run(query, "selectHakijat")
  }

  def selectHakutoiveet(hakemusOids: Iterable[String]): Seq[HakijaHakutoiveRow] = {
    val query = sql"""
    SELECT
      ht.hakemus_oid,
      ht.hakukohde_oid,
      ht.hakutoivenumero,
      hk.jarjestyspaikka_oid,
      k.koulutukset_koodiuri
    FROM gen.gen_hakutoive ht
    LEFT JOIN gen.gen_hakukohde hk ON ht.hakukohde_oid = hk.hakukohde_oid
    LEFT JOIN gen.gen_toteutus  t  ON hk.toteutus_oid  = t.toteutus_oid
    LEFT JOIN gen.gen_koulutus  k  ON t.koulutus_oid   = k.koulutus_oid
    WHERE ht.hakemus_oid IN (#${RepositoryUtils.makeListOfValuesQueryStr(hakemusOids)})
    """.as[HakijaHakutoiveRow]

    LOG.debug(s"selectHakutoiveetQuery: ${query.statements.head}")
    db.run(query, "selectHakijatHakutoiveet")
  }

  def selectKoodistot(koodiUrit: Set[String]): Seq[KoodistoArvo] = {
    val query = sql"""
    SELECT versioitu_koodiuri,
      koodiarvo,
      koodistouri,
      koodiversio,
      nimi_fi,
      nimi_sv,
      nimi_en
    FROM gen.gen_koodi
    WHERE versioitu_koodiuri in (#${RepositoryUtils.makeListOfValuesQueryStr(koodiUrit)})
    """.as[KoodistoArvo]

    LOG.debug(s"selectKoodistotQuery: ${query.statements.head}")
    db.run(query, "selectHakijatKoodistot")
  }
}
