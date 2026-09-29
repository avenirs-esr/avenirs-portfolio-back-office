package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(requiredProperties = {"id", "title", "children"})
public record ScopeNodeResponse(UUID id, String title, List<ScopeNodeResponse> children) {}
