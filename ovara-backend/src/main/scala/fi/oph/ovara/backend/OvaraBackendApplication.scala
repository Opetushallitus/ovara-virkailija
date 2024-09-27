package fi.oph.ovara.backend;

import fi.oph.ovara.backend.repository.OvaraDatabase
import org.springframework.beans.factory.annotation.Autowired
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
  @Autowired
  val db: OvaraDatabase = null

  @Bean
  def applicationRunner(): ApplicationRunner =
    new ApplicationRunner:
      override def run(args: ApplicationArguments): Unit =
        println("STARTING APPLICATION RUNNER")
 
  @Profile(Array("dev"))
  @Bean
  def corsConfigurer(): WebMvcConfigurer = new WebMvcConfigurer {
    override def addCorsMappings(registry: CorsRegistry): Unit = {
      registry.addMapping("/**").allowedOrigins("https://localhost:3405")
    }
  }
}