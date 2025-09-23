package fr.avenirsesr.portfolio.backoffice.additionalskill.application.dto;

import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.model.AdditionalSkillConfiguration;
import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.model.AdditionalSkillLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(requiredProperties = {"BEGINNER", "INTERMEDIATE", "COMPETENT", "ADVANCED", "EXPERT"})
public record AdditionalSkillConfigurationDTO(
    AdditionalSkillLevelDTO BEGINNER,
    AdditionalSkillLevelDTO INTERMEDIATE,
    AdditionalSkillLevelDTO COMPETENT,
    AdditionalSkillLevelDTO ADVANCED,
    AdditionalSkillLevelDTO EXPERT) {

  public static AdditionalSkillConfigurationDTO fromModel(
      AdditionalSkillConfiguration configuration) {
    return new AdditionalSkillConfigurationDTO(
        new AdditionalSkillLevelDTO(
            configuration.BEGINNER().label(), configuration.BEGINNER().description()),
        new AdditionalSkillLevelDTO(
            configuration.INTERMEDIATE().label(), configuration.INTERMEDIATE().description()),
        new AdditionalSkillLevelDTO(
            configuration.COMPETENT().label(), configuration.COMPETENT().description()),
        new AdditionalSkillLevelDTO(
            configuration.ADVANCED().label(), configuration.ADVANCED().description()),
        new AdditionalSkillLevelDTO(
            configuration.EXPERT().label(), configuration.EXPERT().description()));
  }

  public static AdditionalSkillConfiguration toModel(AdditionalSkillConfigurationDTO dto) {
    return new AdditionalSkillConfiguration(
        new AdditionalSkillLevel(dto.BEGINNER().label(), dto.BEGINNER().description()),
        new AdditionalSkillLevel(dto.INTERMEDIATE().label(), dto.INTERMEDIATE().description()),
        new AdditionalSkillLevel(dto.COMPETENT().label(), dto.COMPETENT().description()),
        new AdditionalSkillLevel(dto.ADVANCED().label(), dto.ADVANCED().description()),
        new AdditionalSkillLevel(dto.EXPERT().label(), dto.EXPERT().description()));
  }
}
