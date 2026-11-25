package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.repository;

import static fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.specification.InstitutionAdministratorSpecification.*;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionAdministrator;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionAdministratorRepository;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.mapper.InstitutionAdministratorMapper;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionAdministratorEntity;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.repository.GenericJpaRepositoryAdapter;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class InstitutionAdministratorDatabaseRepository
    extends GenericJpaRepositoryAdapter<InstitutionAdministrator, InstitutionAdministratorEntity>
    implements InstitutionAdministratorRepository {

  private final InstitutionAdministratorJpaRepository jpaRepository;

  public InstitutionAdministratorDatabaseRepository(
      InstitutionAdministratorJpaRepository jpaRepository) {
    super(
        jpaRepository,
        jpaRepository,
        InstitutionAdministratorEntity.class,
        InstitutionAdministratorMapper.INSTANCE);
    this.jpaRepository = jpaRepository;
  }

  @Override
  public void deleteByUserAndInstitution(UUID userId, UUID institutionId) {
    jpaRepository
        .findOne(byUserAndInstitution(userId, institutionId))
        .ifPresent(jpaRepository::delete);
  }

  @Override
  @Transactional
  public boolean existsByUserAndInstitution(UUID userId, UUID institutionId) {
    return jpaRepository.exists(byUserAndInstitution(userId, institutionId));
  }

  @Override
  public List<UUID> findInstitutionIdsByUser(UUID userId) {
    return jpaRepository.findAll(hasExternalUser(userId)).stream()
        .map(entity -> entity.getInstitution().getId())
        .toList();
  }

  @Override
  public List<InstitutionAdministrator> findByInstitution(UUID institutionId) {
    return jpaRepository.findAll(hasInstitution(institutionId)).stream()
        .map(InstitutionAdministratorMapper.INSTANCE::toDomain)
        .toList();
  }
}
