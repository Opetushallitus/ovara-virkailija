package fi.oph.ovara.backend;

import fi.oph.ovara.backend.domain.Toteutus
import fi.oph.ovara.backend.repository.OvaraDatabase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.{ApplicationArguments, ApplicationRunner, CommandLineRunner, SpringApplication}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment


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
  def applicationRunner(environment: Environment) =
    new ApplicationRunner:
      override def run(args: ApplicationArguments): Unit =
        println("STARTING APPLICATION RUNNER")
        println(db)
        println(db.url)
        println(db.url2)
}