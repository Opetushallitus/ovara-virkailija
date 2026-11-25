package fi.oph.ovara.backend.raportointi

import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.{Components, OpenAPI}
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration}

import scala.jdk.CollectionConverters.*

@Configuration
class SwaggerConfig {

  // Swagger UI kutsuu speksin palvelinosoitetta. Ilman sitä springdoc päättelee osoitteen pyynnöstä, joka
  // kuormantasaajan takana on http, jolloin selain estää https-sivulta tehdyt kutsut (Content-Security-Policy).
  @Value("${ovara.backend.url}")
  val ovara_backend_url: String = null

  @Bean
  def publicApi(): GroupedOpenApi = {
    GroupedOpenApi
      .builder()
      .group("ovara-apis")
      .pathsToMatch("/**")
      .build()
  }

  @Bean
  def ovaraOpenApi(): OpenAPI = {
    OpenAPI()
      .info(Info().title("Ovara API").version("1"))
      .servers(List(Server().url(ovara_backend_url)).asJava)
      .components(
        Components()
      )
  }

}
