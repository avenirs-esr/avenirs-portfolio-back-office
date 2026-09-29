package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(requiredProperties = {"institutions", "groups"})
public record StaffScopeResponse(
    List<ScopeNodeResponse> institutions, List<ScopeNodeResponse> groups) {}
