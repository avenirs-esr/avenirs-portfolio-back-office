package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.EAffiliationScopeNodeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(requiredProperties = {"id", "title", "type", "children"})
public record ScopeNodeResponse(
    UUID id,
    String title,
    @Schema(
            description =
                "Nature of the node: PRIMARY/SECONDARY for institutions,"
                    + " PROGRAM/PROGRAM_OPTION/STUDENT_GROUP for groups.")
        EAffiliationScopeNodeType type,
    @ArraySchema(
            schema = @Schema(ref = "#/components/schemas/ScopeNodeResponse"),
            arraySchema = @Schema(description = "Descendants in scope; empty for a leaf."))
        List<ScopeNodeResponse> children) {}
