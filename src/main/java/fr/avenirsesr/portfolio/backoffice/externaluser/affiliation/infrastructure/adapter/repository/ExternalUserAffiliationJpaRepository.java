package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.model.ExternalUserAffiliationEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExternalUserAffiliationJpaRepository
    extends JpaRepository<ExternalUserAffiliationEntity, UUID>,
        JpaSpecificationExecutor<ExternalUserAffiliationEntity> {}
