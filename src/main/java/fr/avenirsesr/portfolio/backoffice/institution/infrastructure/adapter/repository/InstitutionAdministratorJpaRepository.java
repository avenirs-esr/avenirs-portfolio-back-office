package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionAdministratorEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InstitutionAdministratorJpaRepository
    extends JpaRepository<InstitutionAdministratorEntity, UUID>,
        JpaSpecificationExecutor<InstitutionAdministratorEntity> {}
