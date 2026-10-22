package fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
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

  /**
   * Pages over the users holding at least one affiliation matching the given filters, each of them
   * reported once however many affiliations it holds, sorted by last name then first name.
   */
  PagedResult<ExternalUser> findAllAffiliated(
      String keyword,
      List<EUserCategory> categories,
      List<UUID> institutionIds,
      List<UUID> groupIds,
      PageCriteria pageCriteria);

  int countAll();

  Map<UUID, Integer> countAllByGroupIdsWithSubGroups(Collection<UUID> groupIds);
}
