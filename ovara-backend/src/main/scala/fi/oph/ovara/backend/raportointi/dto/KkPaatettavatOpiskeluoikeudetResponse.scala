package fi.oph.ovara.backend.raportointi.dto

import fi.oph.ovara.backend.utils.Constants.DATE_FORMATTER_FOR_EXCEL
import fi.oph.ovara.backend.domain.KkPaatettavaOpiskeluoikeus
import fi.oph.ovara.backend.opiskelijavalintatieto.KielistettyResponse
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode

import java.time.ZonedDateTime
import java.util.Optional
import scala.annotation.meta.field
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.RichOption

case class KkPaatettavatOpiskeluoikeudetResponse(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty muodostusAikaleima: ZonedDateTime,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty henkilot: java.util.List[KkPaatettavatOpiskeluoikeudetHenkilo]
)

case class KkPaatettavatOpiskeluoikeudetHenkilo(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty oppijanumero: String,
  @(Schema @field)(requiredMode = RequiredMode.NOT_REQUIRED)
  @BeanProperty henkilotunnus: Optional[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty syntymaaika: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty sukunimi: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty etunimet: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty kutsumanimi: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty paatettavatOpiskeluoikeudet: java.util.List[KkPaatettavaOpiskeluoikeusTiedot]
)

case class KkPaatettavaOpiskeluoikeusTiedot(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty virtaTiedot: VirtaTiedot,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty vastaanottoTiedot: VastaanottoTiedot
)

case class VirtaTiedot(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty opiskelijaAvain: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty opiskeluoikeusAvain: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty nimi: KielistettyResponse,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty paattymisPaivamaara: Optional[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty tila: String
)

case class VastaanottoTiedot(
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakemusOid: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakuOid: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakuNimi: KielistettyResponse,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakukohdeOid: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty hakukohdeNimi: KielistettyResponse,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty oppilaitosOid: String,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty oppilaitosNimi: KielistettyResponse,
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty koulutusKoodit: java.util.List[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty opiskeluoikeusAlkamisaika: Optional[String],
  @(Schema @field)(requiredMode = RequiredMode.REQUIRED)
  @BeanProperty paikanVastaanottoaika: String
)

def buildKkPaatettavatOpiskeluoikeudetResponse(
  data: List[KkPaatettavaOpiskeluoikeus],
  muodostusAikaleima: ZonedDateTime
): KkPaatettavatOpiskeluoikeudetResponse = {
  val opiskeluoikeudetByOppijanumero = data.groupBy(_.oppijanumero)

  val henkilot = data.distinctBy(_.oppijanumero).map { henkilo =>
    val opiskeluoikeudet = opiskeluoikeudetByOppijanumero(henkilo.oppijanumero).map { item =>
      KkPaatettavaOpiskeluoikeusTiedot(
        virtaTiedot = VirtaTiedot(
          opiskelijaAvain = item.opiskelijaAvain,
          opiskeluoikeusAvain = item.opiskeluoikeusAvain,
          nimi = KielistettyResponse(item.opiskeluoikeudenNimi),
          paattymisPaivamaara = item.opiskeluoikeudenPaattymispvm.map(_.format(DATE_FORMATTER_FOR_EXCEL)).toJava,
          tila = item.opiskeluoikeudenViimeisinTila
        ),
        vastaanottoTiedot = VastaanottoTiedot(
          hakemusOid = item.hakemusOid,
          hakuOid = item.hakuOid,
          hakuNimi = KielistettyResponse(item.hakuNimi),
          hakukohdeOid = item.hakukohdeOid,
          hakukohdeNimi = KielistettyResponse(item.hakukohdeNimi),
          oppilaitosOid = item.oppilaitosOid,
          oppilaitosNimi = KielistettyResponse(item.oppilaitosNimi),
          koulutusKoodit = item.koulutusluokitusKoodit.asJava,
          opiskeluoikeusAlkamisaika =
            item.uudenOpiskeluoikeudenAlkamispvm.map(_.format(DATE_FORMATTER_FOR_EXCEL)).toJava,
          paikanVastaanottoaika = item.vastaanottoAjankohta.format(DATE_FORMATTER_FOR_EXCEL)
        )
      )
    }

    KkPaatettavatOpiskeluoikeudetHenkilo(
      oppijanumero = henkilo.oppijanumero,
      henkilotunnus = henkilo.hetu.toJava,
      syntymaaika = henkilo.syntymaAika.format(DATE_FORMATTER_FOR_EXCEL),
      sukunimi = henkilo.sukunimi,
      etunimet = henkilo.etunimet,
      kutsumanimi = henkilo.kutsumanimi,
      paatettavatOpiskeluoikeudet = opiskeluoikeudet.asJava
    )
  }

  KkPaatettavatOpiskeluoikeudetResponse(muodostusAikaleima, henkilot.asJava)
}
