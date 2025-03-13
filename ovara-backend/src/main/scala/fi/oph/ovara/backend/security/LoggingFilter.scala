package fi.oph.ovara.backend.security

import jakarta.servlet.{Filter, FilterChain, FilterConfig, ServletException, ServletRequest, ServletResponse}
import org.apereo.cas.client.session.SingleSignOutFilter
import org.slf4j.LoggerFactory
import java.io.IOException

class LoggingFilter(singleSignOutFilter: SingleSignOutFilter) extends Filter {
  private val logger = LoggerFactory.getLogger(getClass)

  override def init(filterConfig: FilterConfig): Unit = {
    logger.info("LoggingFilter initialized")
    singleSignOutFilter.init(filterConfig)
  }

  @throws[IOException]
  @throws[ServletException]
  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain): Unit = {
    logger.info(s"SingleSignOutFilter triggered for request: ${request}")
    singleSignOutFilter.doFilter(request, response, chain)
  }

  override def destroy(): Unit = {
    logger.info("LoggingFilter destroyed")
    singleSignOutFilter.destroy()
  }
}
