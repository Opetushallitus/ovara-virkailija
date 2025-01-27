package fi.oph.ovara.backend.service

import org.json4s.*
import org.json4s.jackson.JsonMethods.*
import org.slf4j.{Logger, LoggerFactory}
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.{CacheEvict, CachePut, Cacheable}
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.util.retry.Retry

import java.time.Duration

@Component
class LokalisointiService(userService: UserService, webClientBuilder: WebClient.Builder) {
  implicit val formats: Formats = DefaultFormats

  val LOG: Logger = LoggerFactory.getLogger(classOf[LokalisointiService])

  @Value("${opintopolku.virkailija.url}")
  val opintopolku_virkailija_domain: String = null

  @Autowired
  val cacheManager: CacheManager = null

  @Bean
  def webClient(webClientBuilder: WebClient.Builder): WebClient = {
    webClientBuilder.baseUrl(s"$opintopolku_virkailija_domain/lokalisointi/tolgee").build()
  }

  @Cacheable(value = Array("lokalisointi"))
  def getOvaraTranslations(asiointikieli: String): Map[String, String] = {

    val lokalisointiCache = cacheManager.getCache("lokalisointi")
    val cachedLokalisoinnit = lokalisointiCache.get(asiointikieli, classOf[Map[String, String]])

    if (cachedLokalisoinnit != null) {
      cachedLokalisoinnit
    } else {
      val json =
        try {
          webClient(webClientBuilder)
            .get()
            .uri(s"/ovara/$asiointikieli.json")
            .retrieve()
            .bodyToMono(classOf[String])
            .retryWhen(
              Retry
                .fixedDelay(3, Duration.ofSeconds(2))
                .filter(throwable => throwable.isInstanceOf[Exception])
                .doAfterRetry(retrySignal => LOG.error(s"Failed to fetch translations for language $asiointikieli"))
            )
            .block()
        } catch {
          case throwable: Throwable =>
            LOG.error(s"Failed to fetch translations for language $asiointikieli after max retries")
            ""
        }

      parse(json).extract[Map[String, String]]
    }
  }

  @CacheEvict(value = Array("lokalisointi"), allEntries = true)
  @Scheduled(fixedRateString = "${caching.spring.lokalisointiTTL}")
  def emptyLokalisointiCache(): Unit = {
    LOG.info("Emptying lokalisointi cache")
  }
}
