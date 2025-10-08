package fr.avenirsesr.portfolio.backoffice.additionalskill.application.dto;

import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.model.AdditionalSkillLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(requiredProperties = {"label", "description"})
public record AdditionalSkillLevelDTO(String label, String description) {
  public static AdditionalSkillLevelDTO from(AdditionalSkillLevel level) {
    return new AdditionalSkillLevelDTO(level.label(), level.description());
  }
}
