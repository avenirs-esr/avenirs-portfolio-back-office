package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.mapper.ExternalUserMapper;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.model.ExternalUserEntity;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.specification.ExternalUserSpecification;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.repository.GenericJpaRepositoryAdapter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ExternalUserDatabaseRepository
    extends GenericJpaRepositoryAdapter<ExternalUser, ExternalUserEntity>
    implements ExternalUserRepository {

  private final ExternalUserJpaRepository jpaRepository;

  public ExternalUserDatabaseRepository(ExternalUserJpaRepository jpaRepository) {
    super(jpaRepository, jpaRepository, ExternalUserEntity.class, ExternalUserMapper.INSTANCE);
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<ExternalUser> findByEppn(String eppn) {
    return Optional.ofNullable(jpaRepository.findByEppn(eppn))
        .map(ExternalUserMapper.INSTANCE::toDomain);
  }

  @Override
  public int countAll() {
    return jpaRepository.findAll().size();
  }

  @Override
  public List<ExternalUser> findAll(UUID institutionId, UUID groupId) {
    Specification<ExternalUserEntity> specification =
        Specification.where(ExternalUserSpecification.withInstitutionId(institutionId))
            .and(ExternalUserSpecification.withGroupId(groupId));
    return findAll(specification);
  }
}
