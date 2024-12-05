package fi.oph.ovara.backend;

import org.slf4j.{Logger, LoggerFactory}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.{ApplicationArguments, ApplicationRunner, SpringApplication}
import org.springframework.context.annotation.{Bean, Profile}
import org.springframework.web.servlet.config.annotation.{CorsRegistry, WebMvcConfigurer}

object OvaraBackendApplication {
  val CALLER_ID = "1.2.246.562.10.00000000001.ovara-virkailija"
  
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[OvaraBackendApplication], args *)
  }
}

@SpringBootApplication
class OvaraBackendApplication {

  val LOG: Logger = LoggerFactory.getLogger(classOf[OvaraBackendApplication])

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
