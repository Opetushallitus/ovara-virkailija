package fi.oph.ovara.backend.security

import fi.oph.ovara.backend.OvaraBackendApplication.CALLER_ID
import fi.vm.sade.javautils.kayttooikeusclient.OphUserDetailsServiceImpl
import fi.vm.sade.javautils.nio.cas.{CasClient, CasClientBuilder, CasConfig}
import org.apereo.cas.client.session.SingleSignOutFilter
import org.apereo.cas.client.validation.{Cas20ServiceTicketValidator, TicketValidator}
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration}
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

  @Value("${opintopolku.virkailija.url}")
  val opintopolku_virkailija_domain: String = null

  @Value("${ovara-backend.cas.username}")
  val cas_username: String = null

  @Value("${ovara-backend.cas.password}")
  val cas_password: String = null

  @Bean
  def createCasClient(): CasClient = CasClientBuilder.build(CasConfig.CasConfigBuilder(
    cas_username,
    cas_password,
    s"$opintopolku_virkailija_domain/cas",
    s"$opintopolku_virkailija_domain/oppijanumerorekisteri-service",
    CALLER_ID,
    CALLER_ID,
    "/j_spring_cas_security_check"
  ).setJsessionName("JSESSIONID").build())

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
  @Order(3)
  def healthcheckFilterChain(http: HttpSecurity): SecurityFilterChain = {
    http
      .securityMatcher("/api/healthcheck")
      .authorizeHttpRequests(requests => requests.anyRequest.permitAll)
      .csrf(c => c.disable)
      .build()
  }

  @Bean
  @Order(1)
  def csrfFilterChain(http: HttpSecurity): SecurityFilterChain = {
    http
      .securityMatcher("/api/csrf")
      .authorizeHttpRequests(requests => requests.anyRequest.permitAll)
      .exceptionHandling(exceptionHandling =>
        exceptionHandling.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
      )
      .build()
  }

  @Bean
  @Order(2)
  def apiLoginFilterChain(http: HttpSecurity, casAuthenticationEntryPoint: CasAuthenticationEntryPoint): SecurityFilterChain = {
    http
      .securityMatcher("/api/login")
      .authorizeHttpRequests(requests => requests.anyRequest.fullyAuthenticated)
      .exceptionHandling(c => c.authenticationEntryPoint(casAuthenticationEntryPoint))
      .build()
  }

  @Bean
  def swaggerFilterChain(http: HttpSecurity): SecurityFilterChain = {
    val SWAGGER_WHITELIST = List(
      "/swagger-resources",
      "/swagger-resources/**",
      "/swagger-ui.html",
      "/swagger-ui/**"
    )

    http
    .securityMatcher(SWAGGER_WHITELIST*)
      .authorizeHttpRequests(requests => requests.anyRequest().permitAll())
      .build()
  }
}