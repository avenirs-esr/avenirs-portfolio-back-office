package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.shared.domain.model.Configuration;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationScope;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.ConfigurationTranslationService;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.mapper.ConfigurationMapper;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.model.ConfigurationEntity;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.model.ConfigurationTranslationEntity;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.repository.ConfigurationDatabaseRepository;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ConfigurationTranslationServiceImpl implements ConfigurationTranslationService {
  private final ConfigurationDatabaseRepository configurationRepository;

  @Override
  public Map<ELanguage, List<Configuration>> findInScopeWithAllTranslations(
      EConfigurationScope scope) {
    return configurationRepository.inScopeEntities(scope).stream()
        .flatMap(
            entity ->
                entity.getTranslations().stream()
                    .map(
                        translation ->
                            Map.entry(
                                translation.getLanguage(),
                                ConfigurationMapper.INSTANCE.toTranslatedDomain(
                                    entity, translation.getLanguage()))))
        .collect(
            Collectors.groupingBy(
                Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
  }

  @Override
  public void buildAndSaveTranslatedEntities(
      Map<ELanguage, List<Configuration>> domains, EConfigurationScope scope) {
    var entities = configurationRepository.inScopeEntities(scope);

    List<ConfigurationEntity> configurationsToSave =
        domains.get(ELanguage.FRENCH).stream()
            .map(
                configuration -> {
                  var entity =
                      entities.stream()
                          .filter(e -> e.getId() == configuration.getId())
                          .findAny()
                          .orElse(
                              ConfigurationMapper.INSTANCE.fromDomainWithoutValue(configuration));

                  domains
                      .keySet()
                      .forEach(
                          language ->
                              addTranslationToEntity(domains, configuration, language, entity));
                  return entity;
                })
            .toList();

    configurationRepository.saveAllEntities(configurationsToSave);
  }

  private static void addTranslationToEntity(
      Map<ELanguage, List<Configuration>> domains,
      Configuration configuration,
      ELanguage language,
      ConfigurationEntity entity) {
    var newValue =
        domains.get(language).stream()
            .filter(c -> Objects.equals(c.getKey(), configuration.getKey()))
            .findAny()
            .orElseThrow()
            .getValue();

    entity.getTranslations().stream()
        .filter(t -> t.getLanguage().equals(language))
        .findFirst()
        .ifPresentOrElse(
            translation -> translation.setValue(newValue),
            () -> {
              var newTranslation =
                  ConfigurationTranslationEntity.of(UUID.randomUUID(), language, entity, newValue);
              entity.getTranslations().add(newTranslation);
            });
  }
}
