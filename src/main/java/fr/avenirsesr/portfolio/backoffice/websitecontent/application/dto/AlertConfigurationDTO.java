package fr.avenirsesr.portfolio.backoffice.websitecontent.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(requiredProperties = {"message"})
public record AlertConfigurationDTO(
    String message, Boolean active, Instant startDate, Instant endDate) {}
