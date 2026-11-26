package fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(requiredProperties = {"name", "description", "url", "institutionId"})
public record CreateLocalToolRequest(
    String name, String description, String url, UUID institutionId) {}
