package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InstitutionJpaRepository
    extends JpaRepository<InstitutionEntity, UUID>, JpaSpecificationExecutor<InstitutionEntity> {
  boolean existsByHai(String hai);

  Optional<InstitutionEntity> findByHai(String hai);
}
