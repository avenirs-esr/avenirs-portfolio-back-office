package fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(requiredProperties = {"name", "description", "url"})
public record CreateNationalToolRequest(String name, String description, String url) {}
