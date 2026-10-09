package fi.oph.ovara.backend.external.kkhakijat

import com.fasterxml.jackson.annotation.{JsonInclude, JsonProperty, JsonPropertyOrder}
import com.fasterxml.jackson.annotation.JsonInclude.Include
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode

import java.lang.{Boolean as JBoolean, Integer as JInteger}
import java.math.BigDecimal as JBigDecimal
import scala.annotation.meta.{beanGetter, field}
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*

// Vastausluokat käyttävät Optionin sijaan nullattavia Java-tyyppejä, jotta springdoc osaa
// kuvata kentät oikein Swaggerissa. Null-arvoiset kentät jätetään pois JSONista (NON_NULL),
// ja kenttien järjestys on kiinnitetty @JsonPropertyOrder-annotaatiolla.

@JsonInclude(Include.NON_NULL)
case class KKHakijatResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakijat: java.util.List[KKHakijaResponse]
)

object KKHakijatResponse {
  def apply(hakijat: Seq[KKHakija]): KKHakijatResponse =
    KKHakijatResponse(hakijat.map(KKHakijaResponse.apply).asJava)
}

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(
  Array(
    "hetu",
    "oppijanumero",
    "sukunimi",
    "etunimet",
    "kutsumanimi",
    "lahiosoite",
    "postinumero",
    "postitoimipaikka",
    "maa",
    "kansalaisuus",
    "kaksoiskansalaisuus",
    "kansalaisuudet",
    "syntymaaika",
    "matkapuhelin",
    "puhelin",
    "sahkoposti",
    "kotikunta",
    "sukupuoli",
    "aidinkieli",
    "asiointikieli",
    "koulusivistyskieli",
    "koulusivistyskielet",
    "koulutusmarkkinointilupa",
    "onYlioppilas",
    "yoSuoritusVuosi",
    "turvakielto",
    "hakemukset",
    "ensikertalainen"
  )
)
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
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty kansalaisuus: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty kaksoiskansalaisuus: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty kansalaisuudet: java.util.List[String],
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty syntymaaika: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty matkapuhelin: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty puhelin: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty sahkoposti: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kotikunta: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty sukupuoli: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty aidinkieli: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty asiointikieli: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty koulusivistyskieli: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty koulusivistyskielet: java.util.List[String],
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty koulutusmarkkinointilupa: JBoolean,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty onYlioppilas: Boolean,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty yoSuoritusVuosi: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty turvakielto: Boolean,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakemukset: java.util.List[KKHakemusResponse],
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty ensikertalainen: JBoolean
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
      kansalaisuus = h.kansalaisuus.orNull,
      kaksoiskansalaisuus = h.kaksoiskansalaisuus.orNull,
      kansalaisuudet = h.kansalaisuudet.map(_.asJava).orNull,
      syntymaaika = h.syntymaaika.orNull,
      matkapuhelin = h.matkapuhelin.orNull,
      puhelin = h.puhelin.orNull,
      sahkoposti = h.sahkoposti.orNull,
      kotikunta = h.kotikunta,
      sukupuoli = h.sukupuoli,
      aidinkieli = h.aidinkieli,
      asiointikieli = h.asiointikieli,
      koulusivistyskieli = h.koulusivistyskieli.orNull,
      koulusivistyskielet = h.koulusivistyskielet.map(_.toList.asJava).orNull,
      koulutusmarkkinointilupa = h.koulutusmarkkinointilupa.map(Boolean.box).orNull,
      onYlioppilas = h.onYlioppilas,
      yoSuoritusVuosi = h.yoSuoritusVuosi.orNull,
      turvakielto = h.turvakielto,
      hakemukset = h.hakemukset.map(KKHakemusResponse.apply).asJava,
      ensikertalainen = h.ensikertalainen.map(Boolean.box).orNull
    )
}

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(
  Array(
    "haku",
    "hakuVuosi",
    "hakuKausi",
    "hakemusnumero",
    "hakemusViimeinenMuokkausAikaleima",
    "hakemusJattoAikaleima",
    "valinnanAikaleima",
    "organisaatio",
    "hakukohde",
    "hakukohdeKkId",
    "hakutoivePrioriteetti",
    "avoinVayla",
    "valinnanTila",
    "valintatapajononTyyppi",
    "valintatapajononNimi",
    "hyvaksymisenEhto",
    "vastaanottotieto",
    "pisteet",
    "ilmoittautumiset",
    "pohjakoulutus",
    "julkaisulupa",
    "hKelpoisuus",
    "hKelpoisuusLahde",
    "hKelpoisuusMaksuvelvollisuus",
    "lukuvuosimaksu",
    "hakukohteenKoulutukset",
    "liitteet"
  )
)
case class KKHakemusResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty haku: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakuVuosi: Int,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakuKausi: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakemusnumero: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty hakemusViimeinenMuokkausAikaleima: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty hakemusJattoAikaleima: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty valinnanAikaleima: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty organisaatio: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakukohde: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty hakukohdeKkId: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty hakutoivePrioriteetti: JInteger,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty avoinVayla: JBoolean,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty valinnanTila: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty valintatapajononTyyppi: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty valintatapajononNimi: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty hyvaksymisenEhto: HyvaksymisenEhtoResponse,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty vastaanottotieto: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty pisteet: JBigDecimal,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty ilmoittautumiset: java.util.List[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty pohjakoulutus: java.util.List[String],
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty julkaisulupa: JBoolean,
  // Eksplisiittinen nimi: muuten Jackson/springdoc johtaa getteristä getHKelpoisuus nimen "hkelpoisuus".
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @(JsonProperty @field @beanGetter)("hKelpoisuus")
  @BeanProperty hKelpoisuus: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @(JsonProperty @field @beanGetter)("hKelpoisuusLahde")
  @BeanProperty hKelpoisuusLahde: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @(JsonProperty @field @beanGetter)("hKelpoisuusMaksuvelvollisuus")
  @BeanProperty hKelpoisuusMaksuvelvollisuus: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty lukuvuosimaksu: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakukohteenKoulutukset: java.util.List[KkHakukohteenkoulutusResponse],
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty liitteet: java.util.List[LiiteResponse]
)

object KKHakemusResponse {
  def apply(h: KKHakutoive): KKHakemusResponse =
    KKHakemusResponse(
      haku = h.haku,
      hakuVuosi = h.hakuVuosi,
      hakuKausi = h.hakuKausi,
      hakemusnumero = h.hakemusnumero,
      hakemusViimeinenMuokkausAikaleima = h.hakemusViimeinenMuokkausAikaleima.orNull,
      hakemusJattoAikaleima = h.hakemusJattoAikaleima.orNull,
      valinnanAikaleima = h.valinnanAikaleima.orNull,
      organisaatio = h.organisaatio,
      hakukohde = h.hakukohde,
      hakukohdeKkId = h.hakukohdeKkId.orNull,
      hakutoivePrioriteetti = h.hakutoivePrioriteetti.map(Int.box).orNull,
      avoinVayla = h.avoinVayla.map(Boolean.box).orNull,
      valinnanTila = h.valinnanTila.map(_.name).orNull,
      valintatapajononTyyppi = h.valintatapajononTyyppi.orNull,
      valintatapajononNimi = h.valintatapajononNimi.orNull,
      hyvaksymisenEhto = h.hyvaksymisenEhto.map(HyvaksymisenEhtoResponse.apply).orNull,
      vastaanottotieto = h.vastaanottotieto.map(_.name).orNull,
      pisteet = h.pisteet.map(_.bigDecimal).orNull,
      ilmoittautumiset = h.ilmoittautumiset.map(_.name).asJava,
      pohjakoulutus = h.pohjakoulutus.asJava,
      julkaisulupa = h.julkaisulupa.map(Boolean.box).orNull,
      hKelpoisuus = h.hKelpoisuus.map(_.name).getOrElse(""),
      hKelpoisuusLahde = h.hKelpoisuusLahde.orNull,
      hKelpoisuusMaksuvelvollisuus = h.hKelpoisuusMaksuvelvollisuus.map(_.name).orNull,
      lukuvuosimaksu = h.lukuvuosimaksu.orNull,
      hakukohteenKoulutukset = h.hakukohteenKoulutukset.map(KkHakukohteenkoulutusResponse.apply).asJava,
      liitteet = h.liitteet.map(_.map(LiiteResponse.apply).asJava).orNull
    )
}

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(Array("ehdollisestiHyvaksyttavissa", "ehtoKoodi", "ehtoFI", "ehtoSV", "ehtoEN"))
case class HyvaksymisenEhtoResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty ehdollisestiHyvaksyttavissa: Boolean,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty ehtoKoodi: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty ehtoFI: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty ehtoSV: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty ehtoEN: String
)

object HyvaksymisenEhtoResponse {
  def apply(h: HyvaksymisenEhto): HyvaksymisenEhtoResponse =
    HyvaksymisenEhtoResponse(
      ehdollisestiHyvaksyttavissa = h.ehdollisestiHyvaksyttavissa,
      ehtoKoodi = h.ehtoKoodi.orNull,
      ehtoFI = h.ehtoFI.orNull,
      ehtoSV = h.ehtoSV.orNull,
      ehtoEN = h.ehtoEN.orNull
    )
}

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(
  Array(
    "komoOid",
    "tkKoulutuskoodi",
    "kkKoulutusId",
    "koulutuksenAlkamiskausi",
    "koulutuksenAlkamisvuosi",
    "johtaaTutkintoon"
  )
)
case class KkHakukohteenkoulutusResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty komoOid: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty tkKoulutuskoodi: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty kkKoulutusId: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty koulutuksenAlkamiskausi: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty koulutuksenAlkamisvuosi: JInteger,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty johtaaTutkintoon: JBoolean
)

object KkHakukohteenkoulutusResponse {
  def apply(k: KkHakukohteenkoulutus): KkHakukohteenkoulutusResponse =
    KkHakukohteenkoulutusResponse(
      komoOid = k.komoOid,
      tkKoulutuskoodi = k.koulutusKoodi.orNull,
      kkKoulutusId = k.kkKoulutusId.orNull,
      koulutuksenAlkamiskausi = k.koulutuksenAlkamiskausi.orNull,
      koulutuksenAlkamisvuosi = k.koulutuksenAlkamisvuosi.map(Int.box).orNull,
      johtaaTutkintoon = k.johtaaTutkintoon.map(Boolean.box).orNull
    )
}

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder(Array("hakuId", "hakuRyhmaId", "tila", "saapumisenTila", "nimi", "vastaanottaja"))
case class LiiteResponse(
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty hakuId: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty hakuRyhmaId: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty tila: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty saapumisenTila: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty nimi: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty vastaanottaja: String
)

object LiiteResponse {
  def apply(l: Liite): LiiteResponse =
    LiiteResponse(
      hakuId = l.hakuId.orNull,
      hakuRyhmaId = l.hakuRyhmaId.orNull,
      tila = l.tila.orNull,
      saapumisenTila = l.saapumisenTila.orNull,
      nimi = l.nimi.orNull,
      vastaanottaja = l.vastaanottaja.orNull
    )
}
