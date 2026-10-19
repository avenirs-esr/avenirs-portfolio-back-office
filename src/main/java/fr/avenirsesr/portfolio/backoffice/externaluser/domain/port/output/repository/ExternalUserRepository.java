package fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.common.data.domain.port.output.repository.GenericRepositoryPort;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ExternalUserRepository extends GenericRepositoryPort<ExternalUser> {
  Optional<ExternalUser> findById(UUID id);

  Optional<ExternalUser> findByEppn(String eppn);

  List<ExternalUser> findAll();

  List<ExternalUser> findAll(UUID institutionId, UUID groupId);

  int countAll();

  Map<UUID, Integer> countAllByGroupIdsWithSubGroups(Collection<UUID> groupIds);
}
