package fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto;

import fr.avenirsesr.portfolio.backoffice.tool.domain.model.EToolType;
import fr.avenirsesr.portfolio.common.file.application.adapter.dto.FileDTO;
import fr.avenirsesr.portfolio.common.institution.application.adapter.dto.InstitutionReferenceDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record ToolDTO(
    UUID id,
    String name,
    String description,
    String url,
    @Schema(ref = "#/components/schemas/EToolType") EToolType type,
    InstitutionReferenceDTO institution,
    FileDTO banner) {}
