package fi.oph.ovara.backend.repository

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import fi.oph.ovara.backend.domain.Toteutus
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import slick.jdbc.GetResult
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api.*
import slick.util.AsyncExecutor

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration

@Component
case class OvaraDatabase(environment: Environment) {

  //@Value("${spring.datasource.url}")
  var url: String = environment.getProperty("spring.datasource.url")

  //@Value("${spring.datasource.username")
  var username: String = environment.getProperty("spring.datasource.username")

  //@Value("${spring.datasource.password}")
  var password: String = environment.getProperty("spring.datasource.password")

  private def hikariConfig: HikariConfig = {
    val config = new HikariConfig()
    config.setJdbcUrl(url)
    config.setUsername(username)
    config.setPassword(password)
    val maxPoolSize = 10
    config.setMaximumPoolSize(maxPoolSize)
    config.setMinimumIdle(1)
    config
  }

  @Bean
  val db = {
    val executor = AsyncExecutor("ovara", hikariConfig.getMaximumPoolSize, 1000)
    Database.forDataSource(
      new HikariDataSource(hikariConfig),
      maxConnections = Some(hikariConfig.getMaximumPoolSize),
      executor
    )
  }

  implicit val getToteutusResult: GetResult[Toteutus] = GetResult(r => Toteutus(
    oid = r.nextString()
  ))

  def selectWithOid() = {
    sql"""select * from pub.pub_dim_toteutus where oid = '1.2.246.562.17.00000000000000003709'""".as[Toteutus]
  }

  def run(): Vector[Toteutus] = {
    println("RUN")
    Await.result(
      db.run(selectWithOid()),
      Duration(100, TimeUnit.SECONDS)
    )
  }
}
