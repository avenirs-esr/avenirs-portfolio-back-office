package fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto;

import java.util.UUID;

public record CreateLocalToolRequest(
    String name, String description, String url, UUID institutionId) {}
