package fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.model.ToolEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ToolJpaRepository
    extends JpaRepository<ToolEntity, UUID>, JpaSpecificationExecutor<ToolEntity> {}
