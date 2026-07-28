package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.model.GroupEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GroupJpaRepository
    extends JpaRepository<GroupEntity, UUID>, JpaSpecificationExecutor<GroupEntity> {
  boolean existsByIdSiSco(String idSiSco);

  Optional<GroupEntity> findByIdSiSco(String idSiSco);
}
