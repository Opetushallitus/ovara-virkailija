package fi.oph.ovara.backend;

import fi.oph.ovara.backend.repository.OvaraDatabase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.{ApplicationArguments, ApplicationRunner, SpringApplication}
import org.springframework.context.annotation.Bean

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
  def applicationRunner() =
    new ApplicationRunner:
      override def run(args: ApplicationArguments): Unit =
        println("STARTING APPLICATION RUNNER")
}