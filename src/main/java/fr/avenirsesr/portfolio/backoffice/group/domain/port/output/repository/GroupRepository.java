package fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository;

import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.data.domain.port.output.repository.GenericRepositoryPort;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupRepository extends GenericRepositoryPort<Group> {
  boolean existsByIdSISco(String idSISco);

  Optional<Group> findByIdSISco(String idSISco);

  /** Lists the groups of an institution, sorted alphabetically by name. */
  PagedResult<Group> findAll(
      UUID institutionId, String keyword, EGroupType type, PageCriteria pageCriteria);

  List<Group> findAllByParentIds(Collection<UUID> parentIds);
}
