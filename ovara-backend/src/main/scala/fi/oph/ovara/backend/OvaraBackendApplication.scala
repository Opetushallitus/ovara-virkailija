package fi.oph.ovara.backend;

import fi.oph.ovara.backend.domain.Toteutus
import fi.oph.ovara.backend.repository.OvaraDatabase
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
  @Bean
  def applicationRunner(environment: Environment) =
    new ApplicationRunner:
      override def run(args: ApplicationArguments): Unit =
        println("STARTING APPLICATION RUNNER")
        val res = OvaraDatabase(environment).run()
        println(res)

  /*@Bean
  def commandLineRunner(ctx: ApplicationContext) =
    new CommandLineRunner:
      override def run(args: String*): Unit = {
        System.out.println("Let's inspect the beans provided by Spring Boot:")
        val beanNames = ctx.getBeanDefinitionNames
        for (beanName <- beanNames) {
          System.out.println(beanName)
        }
      }*/
}