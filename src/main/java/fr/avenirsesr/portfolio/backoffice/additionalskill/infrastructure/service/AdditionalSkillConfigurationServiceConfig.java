package fr.avenirsesr.portfolio.backoffice.additionalskill.infrastructure.service;

import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.port.input.AdditionalSkillConfigurationService;
import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.service.AdditionalSkillConfigurationServiceImpl;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.ConfigurationTranslationService;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.output.repository.ConfigurationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AdditionalSkillConfigurationServiceConfig {
  private final ConfigurationRepository configurationRepository;
  private final ConfigurationTranslationService configurationTranslationService;

  public AdditionalSkillConfigurationServiceConfig(
      ConfigurationRepository configurationRepository,
      ConfigurationTranslationService configurationTranslationService) {
    this.configurationRepository = configurationRepository;
    this.configurationTranslationService = configurationTranslationService;
  }

  @Bean
  public AdditionalSkillConfigurationService additionalSkillConfigurationService() {
    return new AdditionalSkillConfigurationServiceImpl(
        configurationRepository, configurationTranslationService);
  }
}
