package fi.oph.ovara.backend.raportointi.dto

import fi.oph.ovara.backend.domain.{Kielistetty, KkPaatettavaOpiskeluoikeus}

import java.time.{LocalDate, LocalDateTime}

case class KkPaatettavatOpiskeluoikeudetResponse(
  muodostusAikaleima: LocalDateTime,
  henkilot: List[KkPaatettavatOpiskeluoikeudetHenkilo]
)

case class KkPaatettavatOpiskeluoikeudetHenkilo(
  oppijanumero: String,
  henkilotunnus: Option[String],
  syntymaaika: LocalDate,
  sukunimi: String,
  etunimet: String,
  kutsumanimi: String,
  paatettavatOpiskeluoikeudet: List[KkPaatettavaOpiskeluoikeusTiedot]
)

case class KkPaatettavaOpiskeluoikeusTiedot(
  virtaTiedot: VirtaTiedot,
  vastaanottoTiedot: VastaanottoTiedot
)

case class VirtaTiedot(
  opiskelijaAvain: String,
  opiskeluoikeusAvain: String,
  nimi: Kielistetty,
  paattymisPaivamaara: Option[LocalDate],
  tila: String
)

case class VastaanottoTiedot(
  hakemusOid: String,
  hakuOid: String,
  hakuNimi: Kielistetty,
  hakukohdeOid: String,
  hakukohdeNimi: Kielistetty,
  oppilaitosOid: String,
  oppilaitosNimi: Kielistetty,
  koulutusKoodit: List[KoulutusKoodi],
  opiskeluoikeusAlkamisaika: Option[LocalDate],
  paikanVastaanottoaika: LocalDate
)

case class KoulutusKoodi(koodiArvo: String)

def buildKkPaatettavatOpiskeluoikeudetResponse(
  data: List[KkPaatettavaOpiskeluoikeus],
  muodostusAikaleima: LocalDateTime
): KkPaatettavatOpiskeluoikeudetResponse = {
  val opiskeluoikeudetByOppijanumero = data.groupBy(_.oppijanumero)

  val henkilot = data.distinctBy(_.oppijanumero).map { henkilo =>
    val opiskeluoikeudet = opiskeluoikeudetByOppijanumero(henkilo.oppijanumero).map { item =>
      KkPaatettavaOpiskeluoikeusTiedot(
        virtaTiedot = VirtaTiedot(
          opiskelijaAvain = item.opiskelijaAvain,
          opiskeluoikeusAvain = item.opiskeluoikeusAvain,
          nimi = item.opiskeluoikeudenNimi,
          paattymisPaivamaara = item.opiskeluoikeudenPaattymispvm,
          tila = item.opiskeluoikeudenViimeisinTila
        ),
        vastaanottoTiedot = VastaanottoTiedot(
          hakemusOid = item.hakemusOid,
          hakuOid = item.hakuOid,
          hakuNimi = item.hakuNimi,
          hakukohdeOid = item.hakukohdeOid,
          hakukohdeNimi = item.hakukohdeNimi,
          oppilaitosOid = item.oppilaitosOid,
          oppilaitosNimi = item.oppilaitosNimi,
          koulutusKoodit = item.koulutusluokitusKoodit.map(KoulutusKoodi.apply),
          opiskeluoikeusAlkamisaika = item.uudenOpiskeluoikeudenAlkamispvm,
          paikanVastaanottoaika = item.vastaanottoAjankohta
        )
      )
    }

    KkPaatettavatOpiskeluoikeudetHenkilo(
      oppijanumero = henkilo.oppijanumero,
      henkilotunnus = henkilo.hetu,
      syntymaaika = henkilo.syntymaAika,
      sukunimi = henkilo.sukunimi,
      etunimet = henkilo.etunimet,
      kutsumanimi = henkilo.kutsumanimi,
      paatettavatOpiskeluoikeudet = opiskeluoikeudet
    )
  }

  KkPaatettavatOpiskeluoikeudetResponse(muodostusAikaleima, henkilot)
}
