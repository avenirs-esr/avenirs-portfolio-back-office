package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.shared.domain.model.Configuration;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationScope;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.output.repository.ConfigurationRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.mapper.ConfigurationMapper;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.model.ConfigurationEntity;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.specification.ConfigurationSpecification;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.repository.GenericJpaRepositoryAdapter;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationDatabaseRepository
    extends GenericJpaRepositoryAdapter<Configuration, ConfigurationEntity>
    implements ConfigurationRepository {
  public ConfigurationDatabaseRepository(ConfigurationJpaRepository jpaRepository) {
    super(
        jpaRepository,
        jpaRepository,
        ConfigurationMapper::fromDomain,
        ConfigurationMapper::toDomain);
  }

  @Override
  public List<Configuration> inScope(EConfigurationScope scope) {
    return jpaSpecificationExecutor.findAll(ConfigurationSpecification.inScope(scope)).stream()
        .map(ConfigurationMapper::toDomain)
        .toList();
  }

  public List<ConfigurationEntity> inScopeEntities(EConfigurationScope scope) {
    return jpaSpecificationExecutor.findAll(ConfigurationSpecification.inScope(scope));
  }

  public void saveAllEntities(List<ConfigurationEntity> entities) {
    super.saveAllEntities(entities);
  }
}
