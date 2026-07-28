package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.mapper.GroupMapper;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.model.GroupEntity;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.repository.GenericJpaRepositoryAdapter;
import fr.avenirsesr.portfolio.common.group.domain.model.Group;
import java.util.Optional;
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
}
