package fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.common.data.domain.port.output.repository.GenericRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExternalUserRepository extends GenericRepositoryPort<ExternalUser> {
  Optional<ExternalUser> findById(UUID id);

  Optional<ExternalUser> findByEppn(String eppn);

  List<ExternalUser> findAll();

  int countAll();
}
