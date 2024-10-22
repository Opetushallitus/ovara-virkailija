package fi.oph.ovara.backend.security

import fi.vm.sade.javautils.kayttooikeusclient.OphUserDetailsServiceImpl
import org.apereo.cas.client.session.SingleSignOutFilter
import org.apereo.cas.client.validation.{Cas20ServiceTicketValidator, TicketValidator}
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration, Profile}
import org.springframework.core.annotation.Order
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.cas.ServiceProperties
import org.springframework.security.cas.authentication.CasAuthenticationProvider
import org.springframework.security.cas.web.{CasAuthenticationEntryPoint, CasAuthenticationFilter}
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint

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
  def casFilterChain(http: HttpSecurity, authenticationManager: AuthenticationManager, serviceProperties: ServiceProperties): SecurityFilterChain = {
    val casAuthenticationFilter = CasAuthenticationFilter()
    casAuthenticationFilter.setAuthenticationManager(authenticationManager)
    casAuthenticationFilter.setServiceProperties(serviceProperties)
    casAuthenticationFilter.setFilterProcessesUrl("/auth/login")

    val singleSignOutFilter = new SingleSignOutFilter()

    http
      .securityMatcher("/auth/login")
      .csrf(csrf => csrf.disable)
      .addFilter(casAuthenticationFilter)
      .addFilterBefore(singleSignOutFilter, classOf[CasAuthenticationFilter])
      .build()
  }

  @Bean
  def apiDefaultFilterChain(http: HttpSecurity): SecurityFilterChain = {
    http
      .securityMatcher("/api/**")
      .authorizeHttpRequests(requests => requests.anyRequest.fullyAuthenticated)
      .exceptionHandling(exceptionHandling =>
        exceptionHandling.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
      )
      .build()
  }

  @Bean
  @Order(1)
  def apiLoginFilterChain(http: HttpSecurity, casAuthenticationEntryPoint: CasAuthenticationEntryPoint): SecurityFilterChain = {
    http
      .securityMatcher("/api/login")
      .authorizeHttpRequests(requests => requests.anyRequest.fullyAuthenticated)
      .exceptionHandling(c => c.authenticationEntryPoint(casAuthenticationEntryPoint))
      .build()
  }
}