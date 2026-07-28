package fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository;

import fr.avenirsesr.portfolio.common.data.domain.port.output.repository.GenericRepositoryPort;
import fr.avenirsesr.portfolio.common.group.domain.model.Group;
import java.util.Optional;

public interface GroupRepository extends GenericRepositoryPort<Group> {
  boolean existsByIdSiSco(String idSiSco);

  Optional<Group> findByIdSiSco(String idSiSco);
}
