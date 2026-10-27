package fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.application.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.application.adapter.dto.ContextualAuthorizationDTO;
import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.model.ContextualAuthorization;

public final class ContextualAuthorizationApplicationMapper {
  private ContextualAuthorizationApplicationMapper() {}

  public static ContextualAuthorizationDTO toContextualAuthorizationDTO(
      ContextualAuthorization authorization) {
    return new ContextualAuthorizationDTO(
        authorization.entityType(),
        authorization.entityId(),
        authorization.entityName(),
        authorization.authorizations().stream().sorted().toList());
  }
}
