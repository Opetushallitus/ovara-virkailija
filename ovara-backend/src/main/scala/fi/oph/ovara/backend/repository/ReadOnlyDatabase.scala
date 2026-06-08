package fi.oph.ovara.backend.repository

import com.zaxxer.hikari.HikariConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.{Component, Repository}

@Component
@Repository
class ReadOnlyDatabase(
                        @Value("${app.readonly.datasource.url}") url: String,
                        @Value("${spring.datasource.username}") username: String,
                        @Value("${spring.datasource.password}") password: String,
                        @Value("${use.aws.jdbc.wrapper:false}") useAwsJdbcWrapper: String
                      ) extends OvaraDatabase {
  override protected def hikariConfig: HikariConfig = {
    val config = new HikariConfig()
    if (useAwsJdbcWrapper.toBoolean) {
      config.setDriverClassName("software.amazon.jdbc.Driver")
      config.setJdbcUrl(url.replace("jdbc:postgresql:", "jdbc:aws-wrapper:postgresql:"))
    } else {
      config.setJdbcUrl(url)
    }
    config.setUsername(username)
    config.setPassword(password)
    config.setMaximumPoolSize(10)
    config.setMinimumIdle(1)
    config
  }

}
