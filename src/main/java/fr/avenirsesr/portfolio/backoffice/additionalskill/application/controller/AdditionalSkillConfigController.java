package fr.avenirsesr.portfolio.backoffice.additionalskill.application.controller;

import fr.avenirsesr.portfolio.backoffice.additionalskill.application.dto.AdditionalSkillConfigurationDTO;
import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.model.AdditionalSkillConfiguration;
import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.port.input.AdditionalSkillConfigurationService;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("back-office/config/additional-skills")
public class AdditionalSkillConfigController {
  private final AdditionalSkillConfigurationService service;

  @GetMapping(path = "setup")
  public ResponseEntity<Map<ELanguage, AdditionalSkillConfigurationDTO>>
      getAdditionalSkillConfigWithAllTranslations() {
    log.debug("Received request to get additional-skills config for setup");

    Map<ELanguage, AdditionalSkillConfiguration> config =
        service.getConfigurationWithAllTranslations();

    return ResponseEntity.ok(
        config.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> AdditionalSkillConfigurationDTO.fromModel(entry.getValue()))));
  }

  @PostMapping(path = "setup")
  public ResponseEntity<Void> postAdditionalSkillConfig(
      @RequestBody Map<ELanguage, AdditionalSkillConfigurationDTO> configurations) {
    log.debug("Received request to post additional-skills config : {}", configurations);

    service.postConfiguration(
        configurations.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> AdditionalSkillConfigurationDTO.toModel(entry.getValue()))));

    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  @GetMapping()
  public ResponseEntity<AdditionalSkillConfigurationDTO> getAdditionalSkillConfig() {
    log.debug("Received request to get additional-skills config");

    AdditionalSkillConfiguration config = service.getConfiguration();

    return ResponseEntity.ok(AdditionalSkillConfigurationDTO.fromModel(config));
  }
}
