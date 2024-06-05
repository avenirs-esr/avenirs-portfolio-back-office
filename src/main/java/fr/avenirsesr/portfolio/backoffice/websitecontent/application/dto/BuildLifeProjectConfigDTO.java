package fr.avenirsesr.portfolio.backoffice.websitecontent.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(requiredProperties = {"html"})
public record BuildLifeProjectConfigDTO(String html) {}
