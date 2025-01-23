package fi.oph.ovara.backend.raportointi

import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.{Components, OpenAPI}
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
class SwaggerConfig {

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
      .components(
        Components()
      )
  }

}
