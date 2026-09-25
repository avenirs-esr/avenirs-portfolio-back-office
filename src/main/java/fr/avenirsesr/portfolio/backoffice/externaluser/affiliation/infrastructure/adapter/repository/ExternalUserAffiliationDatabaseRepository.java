package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.output.repository.ExternalUserAffiliationRepository;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.mapper.ExternalUserAffiliationMapper;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.model.ExternalUserAffiliationEntity;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.specification.ExternalUserAffiliationSpecification;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.repository.GenericJpaRepositoryAdapter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ExternalUserAffiliationDatabaseRepository
    extends GenericJpaRepositoryAdapter<ExternalUserAffiliation, ExternalUserAffiliationEntity>
    implements ExternalUserAffiliationRepository {

  public ExternalUserAffiliationDatabaseRepository(
      ExternalUserAffiliationJpaRepository jpaRepository) {
    super(
        jpaRepository,
        jpaRepository,
        ExternalUserAffiliationEntity.class,
        ExternalUserAffiliationMapper.INSTANCE);
  }

  @Override
  public List<ExternalUserAffiliation> findAllByExternalUserId(UUID externalUserId) {
    return findAll(ExternalUserAffiliationSpecification.withExternalUserId(externalUserId));
  }

  @Override
  public List<ExternalUserAffiliation> findAllByExternalUserIdAndCategory(
      UUID externalUserId, EUserCategory category) {
    Specification<ExternalUserAffiliationEntity> specification =
        Specification.where(ExternalUserAffiliationSpecification.withExternalUserId(externalUserId))
            .and(ExternalUserAffiliationSpecification.withCategory(category));

    return findAll(specification);
  }

  @Override
  public Optional<ExternalUserAffiliation>
      findByExternalUserIdAndInstitutionIdAndGroupIdAndCategory(
          UUID externalUserId, UUID institutionId, UUID groupId, EUserCategory category) {
    Specification<ExternalUserAffiliationEntity> specification =
        Specification.where(ExternalUserAffiliationSpecification.withExternalUserId(externalUserId))
            .and(ExternalUserAffiliationSpecification.withInstitutionId(institutionId))
            .and(ExternalUserAffiliationSpecification.withGroupId(groupId))
            .and(ExternalUserAffiliationSpecification.withCategory(category));

    return findAll(specification).stream().findFirst();
  }
}
