package fr.avenirsesr.portfolio.backoffice.institution.application.adapter.dto;

import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(requiredProperties = {"id", "name", "uai", "type"})
public record InstitutionAdministratorDTO(
    UUID id,
    String name,
    String sigle,
    String uai,
    @Schema(ref = "#/components/schemas/EInstitutionType") EInstitutionType type) {}
