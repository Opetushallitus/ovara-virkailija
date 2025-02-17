package fi.oph.ovara.backend.security

import com.zaxxer.hikari.HikariDataSource
import fi.oph.ovara.backend.OvaraBackendApplication.CALLER_ID
import fi.oph.ovara.backend.utils.AuditLog
import fi.vm.sade.javautils.kayttooikeusclient.OphUserDetailsServiceImpl
import fi.vm.sade.javautils.nio.cas.{CasClient, CasClientBuilder, CasConfig}
import org.apereo.cas.client.session.{SessionMappingStorage, SingleSignOutFilter}
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
import org.springframework.security.web.context.{HttpSessionSecurityContextRepository, SecurityContextRepository}
import org.springframework.session.jdbc.config.annotation.SpringSessionDataSource
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession

@Configuration
@EnableWebSecurity
@EnableJdbcHttpSession(tableName = "VIRKAILIJA_SESSION")
class SecurityConfig  {
  private final val LOGIN_PATH = "/auth/login"
  private final val SPRING_CAS_SECURITY_CHECK_PATH = "/j_spring_cas_security_check"

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

  @Value("${session.schema.name}")
  private val schema = null

  @Bean
  def auditLog(): AuditLog = AuditLog
  
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
  @SpringSessionDataSource
  def sessionDatasource(@Value("${spring.datasource.url}") url: String,
                        @Value("${spring.datasource.username}") username: String,
                        @Value("${spring.datasource.password}") password: String,
                        @Value("${session.schema.name}") schema: String): HikariDataSource = {
    val config = new HikariDataSource()
    config.setJdbcUrl(url)
    config.setUsername(username)
    config.setPassword(password)
    config.addDataSourceProperty("currentSchema", schema)
    config.setMaximumPoolSize(2)
    config
  }

  @Bean
  def securityContextRepository(): HttpSessionSecurityContextRepository = {
    val httpSessionSecurityContextRepository = new HttpSessionSecurityContextRepository()
    httpSessionSecurityContextRepository
  }

  @Bean
  def serviceProperties(): ServiceProperties = {
    val serviceProperties = ServiceProperties()
    serviceProperties.setService(ovara_backend_url + SPRING_CAS_SECURITY_CHECK_PATH)
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
  def casAuthenticationFilter(authenticationManager: AuthenticationManager, serviceProperties: ServiceProperties, securityContextRepository: SecurityContextRepository): CasAuthenticationFilter = {
    val casAuthenticationFilter = CasAuthenticationFilter()
    casAuthenticationFilter.setAuthenticationManager(authenticationManager)
    casAuthenticationFilter.setServiceProperties(serviceProperties)
    casAuthenticationFilter.setFilterProcessesUrl(SPRING_CAS_SECURITY_CHECK_PATH)
    casAuthenticationFilter.setSecurityContextRepository(securityContextRepository)
    casAuthenticationFilter
  }

  @Bean
  def casFilterChain(http: HttpSecurity, authenticationFilter: CasAuthenticationFilter, sessionMappingStorage: SessionMappingStorage, securityContextRepository: SecurityContextRepository, casAuthenticationEntryPoint: CasAuthenticationEntryPoint): SecurityFilterChain = {
    val SWAGGER_WHITELIST = List(
      "/swagger-resources",
      "/swagger-resources/**",
      "/swagger-ui.html",
      "/swagger-ui/**"
    )

    http
      .securityMatcher("/**")
      .authorizeHttpRequests(requests => requests
        .requestMatchers("/api/healthcheck", "/api/csrf").permitAll()
        .requestMatchers(SWAGGER_WHITELIST: _*).permitAll()
        .anyRequest().fullyAuthenticated()
      )
      .csrf(csrf => csrf
        .ignoringRequestMatchers("/api/healthcheck", "/api/csrf")
      )
      .exceptionHandling(exceptionHandling =>
        exceptionHandling.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
      )
      .addFilterAt(authenticationFilter, classOf[CasAuthenticationFilter])
      .addFilterBefore(singleLogoutFilter(sessionMappingStorage), classOf[CasAuthenticationFilter])
      .securityContext(securityContext => securityContext
        .requireExplicitSave(true)
        .securityContextRepository(securityContextRepository))
      .logout(logout =>
        logout.logoutUrl("/logout")
          .deleteCookies("JSESSIONID"))
      .build()
  }


  @Bean
  @Order(1)
  def apiLoginFilterChain(http: HttpSecurity, casAuthenticationEntryPoint: CasAuthenticationEntryPoint): SecurityFilterChain = {
    http
      .securityMatcher("/api/login")
      .authorizeHttpRequests(requests =>
        requests.requestMatchers(SPRING_CAS_SECURITY_CHECK_PATH).permitAll() // päästetään läpi cas-logout
        .anyRequest.fullyAuthenticated)
      .exceptionHandling(c => c.authenticationEntryPoint(casAuthenticationEntryPoint))
      .build()
  }

  //
  // Käsitellään CASilta tuleva SLO-pyyntö
  //
  @Bean
  def singleLogoutFilter(sessionMappingStorage: SessionMappingStorage): SingleSignOutFilter = {
    val singleSignOutFilter: SingleSignOutFilter = new SingleSignOutFilter();
    singleSignOutFilter.setIgnoreInitConfiguration(true);
    singleSignOutFilter
  }

}