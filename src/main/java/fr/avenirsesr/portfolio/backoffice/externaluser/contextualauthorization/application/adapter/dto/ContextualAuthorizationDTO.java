package fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.application.adapter.dto;

import fr.avenirsesr.portfolio.common.authorization.domain.model.enums.EAuthorization;
import fr.avenirsesr.portfolio.common.authorization.domain.model.enums.EAuthorizationEntityType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(requiredProperties = {"entityType", "entityId", "entityName", "authorizations"})
public record ContextualAuthorizationDTO(
    @Schema(ref = "#/components/schemas/EAuthorizationEntityType")
        EAuthorizationEntityType entityType,
    UUID entityId,
    String entityName,
    @ArraySchema(schema = @Schema(ref = "#/components/schemas/EAuthorization"))
        List<EAuthorization> authorizations) {}
