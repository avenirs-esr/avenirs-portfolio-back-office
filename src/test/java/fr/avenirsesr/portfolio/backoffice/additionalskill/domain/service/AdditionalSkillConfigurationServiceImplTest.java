package fr.avenirsesr.portfolio.backoffice.additionalskill.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.model.AdditionalSkillConfiguration;
import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.model.AdditionalSkillLevel;
import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.model.EAdditionalSkillConfiguration;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.Configuration;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationScope;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.ConfigurationTranslationService;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.output.repository.ConfigurationRepository;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdditionalSkillConfigurationServiceImplTest {

  @Mock private ConfigurationRepository configurationRepository;
  @Mock private ConfigurationTranslationService configurationTranslationService;

  @InjectMocks private AdditionalSkillConfigurationServiceImpl service;

  @BeforeEach
  void setUp() {
    List<Configuration> mockConfigurations =
        List.of(
            Configuration.create(
                UUID.randomUUID(),
                EConfigurationScope.ADDITIONAL_SKILL,
                EAdditionalSkillConfiguration.LEVEL_BEGINNER_LABEL,
                "Débutant"),
            Configuration.create(
                UUID.randomUUID(),
                EConfigurationScope.ADDITIONAL_SKILL,
                EAdditionalSkillConfiguration.LEVEL_BEGINNER_DESCRIPTION,
                "Description débutant"),
            Configuration.create(
                UUID.randomUUID(),
                EConfigurationScope.ADDITIONAL_SKILL,
                EAdditionalSkillConfiguration.LEVEL_INTERMEDIATE_LABEL,
                "Intermédiaire"),
            Configuration.create(
                UUID.randomUUID(),
                EConfigurationScope.ADDITIONAL_SKILL,
                EAdditionalSkillConfiguration.LEVEL_INTERMEDIATE_DESCRIPTION,
                "Description intermédiaire"),
            Configuration.create(
                UUID.randomUUID(),
                EConfigurationScope.ADDITIONAL_SKILL,
                EAdditionalSkillConfiguration.LEVEL_COMPETENT_LABEL,
                "Compétent"),
            Configuration.create(
                UUID.randomUUID(),
                EConfigurationScope.ADDITIONAL_SKILL,
                EAdditionalSkillConfiguration.LEVEL_COMPETENT_DESCRIPTION,
                "Description compétent"),
            Configuration.create(
                UUID.randomUUID(),
                EConfigurationScope.ADDITIONAL_SKILL,
                EAdditionalSkillConfiguration.LEVEL_ADVANCED_LABEL,
                "Avancé"),
            Configuration.create(
                UUID.randomUUID(),
                EConfigurationScope.ADDITIONAL_SKILL,
                EAdditionalSkillConfiguration.LEVEL_ADVANCED_DESCRIPTION,
                "Description avancé"),
            Configuration.create(
                UUID.randomUUID(),
                EConfigurationScope.ADDITIONAL_SKILL,
                EAdditionalSkillConfiguration.LEVEL_EXPERT_LABEL,
                "Expert"),
            Configuration.create(
                UUID.randomUUID(),
                EConfigurationScope.ADDITIONAL_SKILL,
                EAdditionalSkillConfiguration.LEVEL_EXPERT_DESCRIPTION,
                "Description expert"));

    lenient()
        .when(configurationRepository.inScope(EConfigurationScope.ADDITIONAL_SKILL))
        .thenReturn(mockConfigurations);
  }

  @Test
  void shouldReturnConfigurationFromRepository() {
    BddLogger.given("an AdditionalSkillConfigurationServiceImpl service");
    BddLogger.when("getting the AdditionalSkillConfiguration");
    AdditionalSkillConfiguration configuration = service.getConfiguration();

    BddLogger.then("it should return the correct configuration from repository");
    assertNotNull(configuration);
    assertEquals("Débutant", configuration.BEGINNER().label());
    assertEquals("Description débutant", configuration.BEGINNER().description());
    assertEquals("Intermédiaire", configuration.INTERMEDIATE().label());
    assertEquals("Expert", configuration.EXPERT().label());
    verify(configurationRepository).inScope(EConfigurationScope.ADDITIONAL_SKILL);
  }

  @Test
  void shouldSaveNewConfigurationValues() {
    BddLogger.given("an AdditionalSkillConfigurationServiceImpl service");
    AdditionalSkillConfiguration newConfig =
        new AdditionalSkillConfiguration(
            new AdditionalSkillLevel("New Débutant", "New Desc Débutant"),
            new AdditionalSkillLevel("New Intermédiaire", "New Desc Intermédiaire"),
            new AdditionalSkillLevel("New Compétent", "New Desc Compétent"),
            new AdditionalSkillLevel("New Avancé", "New Desc Avancé"),
            new AdditionalSkillLevel("New Expert", "New Desc Expert"));

    BddLogger.when("posting a new AdditionalSkillConfiguration");
    service.postConfiguration(Map.of(ELanguage.FRENCH, newConfig));

    BddLogger.then("it should save the new configuration values");
    verify(configurationTranslationService)
        .buildAndSaveTranslatedEntities(anyMap(), eq(EConfigurationScope.ADDITIONAL_SKILL));
  }

  @Test
  void shouldUpdateExistingConfigurationValues() {
    BddLogger.given("an AdditionalSkillConfigurationServiceImpl service");
    AdditionalSkillConfiguration updatedConfig =
        new AdditionalSkillConfiguration(
            new AdditionalSkillLevel("Débutant modifié", "Description débutant modifiée"),
            new AdditionalSkillLevel("Intermédiaire modifié", "Description intermédiaire modifiée"),
            new AdditionalSkillLevel("Compétent modifié", "Description compétent modifiée"),
            new AdditionalSkillLevel("Avancé modifié", "Description avancé modifiée"),
            new AdditionalSkillLevel("Expert modifié", "Description expert modifiée"));

    BddLogger.when("posting existing AdditionalSkillConfiguration values");
    service.postConfiguration(Map.of(ELanguage.FRENCH, updatedConfig));

    BddLogger.then("it should update the new configuration values");
    verify(configurationTranslationService)
        .buildAndSaveTranslatedEntities(anyMap(), eq(EConfigurationScope.ADDITIONAL_SKILL));
  }
}
