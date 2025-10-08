package fr.avenirsesr.portfolio.backoffice.trace.infrastructure.service;

import fr.avenirsesr.portfolio.backoffice.shared.domain.port.output.repository.ConfigurationRepository;
import fr.avenirsesr.portfolio.backoffice.trace.domain.port.input.TraceConfigurationService;
import fr.avenirsesr.portfolio.backoffice.trace.domain.service.TraceConfigurationServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class TraceConfigurationServiceConfig {

  private final ConfigurationRepository configurationRepository;

  public TraceConfigurationServiceConfig(ConfigurationRepository configurationRepository) {
    this.configurationRepository = configurationRepository;
  }

  @Bean
  public TraceConfigurationService traceConfigurationService() {
    return new TraceConfigurationServiceImpl(configurationRepository);
  }
}
