package fi.oph.ovara.backend.raportointi.dto

import fi.oph.ovara.backend.domain.{Fi, KkPaatettavaOpiskeluoikeus, Sv}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.{LocalDate, ZonedDateTime}
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.RichOptional

class KkPaatettavatOpiskeluoikeudetResponseSpec extends AnyFlatSpec with Matchers {

  val muodostusAikaleima: ZonedDateTime = ZonedDateTime.now()

  val opiskeluoikeus: KkPaatettavaOpiskeluoikeus = KkPaatettavaOpiskeluoikeus(
    sukunimi = "Testinen",
    etunimet = "Testi",
    kutsumanimi = "Testi",
    hetu = Some("010101-1234"),
    syntymaAika = LocalDate.of(2001, 1, 1),
    oppijanumero = "1.2.246.562.24.10002324020",
    opiskeluoikeudenNimi = Map(Fi -> "Tähtitiede", Sv -> "Astronomi"),
    opiskeluoikeudenPaattymispvm = Some(LocalDate.of(2026, 12, 31)),
    opiskeluoikeudenViimeisinTila = "Loma",
    opiskelijaAvain = "opiskelija-avain-1",
    opiskeluoikeusAvain = "opiskeluoikeus-avain-1",
    naytettyHakijalle = true,
    hakemusOid = "1.2.246.562.11.00000000000000000001",
    hakukohdeNimi = Map(Fi -> "Meteorologi", Sv -> "Meteorolog"),
    hakukohdeOid = "1.2.246.562.20.00000000000000000002",
    oppilaitosNimi = Map(Fi -> "Yliopisto", Sv -> "Universitet"),
    oppilaitosOid = "1.2.246.562.10.00000000000000000001",
    uudenOpiskeluoikeudenAlkamispvm = Some(LocalDate.of(2026, 9, 1)),
    vastaanottoAjankohta = LocalDate.of(2026, 8, 15),
    koulutusluokitusKoodit = List("12345", "67890"),
    hakuOid = "1.2.246.562.20.00000000000000000001",
    hakuNimi = Map(Fi -> "Erillishaku", Sv -> "Separat ansökan")
  )

  "buildKkPaatettavatOpiskeluoikeudetResponse" should "map all fields of KkPaatettavaOpiskeluoikeudet to the response" in {
    val response = buildKkPaatettavatOpiskeluoikeudetResponse(List(opiskeluoikeus), muodostusAikaleima)

    response.muodostusAikaleima shouldEqual muodostusAikaleima
    response.henkilot.size shouldEqual 1

    val henkilo = response.henkilot.asScala.head
    henkilo.oppijanumero shouldEqual opiskeluoikeus.oppijanumero
    henkilo.henkilotunnus.toScala shouldEqual opiskeluoikeus.hetu
    henkilo.syntymaaika shouldEqual "1.1.2001"
    henkilo.sukunimi shouldEqual opiskeluoikeus.sukunimi
    henkilo.etunimet shouldEqual opiskeluoikeus.etunimet
    henkilo.kutsumanimi shouldEqual opiskeluoikeus.kutsumanimi
    henkilo.paatettavatOpiskeluoikeudet.size shouldEqual 1

    val tiedot = henkilo.paatettavatOpiskeluoikeudet.asScala.head

    val virtaTiedot = tiedot.virtaTiedot
    virtaTiedot.opiskelijaAvain shouldEqual opiskeluoikeus.opiskelijaAvain
    virtaTiedot.opiskeluoikeusAvain shouldEqual opiskeluoikeus.opiskeluoikeusAvain
    virtaTiedot.nimi.fi.toScala shouldEqual Some("Tähtitiede")
    virtaTiedot.nimi.sv.toScala shouldEqual Some("Astronomi")
    virtaTiedot.paattymisPaivamaara.toScala shouldEqual Some("31.12.2026")
    virtaTiedot.tila shouldEqual opiskeluoikeus.opiskeluoikeudenViimeisinTila

    val vastaanottoTiedot = tiedot.vastaanottoTiedot
    vastaanottoTiedot.hakemusOid shouldEqual opiskeluoikeus.hakemusOid
    vastaanottoTiedot.hakuOid shouldEqual opiskeluoikeus.hakuOid
    vastaanottoTiedot.hakuNimi.fi.toScala shouldEqual Some("Erillishaku")
    vastaanottoTiedot.hakuNimi.sv.toScala shouldEqual Some("Separat ansökan")
    vastaanottoTiedot.hakukohdeOid shouldEqual opiskeluoikeus.hakukohdeOid
    vastaanottoTiedot.hakukohdeNimi.fi.toScala shouldEqual Some("Meteorologi")
    vastaanottoTiedot.hakukohdeNimi.sv.toScala shouldEqual Some("Meteorolog")
    vastaanottoTiedot.oppilaitosOid shouldEqual opiskeluoikeus.oppilaitosOid
    vastaanottoTiedot.oppilaitosNimi.fi.toScala shouldEqual Some("Yliopisto")
    vastaanottoTiedot.oppilaitosNimi.sv.toScala shouldEqual Some("Universitet")
    vastaanottoTiedot.koulutusKoodit.asScala.toList shouldEqual opiskeluoikeus.koulutusluokitusKoodit
    vastaanottoTiedot.opiskeluoikeusAlkamisaika.toScala shouldEqual Some("1.9.2026")
    vastaanottoTiedot.paikanVastaanottoaika shouldEqual "15.8.2026"
    vastaanottoTiedot.naytettyHakijalle shouldEqual true
  }

  it should "map optional fields to empty when absent" in {
    val opiskeluoikeusWithoutOptionals = opiskeluoikeus.copy(
      hetu = None,
      opiskeluoikeudenPaattymispvm = None,
      uudenOpiskeluoikeudenAlkamispvm = None,
      naytettyHakijalle = false
    )

    val response = buildKkPaatettavatOpiskeluoikeudetResponse(List(opiskeluoikeusWithoutOptionals), muodostusAikaleima)
    val henkilo  = response.henkilot.asScala.head
    val tiedot   = henkilo.paatettavatOpiskeluoikeudet.asScala.head

    henkilo.henkilotunnus.toScala shouldEqual None
    tiedot.virtaTiedot.paattymisPaivamaara.toScala shouldEqual None
    tiedot.vastaanottoTiedot.opiskeluoikeusAlkamisaika.toScala shouldEqual None
    tiedot.vastaanottoTiedot.naytettyHakijalle shouldEqual false
  }

  it should "group multiple opiskeluoikeudet under the same henkilo by oppijanumero" in {
    val toinenOpiskeluoikeus = opiskeluoikeus.copy(
      opiskeluoikeusAvain = "opiskeluoikeus-avain-2",
      opiskelijaAvain = "opiskelija-avain-2",
      naytettyHakijalle = false
    )

    val response = buildKkPaatettavatOpiskeluoikeudetResponse(
      List(opiskeluoikeus, toinenOpiskeluoikeus),
      muodostusAikaleima
    )

    response.henkilot.size shouldEqual 1
    response.henkilot.asScala.head.paatettavatOpiskeluoikeudet.size shouldEqual 2
  }
}
