package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionConfigNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionConfig;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionConfigRepository;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.mapper.InstitutionConfigMapper;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionConfigEntity;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.repository.GenericJpaRepositoryAdapter;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class InstitutionConfigDatabaseRepository
    extends GenericJpaRepositoryAdapter<InstitutionConfig, InstitutionConfigEntity>
    implements InstitutionConfigRepository {
  private final InstitutionConfigJpaRepository jpaRepository;

  public InstitutionConfigDatabaseRepository(InstitutionConfigJpaRepository jpaRepository) {
    super(
        jpaRepository,
        jpaRepository,
        InstitutionConfigEntity.class,
        InstitutionConfigMapper.INSTANCE);
    this.jpaRepository = jpaRepository;
  }

  @Override
  public InstitutionConfig findByInstitutionId(UUID institutionId) {
    InstitutionConfigEntity institutionConfigEntity =
        jpaRepository.findByInstitutionId(institutionId);
    if (institutionConfigEntity == null) {
      throw new InstitutionConfigNotFoundException();
    }
    return InstitutionConfigMapper.INSTANCE.toDomain(institutionConfigEntity);
  }
}
