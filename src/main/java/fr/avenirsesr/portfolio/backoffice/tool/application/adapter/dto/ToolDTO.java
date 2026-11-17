package fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto;

import fr.avenirsesr.portfolio.backoffice.tool.domain.model.EToolType;
import java.util.UUID;

public record ToolDTO(
    UUID id, String name, String description, String url, EToolType type, UUID institutionId) {}
