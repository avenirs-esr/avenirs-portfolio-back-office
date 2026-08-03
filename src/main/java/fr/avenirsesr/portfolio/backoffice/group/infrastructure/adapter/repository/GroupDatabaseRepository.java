package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.enums.EGroupType;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.mapper.GroupMapper;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.model.GroupEntity;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.specification.GroupSpecification;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.repository.GenericJpaRepositoryAdapter;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
  public boolean existsByIdSiSco(String idSiSco) {
    return jpaRepository.existsByIdSiSco(idSiSco);
  }

  @Override
  public Optional<Group> findByIdSiSco(String idSiSco) {
    return jpaRepository.findByIdSiSco(idSiSco).map(GroupMapper.INSTANCE::toDomain);
  }

  @Override
  public List<Group> findAll(
      UUID institutionId, UUID parentId, EGroupType type, LocalDate startDate, LocalDate endDate) {
    Specification<GroupEntity> specification =
        Specification.where(GroupSpecification.withInstitutionId(institutionId))
            .and(GroupSpecification.withParentId(parentId))
            .and(GroupSpecification.withType(type))
            .and(GroupSpecification.withStartDate(startDate))
            .and(GroupSpecification.withEndDate(endDate));
    return findAll(specification);
  }
}
