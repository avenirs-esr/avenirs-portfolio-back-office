package fr.avenirsesr.portfolio.backoffice.additionalskill.domain.service;

import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.model.AdditionalSkillConfiguration;
import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.model.AdditionalSkillLevel;
import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.model.EAdditionalSkillConfiguration;
import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.port.input.AdditionalSkillConfigurationService;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.Configuration;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationScope;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.ConfigurationTranslationService;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.output.repository.ConfigurationRepository;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class AdditionalSkillConfigurationServiceImpl
    implements AdditionalSkillConfigurationService {
  private final ConfigurationRepository configurationRepository;
  private final ConfigurationTranslationService configurationTranslationService;

  private String getAdditionalSkillLevelConfigFrom(
      List<Configuration> configurations, EAdditionalSkillConfiguration key) {
    return configurations.stream()
        .filter(c -> c.getKey() == key)
        .findFirst()
        .orElseThrow()
        .getValue();
  }

  private AdditionalSkillConfiguration buildAdditionalSkillConfiguration(
      List<Configuration> configurations) {
    return new AdditionalSkillConfiguration(
        new AdditionalSkillLevel(
            getAdditionalSkillLevelConfigFrom(
                configurations, EAdditionalSkillConfiguration.LEVEL_BEGINNER_LABEL),
            getAdditionalSkillLevelConfigFrom(
                configurations, EAdditionalSkillConfiguration.LEVEL_BEGINNER_DESCRIPTION)),
        new AdditionalSkillLevel(
            getAdditionalSkillLevelConfigFrom(
                configurations, EAdditionalSkillConfiguration.LEVEL_INTERMEDIATE_LABEL),
            getAdditionalSkillLevelConfigFrom(
                configurations, EAdditionalSkillConfiguration.LEVEL_INTERMEDIATE_DESCRIPTION)),
        new AdditionalSkillLevel(
            getAdditionalSkillLevelConfigFrom(
                configurations, EAdditionalSkillConfiguration.LEVEL_COMPETENT_LABEL),
            getAdditionalSkillLevelConfigFrom(
                configurations, EAdditionalSkillConfiguration.LEVEL_COMPETENT_DESCRIPTION)),
        new AdditionalSkillLevel(
            getAdditionalSkillLevelConfigFrom(
                configurations, EAdditionalSkillConfiguration.LEVEL_ADVANCED_LABEL),
            getAdditionalSkillLevelConfigFrom(
                configurations, EAdditionalSkillConfiguration.LEVEL_ADVANCED_DESCRIPTION)),
        new AdditionalSkillLevel(
            getAdditionalSkillLevelConfigFrom(
                configurations, EAdditionalSkillConfiguration.LEVEL_EXPERT_LABEL),
            getAdditionalSkillLevelConfigFrom(
                configurations, EAdditionalSkillConfiguration.LEVEL_EXPERT_DESCRIPTION)));
  }

  @Override
  public AdditionalSkillConfiguration getConfiguration() {
    List<Configuration> configurations =
        configurationRepository.inScope(EConfigurationScope.ADDITIONAL_SKILL);

    return buildAdditionalSkillConfiguration(configurations);
  }

  @Override
  public Map<ELanguage, AdditionalSkillConfiguration> getConfigurationWithAllTranslations() {
    Map<ELanguage, List<Configuration>> configurationsByLanguage =
        configurationTranslationService.findInScopeWithAllTranslations(
            EConfigurationScope.ADDITIONAL_SKILL);

    return configurationsByLanguage.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, entry -> buildAdditionalSkillConfiguration(entry.getValue())));
  }

  @Override
  public void postConfiguration(Map<ELanguage, AdditionalSkillConfiguration> configurations) {
    Map<ELanguage, List<Configuration>> savedConfigurations =
        configurationTranslationService.findInScopeWithAllTranslations(
            EConfigurationScope.ADDITIONAL_SKILL);

    Map<ELanguage, List<Configuration>> newTranslatedConfigurations =
        configurations.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry ->
                        updateConfigurationOfLanguage(
                            entry.getKey(), entry.getValue(), savedConfigurations)));

    configurationTranslationService.buildAndSaveTranslatedEntities(
        newTranslatedConfigurations, EConfigurationScope.ADDITIONAL_SKILL);

    log.info("Added additional skill configurations : {}", newTranslatedConfigurations);
  }

  private List<Configuration> updateConfigurationOfLanguage(
      ELanguage language,
      AdditionalSkillConfiguration translatedConfiguration,
      Map<ELanguage, List<Configuration>> savedConfigurations) {
    var newConfigurations = new ArrayList<Configuration>();
    for (EAdditionalSkillConfiguration key : EAdditionalSkillConfiguration.values()) {
      var value =
          switch (key) {
            case LEVEL_BEGINNER_LABEL -> translatedConfiguration.BEGINNER().label();
            case LEVEL_BEGINNER_DESCRIPTION -> translatedConfiguration.BEGINNER().description();
            case LEVEL_INTERMEDIATE_LABEL -> translatedConfiguration.INTERMEDIATE().label();
            case LEVEL_INTERMEDIATE_DESCRIPTION ->
                translatedConfiguration.INTERMEDIATE().description();
            case LEVEL_COMPETENT_LABEL -> translatedConfiguration.COMPETENT().label();
            case LEVEL_COMPETENT_DESCRIPTION -> translatedConfiguration.COMPETENT().description();
            case LEVEL_ADVANCED_LABEL -> translatedConfiguration.ADVANCED().label();
            case LEVEL_ADVANCED_DESCRIPTION -> translatedConfiguration.ADVANCED().description();
            case LEVEL_EXPERT_LABEL -> translatedConfiguration.EXPERT().label();
            case LEVEL_EXPERT_DESCRIPTION -> translatedConfiguration.EXPERT().description();
          };

      var newConfiguration =
          savedConfigurations.get(language) != null
                  && savedConfigurations.get(language).stream().anyMatch(c -> c.getKey() == key)
              ? savedConfigurations.get(language).stream()
                  .filter(c -> c.getKey() == key)
                  .findAny()
                  .orElseThrow()
              : Configuration.create(
                  UUID.randomUUID(), EConfigurationScope.ADDITIONAL_SKILL, key, value);

      newConfiguration.setValue(value);
      newConfigurations.add(newConfiguration);
    }
    return newConfigurations;
  }
}
