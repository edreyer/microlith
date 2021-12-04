package io.liquidsoftware.base.user.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import io.liquidsoftware.common.config.CommonConfig
import io.liquidsoftware.common.logging.LoggerDelegate

@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties
@ComponentScan(basePackages = ["io.liquidsoftware.base.user"])
@ConfigurationPropertiesScan(basePackages = ["io.liquidsoftware.base.user"])
@Import(
  CommonConfig::class
)
class UserModuleConfig {
  val logger by LoggerDelegate()

  init {
    logger.info("Starting User Module")
  }
}
