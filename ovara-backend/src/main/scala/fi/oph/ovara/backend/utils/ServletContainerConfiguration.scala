package fi.oph.ovara.backend.utils

import org.apache.catalina.connector.Connector
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.embedded.tomcat.{TomcatConnectorCustomizer, TomcatServletWebServerFactory}
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
@ConditionalOnProperty(Array("ovara_backend.uses-ssl-proxy"))
class ServletContainerConfiguration {

  @Bean
  def sslProxyCustomizer(): WebServerFactoryCustomizer[TomcatServletWebServerFactory] =
    (container: TomcatServletWebServerFactory) =>
      container.addConnectorCustomizers((connector: Connector) => {
        connector.setScheme("https")
        connector.setSecure(true)
      })
}
