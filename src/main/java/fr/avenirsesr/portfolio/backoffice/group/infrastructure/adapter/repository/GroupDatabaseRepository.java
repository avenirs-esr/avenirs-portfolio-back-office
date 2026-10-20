package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.mapper.GroupMapper;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.model.GroupEntity;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.specification.GroupSpecification;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.repository.GenericJpaRepositoryAdapter;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class GroupDatabaseRepository extends GenericJpaRepositoryAdapter<Group, GroupEntity>
    implements GroupRepository {
  private final GroupJpaRepository jpaRepository;

  public GroupDatabaseRepository(GroupJpaRepository jpaRepository) {
    super(jpaRepository, jpaRepository, GroupEntity.class, GroupMapper.INSTANCE);
    this.jpaRepository = jpaRepository;
  }

  @Override
  public boolean existsByIdSISco(String idSISco) {
    return jpaRepository.existsByIdSISco(idSISco);
  }

  @Override
  public Optional<Group> findByIdSISco(String idSISco) {
    return jpaRepository.findByIdSISco(idSISco).map(GroupMapper.INSTANCE::toDomain);
  }

  @Override
  public PagedResult<Group> findAll(
      UUID institutionId, String keyword, List<EGroupType> types, PageCriteria pageCriteria) {
    Specification<GroupEntity> specification =
        Specification.where(GroupSpecification.withInstitutionId(institutionId))
            .and(GroupSpecification.withNameContaining(keyword))
            .and(GroupSpecification.withType(types));
    PageRequest pageRequest =
        PageRequest.of(
            pageCriteria.page(),
            pageCriteria.pageSize(),
            Sort.by(Sort.Order.asc("name").ignoreCase()));

    return findAll(specification, pageRequest);
  }

  @Override
  public List<Group> findAllByParentIds(Collection<UUID> parentIds) {
    return findAll(GroupSpecification.withParentIds(parentIds));
  }
}
