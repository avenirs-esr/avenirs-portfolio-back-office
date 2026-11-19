package fr.avenirsesr.portfolio.backoffice.tool.domain.port.output.repository;

import fr.avenirsesr.portfolio.backoffice.tool.domain.model.EToolType;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.data.domain.port.output.repository.GenericRepositoryPort;
import java.util.Collection;
import java.util.UUID;

public interface ToolRepository extends GenericRepositoryPort<Tool> {
  PagedResult<Tool> findAllNational(PageCriteria pageCriteria);

  PagedResult<Tool> findAllOfInstitutions(
      Collection<UUID> institutionIds, EToolType type, PageCriteria pageCriteria);

  PagedResult<Tool> findAllNationalOrOfInstitutions(
      Collection<UUID> institutionIds, EToolType type, PageCriteria pageCriteria);
}
