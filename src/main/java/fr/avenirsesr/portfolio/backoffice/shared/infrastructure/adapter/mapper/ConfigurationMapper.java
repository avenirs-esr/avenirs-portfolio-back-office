package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.model.EAdditionalSkillConfiguration;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.Configuration;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.model.ConfigurationEntity;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.EWebsiteContentConfiguration;
import fr.avenirsesr.portfolio.common.configuration.domain.model.ETraceConfiguration;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import fr.avenirsesr.portfolio.common.language.infrastructure.adapter.utils.TranslationUtil;

public interface ConfigurationMapper {
  static ConfigurationEntity fromDomain(Configuration configuration) {
    return ConfigurationEntity.of(
        configuration.getId(),
        configuration.getScope(),
        configuration.getKey(),
        configuration.getValue());
  }

  static ConfigurationEntity fromDomainWithoutValue(Configuration configuration) {
    return ConfigurationEntity.of(
        configuration.getId(), configuration.getScope(), configuration.getKey(), null);
  }

  static Configuration toDomain(ConfigurationEntity configurationEntity) {
    return toDomain(configurationEntity, null);
  }

  static Configuration toDomain(ConfigurationEntity configurationEntity, ELanguage language) {
    var translatedValue =
        configurationEntity.getValue().isPresent()
            ? configurationEntity.getValue().get()
            : language == null
                ? TranslationUtil.getTranslation(configurationEntity.getTranslations()).getValue()
                : TranslationUtil.getTranslation(configurationEntity.getTranslations(), language)
                    .getValue();

    return Configuration.toDomain(
        configurationEntity.getId(),
        configurationEntity.getScope(),
        switch (configurationEntity.getScope()) {
          case TRACE -> ETraceConfiguration.valueOf(configurationEntity.getKey());
          case ADDITIONAL_SKILL ->
              EAdditionalSkillConfiguration.valueOf(configurationEntity.getKey());
          case WEBSITE_CONTENT ->
              EWebsiteContentConfiguration.valueOf(configurationEntity.getKey());
        },
        translatedValue,
        configurationEntity.getCreatedAt(),
        configurationEntity.getUpdatedAt());
  }
}
