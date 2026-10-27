package fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.port.output.provider;

import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.model.ContextualAuthorization;
import fr.avenirsesr.portfolio.common.authorization.domain.model.enums.EAuthorizationEntityType;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import java.util.Set;
import java.util.UUID;

public interface ContextualAuthorizationProvider {
  Set<EAuthorizationEntityType> supportedTypes();

  Set<EAuthorizationEntityType> availableTypes(String eppn, Set<UUID> institutionIds);

  PagedResult<ContextualAuthorization> findPage(
      EAuthorizationEntityType entityType,
      String eppn,
      Set<UUID> institutionIds,
      PageCriteria pageCriteria);
}
