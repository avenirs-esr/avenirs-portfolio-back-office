package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.model.EAdditionalSkillConfiguration;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.Configuration;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationValueType;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.model.ConfigurationEntity;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.EWebsiteContentConfiguration;
import fr.avenirsesr.portfolio.common.configuration.domain.exception.ConfigurationException;
import fr.avenirsesr.portfolio.common.configuration.domain.model.EConfiguration;
import fr.avenirsesr.portfolio.common.configuration.domain.model.ETraceConfiguration;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.mapper.Mapper;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import fr.avenirsesr.portfolio.common.language.infrastructure.adapter.utils.TranslationUtil;

public class ConfigurationMapper implements Mapper<ConfigurationEntity, Configuration> {
  public static final ConfigurationMapper INSTANCE = new ConfigurationMapper();

  private static final ObjectMapper JSON = new ObjectMapper();

  @Override
  public ConfigurationEntity fromDomain(Configuration configuration) {
    return ConfigurationEntity.of(
        configuration.getId(),
        configuration.getScope(),
        configuration.getKey(),
        toStoredValue(configuration.getKey(), configuration.getValue()));
  }

  public ConfigurationEntity fromDomainWithoutValue(Configuration configuration) {
    return ConfigurationEntity.of(
        configuration.getId(), configuration.getScope(), configuration.getKey(), null);
  }

  @Override
  public Configuration toDomain(ConfigurationEntity configurationEntity) {
    return toTranslatedDomain(configurationEntity, null);
  }

  public Configuration toTranslatedDomain(
      ConfigurationEntity configurationEntity, ELanguage language) {
    EConfiguration key =
        switch (configurationEntity.getScope()) {
          case TRACE -> ETraceConfiguration.valueOf(configurationEntity.getKey());
          case ADDITIONAL_SKILL ->
              EAdditionalSkillConfiguration.valueOf(configurationEntity.getKey());
          case WEBSITE_CONTENT ->
              EWebsiteContentConfiguration.valueOf(configurationEntity.getKey());
        };

    var translatedValue =
        configurationEntity.getValue().isPresent()
            ? fromStoredValue(key, configurationEntity.getValue().get())
            : language == null
                ? TranslationUtil.getTranslation(configurationEntity.getTranslations()).getValue()
                : TranslationUtil.getTranslation(configurationEntity.getTranslations(), language)
                    .getValue();

    return Configuration.toDomain(
        configurationEntity.getId(),
        configurationEntity.getScope(),
        key,
        translatedValue,
        configurationEntity.getCreatedAt(),
        configurationEntity.getUpdatedAt());
  }

  static String toStoredValue(EConfiguration key, String value) {
    if (value == null || EConfigurationValueType.of(key) == EConfigurationValueType.JSON) {
      return value;
    }
    try {
      return JSON.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new ConfigurationException(
          "Unable to store configuration " + key.name() + ": " + e.getOriginalMessage());
    }
  }

  static String fromStoredValue(EConfiguration key, String storedValue) {
    if (EConfigurationValueType.of(key) == EConfigurationValueType.JSON) {
      return storedValue;
    }
    try {
      JsonNode node = JSON.readTree(storedValue);
      return node.isTextual() ? node.textValue() : storedValue;
    } catch (JsonProcessingException e) {
      throw new ConfigurationException(
          "Configuration " + key.name() + " does not hold valid JSON: " + e.getOriginalMessage());
    }
  }
}
