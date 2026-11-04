package fr.avenirsesr.portfolio.backoffice.websitecontent.infrastructure.service;

import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.ConfigurationTranslationService;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.output.repository.ConfigurationRepository;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.output.serializer.ConfigurationValueSerializer;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.port.input.WebsiteContentConfigurationService;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.service.WebsiteContentConfigurationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebsiteContentConfigurationServiceConfig {
  private final ConfigurationRepository configurationRepository;
  private final ConfigurationTranslationService configurationTranslationService;
  private final ConfigurationValueSerializer configurationValueSerializer;

  public WebsiteContentConfigurationServiceConfig(
      ConfigurationRepository configurationRepository,
      ConfigurationTranslationService configurationTranslationService,
      ConfigurationValueSerializer configurationValueSerializer) {
    this.configurationRepository = configurationRepository;
    this.configurationTranslationService = configurationTranslationService;
    this.configurationValueSerializer = configurationValueSerializer;
  }

  @Bean
  public WebsiteContentConfigurationService websiteContentConfigurationService() {
    return new WebsiteContentConfigurationServiceImpl(
        configurationRepository, configurationTranslationService, configurationValueSerializer);
  }
}
