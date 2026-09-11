package fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository;

import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.common.data.domain.port.output.repository.GenericRepositoryPort;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupRepository extends GenericRepositoryPort<Group> {
  boolean existsByIdSiSco(String idSiSco);

  Optional<Group> findByIdSiSco(String idSiSco);

  List<Group> findAll(
      UUID institutionId, UUID parentId, EGroupType type, LocalDate startDate, LocalDate endDate);
}
