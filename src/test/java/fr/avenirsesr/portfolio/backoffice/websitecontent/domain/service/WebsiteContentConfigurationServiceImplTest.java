package fr.avenirsesr.portfolio.backoffice.websitecontent.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import fr.avenirsesr.portfolio.backoffice.shared.domain.model.Configuration;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationScope;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.ConfigurationTranslationService;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.BuildLifeProjectConfiguration;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.EWebsiteContentConfiguration;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebsiteContentConfigurationServiceImplTest {

  @Mock private ConfigurationTranslationService configurationTranslationService;

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
}
