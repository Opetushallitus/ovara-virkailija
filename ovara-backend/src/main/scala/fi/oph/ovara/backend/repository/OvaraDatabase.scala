package fi.oph.ovara.backend.repository

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.{Component, Repository}
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend.Database
import slick.util.AsyncExecutor

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration

@Component
@Repository
class OvaraDatabase(
    @Value("${spring.datasource.url}") url: String,
    @Value("${spring.datasource.username}") username: String,
    @Value("${spring.datasource.password}") password: String
) {

  val LOG = LoggerFactory.getLogger(classOf[OvaraDatabase]);

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

  def run[R](operations: DBIO[R], id: String): R = {
    LOG.info(s"Running db query: $id")

    val result = Await.result(
      db.run(operations),
      Duration(150, TimeUnit.SECONDS)
    )

    LOG.info(s"Db query finished: $id")
    result
  }
}
