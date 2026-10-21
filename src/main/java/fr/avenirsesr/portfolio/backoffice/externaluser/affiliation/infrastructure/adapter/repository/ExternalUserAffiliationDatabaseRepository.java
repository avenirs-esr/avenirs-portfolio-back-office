package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.output.repository.ExternalUserAffiliationRepository;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.mapper.ExternalUserAffiliationMapper;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.model.ExternalUserAffiliationEntity;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.specification.ExternalUserAffiliationSpecification;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.repository.GenericJpaRepositoryAdapter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class ExternalUserAffiliationDatabaseRepository
    extends GenericJpaRepositoryAdapter<ExternalUserAffiliation, ExternalUserAffiliationEntity>
    implements ExternalUserAffiliationRepository {
  private final ExternalUserAffiliationJpaRepository jpaRepository;

  public ExternalUserAffiliationDatabaseRepository(
      ExternalUserAffiliationJpaRepository jpaRepository) {
    super(
        jpaRepository,
        jpaRepository,
        ExternalUserAffiliationEntity.class,
        ExternalUserAffiliationMapper.INSTANCE);
    this.jpaRepository = jpaRepository;
  }

  @Override
  public PagedResult<ExternalUserAffiliation> findAll(
      String keyword,
      List<EUserCategory> categories,
      List<UUID> institutionIds,
      List<UUID> groupIds,
      PageCriteria pageCriteria) {
    Specification<ExternalUserAffiliationEntity> specification =
        Specification.where(
                ExternalUserAffiliationSpecification.withExternalUserNameContaining(keyword))
            .and(ExternalUserAffiliationSpecification.withCategories(categories))
            .and(ExternalUserAffiliationSpecification.withInstitutionIds(institutionIds))
            .and(ExternalUserAffiliationSpecification.withGroupIds(groupIds));
    PageRequest pageRequest =
        PageRequest.of(
            pageCriteria.page(),
            pageCriteria.pageSize(),
            Sort.by(
                Sort.Order.asc("externalUser.lastName").ignoreCase(),
                Sort.Order.asc("externalUser.firstName").ignoreCase()));

    return findAll(specification, pageRequest);
  }

  @Override
  public List<UUID> findDistinctInstitutionIds(UUID externalUserId, EUserCategory category) {
    return jpaRepository.findDistinctInstitutionIds(externalUserId, category);
  }

  @Override
  public List<UUID> findDistinctGroupIds(UUID externalUserId, EUserCategory category) {
    return jpaRepository.findDistinctGroupIds(externalUserId, category);
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
