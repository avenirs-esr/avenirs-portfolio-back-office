package fr.avenirsesr.portfolio.backoffice.websitecontent.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import fr.avenirsesr.portfolio.backoffice.shared.domain.model.Configuration;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationScope;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.ConfigurationTranslationService;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.output.repository.ConfigurationRepository;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.output.serializer.ConfigurationValueSerializer;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.serializer.JacksonConfigurationValueSerializer;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.AlertConfiguration;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.BuildLifeProjectConfiguration;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.EWebsiteContentConfiguration;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebsiteContentConfigurationServiceImplTest {

  @Mock private ConfigurationTranslationService configurationTranslationService;
  @Mock private ConfigurationRepository configurationRepository;

  @Spy
  private ConfigurationValueSerializer configurationValueSerializer =
      new JacksonConfigurationValueSerializer();

  @InjectMocks private WebsiteContentConfigurationServiceImpl service;

  @Test
  void shouldReturnConfigurationByLanguage() {
    BddLogger.given("a WebsiteContentConfigurationServiceImpl service");
    Configuration configFr =
        Configuration.create(
            UUID.randomUUID(),
            EConfigurationScope.WEBSITE_CONTENT,
            EWebsiteContentConfiguration.BUILD_LIFE_PROJECT_CONTENT,
            "<p>Projet de vie</p>");

    Configuration configEn =
        Configuration.create(
            UUID.randomUUID(),
            EConfigurationScope.WEBSITE_CONTENT,
            EWebsiteContentConfiguration.BUILD_LIFE_PROJECT_CONTENT,
            "<p>Life project</p>");

    when(configurationTranslationService.findInScopeWithAllTranslations(
            EConfigurationScope.WEBSITE_CONTENT))
        .thenReturn(
            Map.of(
                ELanguage.FRENCH, List.of(configFr),
                ELanguage.ENGLISH, List.of(configEn)));

    BddLogger.when("getting life project configuration with all translations");
    Map<ELanguage, BuildLifeProjectConfiguration> result =
        service.getLifeProjectConfigurationWithAllTranslations();

    BddLogger.then("it should return configuration by language");
    assertNotNull(result);
    assertEquals("<p>Projet de vie</p>", result.get(ELanguage.FRENCH).html());
    assertEquals("<p>Life project</p>", result.get(ELanguage.ENGLISH).html());
    verify(configurationTranslationService)
        .findInScopeWithAllTranslations(EConfigurationScope.WEBSITE_CONTENT);
  }

  @Test
  void shouldSaveNewConfigurations() {
    BddLogger.given("a WebsiteContentConfigurationServiceImpl service");
    BuildLifeProjectConfiguration config =
        new BuildLifeProjectConfiguration("<p>Nouveau contenu</p>");

    when(configurationTranslationService.findInScopeWithAllTranslations(
            EConfigurationScope.WEBSITE_CONTENT))
        .thenReturn(Map.of(ELanguage.FRENCH, List.of()));

    BddLogger.when("posting life project configuration");
    service.postLifeProjectConfiguration(Map.of(ELanguage.FRENCH, config));

    BddLogger.then("it should save new configurations");
    verify(configurationTranslationService)
        .buildAndSaveTranslatedEntities(anyMap(), eq(EConfigurationScope.WEBSITE_CONTENT));
  }

  @Test
  void shouldUpdateExistingConfiguration() {
    BddLogger.given("a WebsiteContentConfigurationServiceImpl service");
    Configuration existing =
        Configuration.create(
            UUID.randomUUID(),
            EConfigurationScope.WEBSITE_CONTENT,
            EWebsiteContentConfiguration.BUILD_LIFE_PROJECT_CONTENT,
            "<p>Ancien contenu</p>");

    BuildLifeProjectConfiguration updated =
        new BuildLifeProjectConfiguration("<p>Contenu mis à jour</p>");

    when(configurationTranslationService.findInScopeWithAllTranslations(
            EConfigurationScope.WEBSITE_CONTENT))
        .thenReturn(Map.of(ELanguage.FRENCH, List.of(existing)));

    BddLogger.when("posting existing life project configuration with new values");
    service.postLifeProjectConfiguration(Map.of(ELanguage.FRENCH, updated));

    BddLogger.then("it should update the existing configuration");
    verify(configurationTranslationService)
        .buildAndSaveTranslatedEntities(anyMap(), eq(EConfigurationScope.WEBSITE_CONTENT));
    assertEquals("<p>Contenu mis à jour</p>", existing.getValue());
  }

  private static Configuration alertConfiguration(String json) {
    return Configuration.create(
        UUID.randomUUID(),
        EConfigurationScope.WEBSITE_CONTENT,
        EWebsiteContentConfiguration.ALERT,
        json);
  }

  @Test
  void shouldCreateAlertWhenNoneIsConfigured() {
    BddLogger.given("no alert configured yet");
    when(configurationRepository.inScope(EConfigurationScope.WEBSITE_CONTENT))
        .thenReturn(List.of());
    when(configurationRepository.save(any(Configuration.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    var alert =
        new AlertConfiguration(
            "Maintenance",
            null,
            Instant.parse("2026-11-10T08:00:00Z"),
            Instant.parse("2026-11-10T12:00:00Z"));

    BddLogger.when("putting an alert");
    AlertConfiguration result = service.putAlertConfiguration(alert);

    BddLogger.then("a new ALERT configuration holding the alert as JSON is saved and returned");
    var captor = ArgumentCaptor.forClass(Configuration.class);
    verify(configurationRepository).save(captor.capture());
    Configuration saved = captor.getValue();
    assertEquals(EConfigurationScope.WEBSITE_CONTENT, saved.getScope());
    assertEquals(EWebsiteContentConfiguration.ALERT, saved.getKey());
    assertEquals(
        "{\"message\":\"Maintenance\",\"active\":null,"
            + "\"startDate\":\"2026-11-10T08:00:00Z\",\"endDate\":\"2026-11-10T12:00:00Z\"}",
        saved.getValue());
    assertEquals(alert, result);
  }

  @Test
  void shouldReplaceExistingAlert() {
    BddLogger.given("an alert already configured");
    Configuration existing = alertConfiguration("{\"message\":\"Old\",\"active\":false}");
    when(configurationRepository.inScope(EConfigurationScope.WEBSITE_CONTENT))
        .thenReturn(List.of(existing));
    when(configurationRepository.save(existing)).thenReturn(existing);
    var alert = new AlertConfiguration("New", true, null, null);

    BddLogger.when("putting a new alert");
    AlertConfiguration result = service.putAlertConfiguration(alert);

    BddLogger.then("the existing configuration is updated and the new alert returned");
    verify(configurationRepository).save(existing);
    assertEquals(
        alert,
        configurationValueSerializer.deserialize(existing.getValue(), AlertConfiguration.class));
    assertEquals(alert, result);
  }

  @Test
  void shouldReturnNoAlertWhenNoneIsConfigured() {
    BddLogger.given("website content configurations without alert");
    when(configurationRepository.inScope(EConfigurationScope.WEBSITE_CONTENT))
        .thenReturn(
            List.of(
                Configuration.create(
                    UUID.randomUUID(),
                    EConfigurationScope.WEBSITE_CONTENT,
                    EWebsiteContentConfiguration.BUILD_LIFE_PROJECT_CONTENT,
                    "<p>Projet de vie</p>")));

    BddLogger.when("getting the alert configuration");
    var result = service.getAlertConfiguration();

    BddLogger.then("no alert is returned");
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnStoredAlert() {
    BddLogger.given("an alert stored as JSON");
    when(configurationRepository.inScope(EConfigurationScope.WEBSITE_CONTENT))
        .thenReturn(
            List.of(
                alertConfiguration(
                    "{\"message\":\"Maintenance\",\"active\":null,"
                        + "\"startDate\":\"2026-11-10T08:00:00Z\","
                        + "\"endDate\":\"2026-11-10T12:00:00Z\"}")));

    BddLogger.when("getting the alert configuration");
    var result = service.getAlertConfiguration();

    BddLogger.then("the stored alert is returned");
    assertEquals(
        Optional.of(
            new AlertConfiguration(
                "Maintenance",
                null,
                Instant.parse("2026-11-10T08:00:00Z"),
                Instant.parse("2026-11-10T12:00:00Z"))),
        result);
  }

  @Test
  void shouldReturnMessageOfDisplayedAlert() {
    BddLogger.given("an alert scheduled around now");
    var now = Instant.now();
    var json =
        configurationValueSerializer.serialize(
            new AlertConfiguration(
                "Maintenance", null, now.minusSeconds(3600), now.plusSeconds(3600)));
    when(configurationRepository.inScope(EConfigurationScope.WEBSITE_CONTENT))
        .thenReturn(List.of(alertConfiguration(json)));

    BddLogger.when("getting the displayed alert message");
    var result = service.getDisplayedAlertMessage();

    BddLogger.then("its message is returned");
    assertEquals(Optional.of("Maintenance"), result);
  }

  @Test
  void shouldReturnNoMessageWhenAlertIsOver() {
    BddLogger.given("an alert whose scheduled period is over");
    var now = Instant.now();
    var json =
        configurationValueSerializer.serialize(
            new AlertConfiguration(
                "Maintenance", null, now.minusSeconds(7200), now.minusSeconds(3600)));
    when(configurationRepository.inScope(EConfigurationScope.WEBSITE_CONTENT))
        .thenReturn(List.of(alertConfiguration(json)));

    BddLogger.when("getting the displayed alert message");
    var result = service.getDisplayedAlertMessage();

    BddLogger.then("no message is returned");
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnNoMessageWhenNoAlertIsConfigured() {
    BddLogger.given("no alert configured");
    when(configurationRepository.inScope(EConfigurationScope.WEBSITE_CONTENT))
        .thenReturn(List.of());

    BddLogger.when("getting the displayed alert message");
    var result = service.getDisplayedAlertMessage();

    BddLogger.then("no message is returned");
    assertTrue(result.isEmpty());
  }
}
