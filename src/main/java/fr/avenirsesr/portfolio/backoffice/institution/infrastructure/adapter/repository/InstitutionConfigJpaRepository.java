package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionConfigEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InstitutionConfigJpaRepository
    extends JpaRepository<InstitutionConfigEntity, UUID>,
        JpaSpecificationExecutor<InstitutionConfigEntity> {
  InstitutionConfigEntity findByInstitutionId(UUID institutionId);
}
