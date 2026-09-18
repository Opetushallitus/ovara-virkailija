package fi.oph.ovara.backend.repository

import com.zaxxer.hikari.HikariConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.{Component, Repository}

@Component
@Repository
class WriteDatabase(
  @Value("${spring.datasource.url}") url: String,
  @Value("${spring.datasource.username}") username: String,
  @Value("${spring.datasource.password}") password: String,
  @Value("${use.aws.jdbc.wrapper:false}") useAwsJdbcWrapper: String,
  @Value("${cluster.instance.host.pattern:}") clusterInstanceHostPattern: String
) extends OvaraDatabase {

  override protected def hikariConfig: HikariConfig = {
    val config = new HikariConfig()
    if (useAwsJdbcWrapper.toBoolean) {
      if (clusterInstanceHostPattern.isBlank) {
        throw new IllegalStateException(
          "Asetus cluster.instance.host.pattern on pakollinen, kun AWS JDBC -wrapper on " +
            "käytössä (use.aws.jdbc.wrapper=true)."
        )
      }
      config.setDriverClassName("software.amazon.jdbc.Driver")
      config.setJdbcUrl(url.replace("jdbc:postgresql:", "jdbc:aws-wrapper:postgresql:"))
      LOG.info(s"Käytetään clusterInstanceHostPattern-arvoa: $clusterInstanceHostPattern")
      config.addDataSourceProperty("clusterInstanceHostPattern", clusterInstanceHostPattern)
    } else {
      config.setJdbcUrl(url)
    }
    config.setUsername(username)
    config.setPassword(password)
    config.setMaximumPoolSize(10)
    config.setMinimumIdle(2)
    config
  }
}
