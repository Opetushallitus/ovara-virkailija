package fi.oph.ovara.backend.service

import fi.vm.sade.javautils.nio.cas.CasClient
import org.asynchttpclient.RequestBuilder
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.stereotype.Component

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.jdk.javaapi.FutureConverters.asScala

@Component
class OnrService {
  val LOG: Logger = LoggerFactory.getLogger(classOf[OnrService])

  @Value("${opintopolku.virkailija.domain}")
  val opintopolku_virkailija_domain: String = null
  
  @Autowired
  private val client: CasClient = null

  def getAsiointikieli(personOid: String): Either[Throwable, String] =
    LOG.info("Haetaan tiedot oppijanumerorekisteristä")
    val url = s"$opintopolku_virkailija_domain/oppijanumerorekisteri-service/henkilo/$personOid/asiointiKieli"
    fetch(url) match
      case Left(e) => Left(e)
      case Right(o) => Right(o)

  private def fetch(url: String): Either[Throwable, String] =
    val req = new RequestBuilder()
      .setMethod("GET")
      .setUrl(url)
      .build()
    try
      val result = asScala(client.execute(req)).map {
        case r if r.getStatusCode == 200 =>
          Right(r.getResponseBody())
        case r =>
          LOG.error(s"Kutsu oppijanumerorekisteriin epäonnistui: ${r.getStatusCode} ${r.getStatusText} ${r.getResponseBody()}")
          Left(new RuntimeException("Failed to fetch asiointikieli: " + r.getResponseBody()))
      }
      Await.result(result, Duration(10, TimeUnit.SECONDS))
    catch
      case e: Throwable => Left(e)
}
