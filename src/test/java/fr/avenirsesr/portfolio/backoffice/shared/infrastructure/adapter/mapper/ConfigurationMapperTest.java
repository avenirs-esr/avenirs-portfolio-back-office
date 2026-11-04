package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.mapper;

import static org.junit.jupiter.api.Assertions.*;

import fr.avenirsesr.portfolio.backoffice.shared.domain.model.Configuration;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationScope;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationValueType;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.ETypedConfiguration;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.model.ConfigurationEntity;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.model.ConfigurationTranslationEntity;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.EWebsiteContentConfiguration;
import fr.avenirsesr.portfolio.common.configuration.domain.exception.ConfigurationException;
import fr.avenirsesr.portfolio.common.configuration.domain.model.ETraceConfiguration;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConfigurationMapperTest {
  private final ConfigurationMapper mapper = ConfigurationMapper.INSTANCE;

  private enum EJsonTestConfiguration implements ETypedConfiguration {
    JSON_VALUE;

    @Override
    public EConfigurationValueType getValueType() {
      return EConfigurationValueType.JSON;
    }
  }

  @Test
  void shouldStoreTextValueAsJsonString() {
    BddLogger.given("a text configuration holding quotes");
    var configuration =
        Configuration.create(
            UUID.randomUUID(),
            EConfigurationScope.TRACE,
            ETraceConfiguration.MAX_REMINING_DAYS,
            "say \"10\"");

    BddLogger.when("mapping it to an entity");
    var entity = mapper.fromDomain(configuration);

    BddLogger.then("the value is stored as an escaped JSON string");
    assertEquals("\"say \\\"10\\\"\"", entity.getValue().orElseThrow());
  }

  @Test
  void shouldReadTextValueBackAsPlainText() {
    BddLogger.given("a text configuration stored as a JSON string");
    var entity =
        ConfigurationEntity.of(
            UUID.randomUUID(),
            EConfigurationScope.TRACE,
            ETraceConfiguration.MAX_REMINING_DAYS,
            "\"10\"");

    BddLogger.when("mapping it to the domain");
    var configuration = mapper.toDomain(entity);

    BddLogger.then("the domain gets the plain text");
    assertEquals("10", configuration.getValue());
  }

  @Test
  void shouldReadScalarSetByHandAsItsText() {
    BddLogger.given("a text configuration stored as a JSON number");
    var entity =
        ConfigurationEntity.of(
            UUID.randomUUID(),
            EConfigurationScope.TRACE,
            ETraceConfiguration.MAX_REMINING_DAYS,
            "10");

    BddLogger.when("mapping it to the domain");
    var configuration = mapper.toDomain(entity);

    BddLogger.then("the domain gets the number as text");
    assertEquals("10", configuration.getValue());
  }

  @Test
  void shouldKeepJsonValueAsIsBothWays() {
    BddLogger.given("a JSON configuration");
    var json = "{\"message\":\"Maintenance\",\"active\":true}";

    BddLogger.when("storing it and reading it back");
    var storedValue = ConfigurationMapper.toStoredValue(EJsonTestConfiguration.JSON_VALUE, json);
    var readValue =
        ConfigurationMapper.fromStoredValue(EJsonTestConfiguration.JSON_VALUE, storedValue);

    BddLogger.then("the JSON document is neither wrapped nor unwrapped");
    assertEquals(json, storedValue);
    assertEquals(json, readValue);
  }

  @Test
  void shouldMapAlertAsJsonConfiguration() {
    BddLogger.given("an alert configuration");
    var json = "{\"message\":\"Maintenance\",\"active\":true}";
    var configuration =
        Configuration.create(
            UUID.randomUUID(),
            EConfigurationScope.WEBSITE_CONTENT,
            EWebsiteContentConfiguration.ALERT,
            json);

    BddLogger.when("mapping it to an entity and back");
    var entity = mapper.fromDomain(configuration);
    var mappedBack = mapper.toDomain(entity);

    BddLogger.then("its JSON document is kept as is");
    assertEquals(json, entity.getValue().orElseThrow());
    assertEquals(json, mappedBack.getValue());
    assertEquals(EWebsiteContentConfiguration.ALERT, mappedBack.getKey());
  }

  @Test
  void shouldKeepMissingValueEmpty() {
    BddLogger.given("a configuration without value");
    var configuration =
        Configuration.create(
            UUID.randomUUID(),
            EConfigurationScope.WEBSITE_CONTENT,
            EWebsiteContentConfiguration.BUILD_LIFE_PROJECT_CONTENT,
            null);

    BddLogger.when("mapping it to an entity");
    var entity = mapper.fromDomain(configuration);

    BddLogger.then("no value is stored");
    assertTrue(entity.getValue().isEmpty());
  }

  @Test
  void shouldReadTranslatedValueAsPlainText() {
    BddLogger.given("a translated configuration, whose value lives in its translations");
    var entity =
        ConfigurationEntity.of(
            UUID.randomUUID(),
            EConfigurationScope.WEBSITE_CONTENT,
            EWebsiteContentConfiguration.BUILD_LIFE_PROJECT_CONTENT,
            null);
    entity
        .getTranslations()
        .add(
            ConfigurationTranslationEntity.of(
                UUID.randomUUID(), ELanguage.ENGLISH, entity, "<p>Life project</p>"));

    BddLogger.when("mapping it to the domain in english");
    var configuration = mapper.toTranslatedDomain(entity, ELanguage.ENGLISH);

    BddLogger.then("the translation is returned untouched");
    assertEquals("<p>Life project</p>", configuration.getValue());
  }

  @Test
  void shouldRejectInvalidJson() {
    BddLogger.given("a text configuration whose stored value is not JSON");
    var entity =
        ConfigurationEntity.of(
            UUID.randomUUID(),
            EConfigurationScope.TRACE,
            ETraceConfiguration.MAX_REMINING_DAYS,
            "not json");

    BddLogger.when("mapping it to the domain");
    BddLogger.then("a configuration error is raised");
    assertThrows(ConfigurationException.class, () -> mapper.toDomain(entity));
  }
}
