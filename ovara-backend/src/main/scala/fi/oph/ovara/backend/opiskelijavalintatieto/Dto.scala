package fi.oph.ovara.backend.opiskelijavalintatieto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import fi.oph.ovara.backend.domain.{En, Fi, Kielistetty, Sv}
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode

import java.util.Optional
import scala.annotation.meta.field
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.RichOption

case class OpiskelijavalintatietoResponse(
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty oppijanumero: String,
    @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
    @BeanProperty hetu: String,
    @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
    @BeanProperty syntymaaika: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty sukunimi: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty etunimet: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty hakemukset: java.util.List[HakemusResponse]
)

object OpiskelijavalintatietoResponse {
  def apply(opiskelijavalintatieto: Opiskelijavalintatieto): OpiskelijavalintatietoResponse =
    OpiskelijavalintatietoResponse(
      oppijanumero = opiskelijavalintatieto.oppijanumero,
      hetu = opiskelijavalintatieto.hetu,
      syntymaaika = opiskelijavalintatieto.syntymaaika,
      sukunimi = opiskelijavalintatieto.sukunimi,
      etunimet = opiskelijavalintatieto.etunimet,
      hakemukset = opiskelijavalintatieto.hakemukset.map(HakemusResponse.apply).asJava
    )
}

case class HakemusResponse(
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty hakemusOid: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty haku: NimettyResponse,
    @(Schema @field)(
      requiredMode = RequiredMode.REQUIRED,
      description =
        "Viittaa koodistoon [haun kohdejoukko](https://virkailija.opintopolku.fi/koodisto-service/ui/koodisto/view/haunkohdejoukko)"
    )
    @BeanProperty haunKohdejoukko: Optional[String],
    @(Schema @field)(
      requiredMode = RequiredMode.REQUIRED,
      description =
        "Viittaa koodistoon [hakutapa](https://virkailija.opintopolku.fi/koodisto-service/ui/koodisto/view/hakutapa)"
    )
    @BeanProperty hakutapa: Optional[String],
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty hakutoiveet: java.util.List[HakutoiveResponse]
)

object HakemusResponse {
  def apply(hakemus: Hakemus): HakemusResponse =
    HakemusResponse(
      hakemusOid = hakemus.hakemusOid,
      haku = NimettyResponse(hakemus.haku),
      haunKohdejoukko = hakemus.haunKohdejoukko.toJava,
      hakutapa = hakemus.hakutapa.toJava,
      hakutoiveet = hakemus.hakutoiveet.map(HakutoiveResponse.apply).asJava
    )
}

case class HakutoiveResponse(
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty hakukohde: NimettyResponse,
    @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
    @BeanProperty tarjoaja: Optional[NimettyResponse],
    @(Schema @field)(
      requiredMode = RequiredMode.NOT_REQUIRED,
      description =
        "Viittaa koodistoon [kausi](https://virkailija.opintopolku.fi/koodisto-service/ui/koodisto/view/kausi)"
    )
    @BeanProperty koulutuksenAlkamiskausiUri: Optional[String],
    @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
    @BeanProperty koulutuksenAlkamisvuosi: Optional[Integer],
    @(Schema @field)(
      requiredMode = RequiredMode.NOT_REQUIRED,
      allowableValues = Array(
        "HYVAKSYTTY",
        "VARASIJALTA_HYVAKSYTTY",
        "HARKINNANVARAISESTI_HYVAKSYTTY",
        "VARALLA",
        "HYLATTY",
        "PERUUNTUNUT",
        "PERUNUT",
        "PERUUTETTU",
        "KESKEN"
      )
    )
    @BeanProperty valinnanTila: Optional[String],
    @(Schema @field)(
      requiredMode = RequiredMode.NOT_REQUIRED,
      allowableValues = Array(
        "EHDOLLISESTI_VASTAANOTTANUT",
        "VASTAANOTTANUT_SITOVASTI",
        "EI_VASTAANOTETTU_MAARA_AIKANA",
        "PERUNUT",
        "PERUUTETTU",
        "OTTANUT_VASTAAN_TOISEN_PAIKAN",
        "KESKEN"
      )
    )
    @BeanProperty vastaanotonTila: Optional[String],
    @(Schema @field)(
      requiredMode = RequiredMode.NOT_REQUIRED,
      allowableValues = Array(
        "EI_TEHTY",
        "LASNA_KOKO_LUKUVUOSI",
        "POISSA_KOKO_LUKUVUOSI",
        "EI_ILMOITTAUTUNUT",
        "LASNA_SYKSY",
        "POISSA_SYKSY",
        "LASNA",
        "POISSA"
      )
    )
    @BeanProperty ilmoittautumisenTila: Optional[String]
)
object HakutoiveResponse {
  def apply(hakutoive: Hakutoive): HakutoiveResponse =
    this(
      hakukohde = NimettyResponse(hakutoive.hakukohde),
      tarjoaja = hakutoive.tarjoaja.map(NimettyResponse(_)).toJava,
      koulutuksenAlkamiskausiUri = hakutoive.koulutuksenAlkamiskausiUri.toJava,
      koulutuksenAlkamisvuosi = hakutoive.koulutuksenAlkamisvuosi.map(Integer.valueOf).toJava,
      valinnanTila = hakutoive.valinnanTila.toJava,
      vastaanotonTila = hakutoive.vastaanotonTila.toJava,
      ilmoittautumisenTila = hakutoive.ilmoittautumisenTila.toJava
    )
}

case class NimettyResponse(
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty oid: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty nimi: KielistettyResponse
)

object NimettyResponse {
  def apply(nimetty: Nimetty): NimettyResponse = NimettyResponse(nimetty.oid, KielistettyResponse(nimetty.nimi))
}

case class ValidationError(
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty status: Int,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty message: String,
    @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
    @BeanProperty details: java.util.List[String]
)

@JsonInclude(Include.NON_ABSENT)
case class KielistettyResponse(
    @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
    @BeanProperty fi: Optional[String],
    @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
    @BeanProperty sv: Optional[String],
    @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
    @BeanProperty en: Optional[String]
)

object KielistettyResponse {
  def apply(kielistetty: Kielistetty): KielistettyResponse =
    KielistettyResponse(
      fi = kielistetty.get(Fi).toJava,
      sv = kielistetty.get(Sv).toJava,
      en = kielistetty.get(En).toJava
    )
}
