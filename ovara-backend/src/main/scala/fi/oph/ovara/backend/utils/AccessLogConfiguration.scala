package fi.oph.ovara.backend.utils

import ch.qos.logback.access.tomcat.LogbackValve
import org.apache.catalina.Context
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = Array("logback.access"))
class AccessLogConfiguration extends WebServerFactoryCustomizer[TomcatServletWebServerFactory] {
  override def customize(tomcat: TomcatServletWebServerFactory): Unit = {
    tomcat.addContextCustomizers((context: Context) => {
      val logbackValve = new LogbackValve
      logbackValve.setFilename("logback-access.xml")
      logbackValve.setAsyncSupported(true)
      context.getPipeline.addValve(logbackValve)
    })
  }
}
