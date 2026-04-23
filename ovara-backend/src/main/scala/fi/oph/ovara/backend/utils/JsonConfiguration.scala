package fi.oph.ovara.backend.utils

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration
class JsonConfiguration {

  @Bean
  def objectMapper(): ObjectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper.registerModule(new Jdk8Module())
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    mapper.registerModule(new JavaTimeModule())
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.configure(SerializationFeature.INDENT_OUTPUT, true)
    mapper
  }
}
