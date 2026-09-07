package fi.oph.ovara.backend.external.kkhakijat

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode

import scala.annotation.meta.field
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

@JsonInclude(Include.ALWAYS)
case class KKHakijatResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakijat: java.util.List[KKHakijaResponse]
)

object KKHakijatResponse {
  def apply(hakijat: Seq[KKHakija]): KKHakijatResponse =
    KKHakijatResponse(hakijat.map(KKHakijaResponse.apply).asJava)
}

@JsonInclude(Include.ALWAYS)
case class KKHakijaResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hetu: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty oppijanumero: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty sukunimi: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty etunimet: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kutsumanimi: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty lahiosoite: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty postinumero: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty postitoimipaikka: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty maa: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kansalaisuus: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kaksoiskansalaisuus: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kansalaisuudet: Option[java.util.List[String]],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty syntymaaika: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty matkapuhelin: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty puhelin: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty sahkoposti: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kotikunta: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty sukupuoli: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty aidinkieli: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty asiointikieli: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty koulusivistyskieli: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty koulusivistyskielet: Option[java.util.List[String]],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty koulutusmarkkinointilupa: Option[Boolean],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty onYlioppilas: Boolean,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty yoSuoritusVuosi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty turvakielto: Boolean,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakemukset: java.util.List[KKHakemusResponse],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty ensikertalainen: Option[Boolean]
)

object KKHakijaResponse {
  def apply(h: KKHakija): KKHakijaResponse =
    KKHakijaResponse(
      hetu = h.hetu,
      oppijanumero = h.oppijanumero,
      sukunimi = h.sukunimi,
      etunimet = h.etunimet,
      kutsumanimi = h.kutsumanimi,
      lahiosoite = h.lahiosoite,
      postinumero = h.postinumero,
      postitoimipaikka = h.postitoimipaikka,
      maa = h.maa,
      kansalaisuus = h.kansalaisuus,
      kaksoiskansalaisuus = h.kaksoiskansalaisuus,
      kansalaisuudet = h.kansalaisuudet.map(_.asJava),
      syntymaaika = h.syntymaaika,
      matkapuhelin = h.matkapuhelin,
      puhelin = h.puhelin,
      sahkoposti = h.sahkoposti,
      kotikunta = h.kotikunta,
      sukupuoli = h.sukupuoli,
      aidinkieli = h.aidinkieli,
      asiointikieli = h.asiointikieli,
      koulusivistyskieli = h.koulusivistyskieli,
      koulusivistyskielet = h.koulusivistyskielet.map(_.toList.asJava),
      koulutusmarkkinointilupa = h.koulutusmarkkinointilupa,
      onYlioppilas = h.onYlioppilas,
      yoSuoritusVuosi = h.yoSuoritusVuosi,
      turvakielto = h.turvakielto,
      hakemukset = h.hakemukset.map(KKHakemusResponse.apply).asJava,
      ensikertalainen = h.ensikertalainen
    )
}

@JsonInclude(Include.ALWAYS)
case class KKHakemusResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty haku: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakuVuosi: Int,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakuKausi: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakemusnumero: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakemusViimeinenMuokkausAikaleima: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakemusJattoAikaleima: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty valinnanAikaleima: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty organisaatio: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakukohde: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakutoivePrioriteetti: Option[Int],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakukohdeKkId: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty avoinVayla: Option[Boolean],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty valinnanTila: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty valintatapajononTyyppi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty valintatapajononNimi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hyvaksymisenEhto: Option[HyvaksymisenEhtoResponse],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty vastaanottotieto: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty pisteet: Option[BigDecimal],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty ilmoittautumiset: java.util.List[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty pohjakoulutus: java.util.List[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty julkaisulupa: Option[Boolean],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hKelpoisuus: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hKelpoisuusLahde: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hKelpoisuusMaksuvelvollisuus: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty lukuvuosimaksu: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakukohteenKoulutukset: java.util.List[KkHakukohteenkoulutusResponse],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty liitteet: Option[java.util.List[LiiteResponse]]
)

object KKHakemusResponse {
  def apply(h: KKHakutoive): KKHakemusResponse =
    KKHakemusResponse(
      haku = h.haku,
      hakuVuosi = h.hakuVuosi,
      hakuKausi = h.hakuKausi,
      hakemusnumero = h.hakemusnumero,
      hakemusViimeinenMuokkausAikaleima = h.hakemusViimeinenMuokkausAikaleima,
      hakemusJattoAikaleima = h.hakemusJattoAikaleima,
      valinnanAikaleima = h.valinnanAikaleima,
      organisaatio = h.organisaatio,
      hakukohde = h.hakukohde,
      hakutoivePrioriteetti = h.hakutoivePrioriteetti,
      hakukohdeKkId = h.hakukohdeKkId,
      avoinVayla = h.avoinVayla,
      valinnanTila = h.valinnanTila.map(_.name),
      valintatapajononTyyppi = h.valintatapajononTyyppi,
      valintatapajononNimi = h.valintatapajononNimi,
      hyvaksymisenEhto = h.hyvaksymisenEhto.map(HyvaksymisenEhtoResponse.apply),
      vastaanottotieto = h.vastaanottotieto.map(_.name),
      pisteet = h.pisteet,
      ilmoittautumiset = h.ilmoittautumiset.map(_.name).asJava,
      pohjakoulutus = h.pohjakoulutus.asJava,
      julkaisulupa = h.julkaisulupa,
      hKelpoisuus = h.hKelpoisuus,
      hKelpoisuusLahde = h.hKelpoisuusLahde,
      hKelpoisuusMaksuvelvollisuus = h.hKelpoisuusMaksuvelvollisuus,
      lukuvuosimaksu = h.lukuvuosimaksu,
      hakukohteenKoulutukset = h.hakukohteenKoulutukset.map(KkHakukohteenkoulutusResponse.apply).asJava,
      liitteet = h.liitteet.map(_.map(LiiteResponse.apply).asJava)
    )
}

@JsonInclude(Include.ALWAYS)
case class HyvaksymisenEhtoResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty ehdollisestiHyvaksyttavissa: Boolean,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty ehtoKoodi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty ehtoFI: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty ehtoSV: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty ehtoEN: Option[String]
)

object HyvaksymisenEhtoResponse {
  def apply(h: HyvaksymisenEhto): HyvaksymisenEhtoResponse =
    HyvaksymisenEhtoResponse(
      ehdollisestiHyvaksyttavissa = h.ehdollisestiHyvaksyttavissa,
      ehtoKoodi = h.ehtoKoodi,
      ehtoFI = h.ehtoFI,
      ehtoSV = h.ehtoSV,
      ehtoEN = h.ehtoEN
    )
}

@JsonInclude(Include.ALWAYS)
case class KkHakukohteenkoulutusResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty komoOid: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty koulutusKoodi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kkKoulutusId: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty koulutuksenAlkamisvuosi: Option[Int],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty koulutuksenAlkamiskausi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty johtaaTutkintoon: Option[Boolean]
)

object KkHakukohteenkoulutusResponse {
  def apply(k: KkHakukohteenkoulutus): KkHakukohteenkoulutusResponse =
    KkHakukohteenkoulutusResponse(
      komoOid = k.komoOid,
      koulutusKoodi = k.koulutusKoodi,
      kkKoulutusId = k.kkKoulutusId,
      koulutuksenAlkamisvuosi = k.koulutuksenAlkamisvuosi,
      koulutuksenAlkamiskausi = k.koulutuksenAlkamiskausi,
      johtaaTutkintoon = k.johtaaTutkintoon
    )
}

@JsonInclude(Include.ALWAYS)
case class LiiteResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakuId: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakuRyhmaId: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty tila: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty saapumisenTila: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty nimi: Option[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty vastaanottaja: Option[String]
)

object LiiteResponse {
  def apply(l: Liite): LiiteResponse =
    LiiteResponse(
      hakuId = l.hakuId,
      hakuRyhmaId = l.hakuRyhmaId,
      tila = l.tila,
      saapumisenTila = l.saapumisenTila,
      nimi = l.nimi,
      vastaanottaja = l.vastaanottaja
    )
}
