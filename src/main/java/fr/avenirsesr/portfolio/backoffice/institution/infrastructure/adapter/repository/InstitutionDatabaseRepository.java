package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.mapper.InstitutionMapper;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionEntity;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.specification.InstitutionSpecification;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.repository.GenericJpaRepositoryAdapter;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class InstitutionDatabaseRepository
    extends GenericJpaRepositoryAdapter<Institution, InstitutionEntity>
    implements InstitutionRepository {
  private final InstitutionJpaRepository jpaRepository;

  public InstitutionDatabaseRepository(InstitutionJpaRepository jpaRepository) {
    super(jpaRepository, jpaRepository, InstitutionEntity.class, InstitutionMapper.INSTANCE);
    this.jpaRepository = jpaRepository;
  }

  @Override
  public boolean existsByHai(String hai) {
    return jpaRepository.existsByHai(hai);
  }

  @Override
  public Optional<Institution> findByHai(String hai) {
    return jpaRepository.findByHai(hai).map(InstitutionMapper.INSTANCE::toDomain);
  }

  @Override
  public List<Institution> findAll(UUID parentId, EInstitutionType type) {
    Specification<InstitutionEntity> specification =
        Specification.where(InstitutionSpecification.withParentId(parentId))
            .and(InstitutionSpecification.withType(type));
    return findAll(specification);
  }
}
