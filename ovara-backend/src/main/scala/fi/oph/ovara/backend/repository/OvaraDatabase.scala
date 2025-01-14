package fi.oph.ovara.backend.repository

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend.Database
import slick.util.AsyncExecutor

import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration

@Component
class OvaraDatabase(@Value("${spring.datasource.url}") url: String,
                    @Value("${spring.datasource.username}") username: String,
                    @Value("${spring.datasource.password}") password: String) {

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

  def run[R](operations: DBIO[R]) = {
    LOG.info("Running db query.")

    Await.result(
      db.run(operations),
      Duration(100, TimeUnit.SECONDS)
    )
  }
}
