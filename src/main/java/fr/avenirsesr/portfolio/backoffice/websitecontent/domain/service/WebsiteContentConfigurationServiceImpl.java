package fr.avenirsesr.portfolio.backoffice.websitecontent.domain.service;

import fr.avenirsesr.portfolio.backoffice.shared.domain.model.Configuration;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationScope;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.ConfigurationTranslationService;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.output.repository.ConfigurationRepository;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.BuildLifeProjectConfiguration;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.EWebsiteContentConfiguration;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.port.input.WebsiteContentConfigurationService;
import fr.avenirsesr.portfolio.common.configuration.domain.exception.ConfigurationException;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class WebsiteContentConfigurationServiceImpl implements WebsiteContentConfigurationService {
  private final ConfigurationRepository configurationRepository;
  private final ConfigurationTranslationService configurationTranslationService;

  private BuildLifeProjectConfiguration mapConfigToBuildLifeProjectConfig(
      List<Configuration> configurations) {
    return new BuildLifeProjectConfiguration(
        configurations.stream()
            .filter(c -> c.getKey() == EWebsiteContentConfiguration.BUILD_LIFE_PROJECT_CONTENT)
            .findAny()
            .orElseThrow(
                () ->
                    new ConfigurationException(
                        "Missing website content configuration: BUILD_LIFE_PROJECT_CONTENT"))
            .getValue());
  }

  @Override
  public BuildLifeProjectConfiguration getLifeProjectConfiguration() {
    List<Configuration> configurations =
        configurationRepository.inScope(EConfigurationScope.WEBSITE_CONTENT);

    return new BuildLifeProjectConfiguration(
        configurations.stream()
            .filter(c -> c.getKey() == EWebsiteContentConfiguration.BUILD_LIFE_PROJECT_CONTENT)
            .findAny()
            .orElseThrow(
                () ->
                    new ConfigurationException(
                        "Missing website content configuration: BUILD_LIFE_PROJECT_CONTENT"))
            .getValue());
  }

  @Override
  public Map<ELanguage, BuildLifeProjectConfiguration>
      getLifeProjectConfigurationWithAllTranslations() {
    Map<ELanguage, List<Configuration>> configurationsByLanguage =
        configurationTranslationService.findInScopeWithAllTranslations(
            EConfigurationScope.WEBSITE_CONTENT);

    return configurationsByLanguage.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, entry -> mapConfigToBuildLifeProjectConfig(entry.getValue())));
  }

  @Override
  public void postLifeProjectConfiguration(
      Map<ELanguage, BuildLifeProjectConfiguration> configurations) {
    Map<ELanguage, List<Configuration>> savedConfigurations =
        configurationTranslationService.findInScopeWithAllTranslations(
            EConfigurationScope.WEBSITE_CONTENT);

    Map<ELanguage, List<Configuration>> newTranslatedConfigurations =
        configurations.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> {
                      var language = entry.getKey();
                      var value = entry.getValue().html();
                      var newConfiguration =
                          savedConfigurations.get(language) != null
                                  && savedConfigurations.get(language).stream()
                                      .anyMatch(
                                          c ->
                                              c.getKey()
                                                  == EWebsiteContentConfiguration
                                                      .BUILD_LIFE_PROJECT_CONTENT)
                              ? savedConfigurations.get(language).stream()
                                  .filter(
                                      c ->
                                          c.getKey()
                                              == EWebsiteContentConfiguration
                                                  .BUILD_LIFE_PROJECT_CONTENT)
                                  .findAny()
                                  .orElseThrow(
                                      () ->
                                          new ConfigurationException(
                                              "Missing website content configuration while"
                                                  + " updating: BUILD_LIFE_PROJECT_CONTENT"))
                              : Configuration.create(
                                  UUID.randomUUID(),
                                  EConfigurationScope.WEBSITE_CONTENT,
                                  EWebsiteContentConfiguration.BUILD_LIFE_PROJECT_CONTENT,
                                  value);
                      newConfiguration.setValue(value);
                      return List.of(newConfiguration);
                    }));

    configurationTranslationService.buildAndSaveTranslatedEntities(
        newTranslatedConfigurations, EConfigurationScope.WEBSITE_CONTENT);

    log.info("Added website content configurations : {}", newTranslatedConfigurations);
  }
}
