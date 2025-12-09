package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.configuration;

import fr.avenirsesr.portfolio.common.user.domain.port.output.BaseUserService;
import fr.avenirsesr.portfolio.common.user.infrastructure.service.NoOpUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class UserServiceConfig {
  @Bean
  public BaseUserService baseUserService() {
    log.warn("Using NoOpUserService - user management is disabled for this application");
    return new NoOpUserService();
  }
}
