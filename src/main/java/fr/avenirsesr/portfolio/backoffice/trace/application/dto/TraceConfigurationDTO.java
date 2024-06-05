package fr.avenirsesr.portfolio.backoffice.trace.application.dto;

import fr.avenirsesr.portfolio.backoffice.trace.domain.model.TraceConfiguration;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    requiredProperties = {
      "maxRemainingDays",
      "maxRemainingDaysBeforeWarning",
      "maxRemainingDaysBeforeCritical"
    })
public record TraceConfigurationDTO(
    int maxRemainingDays, int maxRemainingDaysBeforeWarning, int maxRemainingDaysBeforeCritical) {

  public static TraceConfigurationDTO of(TraceConfiguration config) {
    return new TraceConfigurationDTO(
        config.maxRemainingDays(),
        config.maxRemainingDaysBeforeWarning(),
        config.maxRemainingDaysBeforeCritical());
  }
}
