package fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.port.input;

import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.model.ContextualAuthorization;
import fr.avenirsesr.portfolio.common.authorization.domain.model.enums.EAuthorizationEntityType;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import java.util.List;
import java.util.UUID;

public interface ExternalUserContextualAuthorizationService {

  List<EAuthorizationEntityType> getAvailableTypes(UUID externalUserId);

  PagedResult<ContextualAuthorization> getAuthorizations(
      UUID externalUserId, EAuthorizationEntityType entityType, PageCriteria pageCriteria);
}
