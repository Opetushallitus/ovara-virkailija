package fi.oph.ovara.backend.repository

import com.zaxxer.hikari.HikariConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.{Component, Repository}

@Component
@Repository
class WriteDatabase(
                     @Value("${spring.datasource.url}") url: String,
                     @Value("${spring.datasource.username}") username: String,
                     @Value("${spring.datasource.password}") password: String
                   ) extends OvaraDatabase {

  override protected def hikariConfig: HikariConfig = {
    val config = new HikariConfig()
    config.setJdbcUrl(url)
    config.setUsername(username)
    config.setPassword(password)
    config.setMaximumPoolSize(10)
    config.setMinimumIdle(2)
    config
  }
}
