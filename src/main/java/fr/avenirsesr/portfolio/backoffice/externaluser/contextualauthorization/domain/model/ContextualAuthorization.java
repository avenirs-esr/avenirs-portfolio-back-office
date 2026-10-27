package fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.model;

import fr.avenirsesr.portfolio.common.authorization.domain.model.enums.EAuthorization;
import fr.avenirsesr.portfolio.common.authorization.domain.model.enums.EAuthorizationEntityType;
import java.util.Set;
import java.util.UUID;

public record ContextualAuthorization(
    EAuthorizationEntityType entityType,
    UUID entityId,
    String entityName,
    Set<EAuthorization> authorizations) {}
