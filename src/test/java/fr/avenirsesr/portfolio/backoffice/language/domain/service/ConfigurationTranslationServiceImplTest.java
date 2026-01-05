package fr.avenirsesr.portfolio.backoffice.language.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.model.EAdditionalSkillConfiguration;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.Configuration;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationScope;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.mapper.ConfigurationMapper;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.model.ConfigurationEntity;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.model.ConfigurationTranslationEntity;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.repository.ConfigurationDatabaseRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.service.ConfigurationTranslationServiceImpl;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ConfigurationTranslationServiceImplTest {

  private ConfigurationDatabaseRepository repository;
  private ConfigurationTranslationServiceImpl service;

  private ConfigurationEntity entityFr;

  @BeforeEach
  void setUp() {
    repository = mock(ConfigurationDatabaseRepository.class);
    service = new ConfigurationTranslationServiceImpl(repository);

    // Entité de base avec traduction FR
    entityFr = new ConfigurationEntity();
    entityFr.setId(UUID.randomUUID());
    entityFr.setScope(EConfigurationScope.ADDITIONAL_SKILL);
    entityFr.setKey("LEVEL_BEGINNER_LABEL");
    entityFr.setValue("Débutant");

    var translations = new HashSet<ConfigurationTranslationEntity>();
    translations.add(
        ConfigurationTranslationEntity.of(
            UUID.randomUUID(), ELanguage.FRENCH, entityFr, "Débutant"));
    entityFr.setTranslations(translations);
  }

  @Test
  void shouldReturnEmptyMapWhenNoEntities() {
    BddLogger.given("a ConfigurationTranslationServiceImpl service");
    when(repository.inScopeEntities(EConfigurationScope.ADDITIONAL_SKILL)).thenReturn(List.of());

    BddLogger.when("searching in an empty list");
    var result = service.findInScopeWithAllTranslations(EConfigurationScope.ADDITIONAL_SKILL);

    BddLogger.then("it should return an empty configuration");
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnConfigurationsGroupedByLanguage() {
    BddLogger.given("a ConfigurationTranslationServiceImpl service");
    when(repository.inScopeEntities(EConfigurationScope.ADDITIONAL_SKILL))
        .thenReturn(List.of(entityFr));

    BddLogger.when("searching in a list of ConfigurationEntity");
    var result = service.findInScopeWithAllTranslations(EConfigurationScope.ADDITIONAL_SKILL);

    BddLogger.then("it should return configurations grouped by language");
    assertTrue(result.containsKey(ELanguage.FRENCH));
    assertEquals(1, result.get(ELanguage.FRENCH).size());
    assertEquals("Débutant", result.get(ELanguage.FRENCH).get(0).getValue());
  }

  @Test
  void shouldSaveNewEntityWhenNotExists() {
    BddLogger.given("a ConfigurationTranslationServiceImpl service");
    Configuration configFr =
        Configuration.create(
            UUID.randomUUID(),
            EConfigurationScope.ADDITIONAL_SKILL,
            EAdditionalSkillConfiguration.LEVEL_BEGINNER_LABEL,
            "Débutant");
    Map<ELanguage, List<Configuration>> input = Map.of(ELanguage.FRENCH, List.of(configFr));

    when(repository.inScopeEntities(EConfigurationScope.ADDITIONAL_SKILL)).thenReturn(List.of());

    BddLogger.when("building and saving non existing translated entities");
    service.buildAndSaveTranslatedEntities(input, EConfigurationScope.ADDITIONAL_SKILL);

    BddLogger.then("it should save the new entities");
    ArgumentCaptor<List<ConfigurationEntity>> captor = ArgumentCaptor.forClass(List.class);
    verify(repository).saveAllEntities(captor.capture());

    var saved = captor.getValue().get(0);
    assertEquals(EAdditionalSkillConfiguration.LEVEL_BEGINNER_LABEL.name(), saved.getKey());
    assertEquals("Débutant", saved.getTranslations().stream().findFirst().orElseThrow().getValue());
  }

  @Test
  void shouldUpdateExistingEntityValueForLanguage() {
    BddLogger.given("a ConfigurationTranslationServiceImpl service");
    Configuration configFrUpdated =
        Configuration.create(
            entityFr.getId(),
            EConfigurationScope.ADDITIONAL_SKILL,
            EAdditionalSkillConfiguration.LEVEL_BEGINNER_LABEL,
            "Débutant modifié");

    Map<ELanguage, List<Configuration>> input = Map.of(ELanguage.FRENCH, List.of(configFrUpdated));

    when(repository.inScopeEntities(EConfigurationScope.ADDITIONAL_SKILL))
        .thenReturn(List.of(entityFr));

    BddLogger.when("building and saving non existing translated entities");
    service.buildAndSaveTranslatedEntities(input, EConfigurationScope.ADDITIONAL_SKILL);

    BddLogger.then("it should update the existing entities");
    assertEquals(
        "Débutant modifié",
        entityFr.getTranslations().stream().findFirst().orElseThrow().getValue());
    verify(repository).saveAllEntities(anyList());
  }

  @Test
  void shouldAddNewTranslationForAnotherLanguage() {
    BddLogger.given("a ConfigurationTranslationServiceImpl service");
    Configuration configFr = ConfigurationMapper.INSTANCE.toDomain(entityFr);
    Configuration configEn =
        Configuration.create(
            entityFr.getId(),
            EConfigurationScope.ADDITIONAL_SKILL,
            EAdditionalSkillConfiguration.LEVEL_BEGINNER_LABEL,
            "Beginner");

    Map<ELanguage, List<Configuration>> input =
        Map.of(
            ELanguage.FRENCH, List.of(configFr),
            ELanguage.ENGLISH, List.of(configEn));

    when(repository.inScopeEntities(EConfigurationScope.ADDITIONAL_SKILL))
        .thenReturn(List.of(entityFr));

    BddLogger.when("building and saving existing translated entities for another language");
    service.buildAndSaveTranslatedEntities(input, EConfigurationScope.ADDITIONAL_SKILL);

    BddLogger.then("it should add the new translation for this language");
    assertTrue(
        entityFr.getTranslations().stream().anyMatch(t -> t.getLanguage() == ELanguage.ENGLISH));
    verify(repository).saveAllEntities(anyList());
  }

  @Test
  void shouldThrowWhenMissingTranslationForLanguage() {
    BddLogger.given("a ConfigurationTranslationServiceImpl service");
    Configuration configFr = ConfigurationMapper.INSTANCE.toDomain(entityFr);
    Map<ELanguage, List<Configuration>> input =
        Map.of(
            ELanguage.FRENCH, List.of(configFr),
            ELanguage.ENGLISH, List.of() // manquant
            );

    when(repository.inScopeEntities(EConfigurationScope.ADDITIONAL_SKILL))
        .thenReturn(List.of(entityFr));

    BddLogger.when("building and saving missing translation for a specific language");
    BddLogger.then("it should throw NoSuchElementException");
    assertThrows(
        NoSuchElementException.class,
        () -> service.buildAndSaveTranslatedEntities(input, EConfigurationScope.ADDITIONAL_SKILL));
  }
}
