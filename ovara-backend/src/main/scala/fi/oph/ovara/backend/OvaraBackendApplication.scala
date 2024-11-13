package fi.oph.ovara.backend;

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.{ApplicationArguments, ApplicationRunner, SpringApplication}
import org.springframework.context.annotation.{Bean, Profile}
import org.springframework.web.servlet.config.annotation.{CorsRegistry, WebMvcConfigurer}

object OvaraBackendApplication {
    def main(args: Array[String]): Unit = {
      SpringApplication.run(classOf[OvaraBackendApplication], args*)
    }
}

@SpringBootApplication
class OvaraBackendApplication {

  val LOG = LoggerFactory.getLogger(classOf[OvaraBackendApplication]);

  @Bean
  def applicationRunner(): ApplicationRunner =
    (args: ApplicationArguments) => LOG.info("STARTED OVARA APPLICATION RUNNER")

  @Profile(Array("dev"))
  @Bean
  def corsConfigurer(): WebMvcConfigurer = new WebMvcConfigurer {
    override def addCorsMappings(registry: CorsRegistry): Unit = {
      registry.addMapping("/**").allowedOrigins("https://localhost:3405")
    }
  }
}