package fi.oph.ovara.backend.security

import fi.vm.sade.javautils.kayttooikeusclient.OphUserDetailsServiceImpl
import org.apereo.cas.client.validation.{Cas20ServiceTicketValidator, TicketValidator}
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.core.env.Environment
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.cas.ServiceProperties
import org.springframework.security.cas.authentication.CasAuthenticationProvider
import org.springframework.security.cas.web.{CasAuthenticationEntryPoint, CasAuthenticationFilter}
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig  {
  @Value("${cas.url}")
  val cas_url: String = null

  @Value("${ovara.backend.url}")
  val ovara_backend_url: String = null

  @Bean
  def serviceProperties(): ServiceProperties = {
    val serviceProperties = ServiceProperties()
    serviceProperties.setService(ovara_backend_url + "/auth/login")
    serviceProperties.setSendRenew(false)
    serviceProperties
  }

  @Bean
  def casAuthenticationEntrypoint(environment: Environment, serviceProperties: ServiceProperties): CasAuthenticationEntryPoint = {
    val casAuthenticationEntryPoint = CasAuthenticationEntryPoint()
    casAuthenticationEntryPoint.setLoginUrl(cas_url + "/login")
    casAuthenticationEntryPoint.setServiceProperties(serviceProperties)
    casAuthenticationEntryPoint
  }

  @Bean
  def ticketValidator(): TicketValidator = {
    Cas20ServiceTicketValidator(cas_url)
  }

  @Bean
  def casAuthenticationProvider(serviceProperties: ServiceProperties, ticketValidator: TicketValidator): CasAuthenticationProvider = {
    val casAuthenticationProvider = CasAuthenticationProvider()
    casAuthenticationProvider.setAuthenticationUserDetailsService(new OphUserDetailsServiceImpl())
    casAuthenticationProvider.setServiceProperties(serviceProperties)
    casAuthenticationProvider.setTicketValidator(ticketValidator)
    casAuthenticationProvider.setKey("ovara-backend")
    casAuthenticationProvider
  }

  @Bean
  def authenticationManager(http: HttpSecurity, casAuthenticationProvider: CasAuthenticationProvider): AuthenticationManager = {
    http.getSharedObject(classOf[AuthenticationManagerBuilder])
      .authenticationProvider(casAuthenticationProvider)
      .build()
  }

  @Bean
  def casAuthenticationFilter(authenticationManager: AuthenticationManager, serviceProperties: ServiceProperties): CasAuthenticationFilter = {
    val casAuthenticationFilter = CasAuthenticationFilter()
    casAuthenticationFilter.setAuthenticationManager(authenticationManager)
    casAuthenticationFilter.setServiceProperties(serviceProperties)
    casAuthenticationFilter.setFilterProcessesUrl("/auth/login")
    casAuthenticationFilter
  }

  @Bean
  def filterChain(http: HttpSecurity, casAuthenticationEntryPoint: CasAuthenticationEntryPoint): SecurityFilterChain = {
    http
      .authorizeHttpRequests(authorizeHttpRequests =>
        authorizeHttpRequests
          .requestMatchers("/api/user").permitAll()
          .requestMatchers("/api/**").authenticated()
          .anyRequest().permitAll()
      )
      .exceptionHandling(exceptionHandling =>
        exceptionHandling.authenticationEntryPoint(casAuthenticationEntryPoint)
      )
      .build()
  }
}