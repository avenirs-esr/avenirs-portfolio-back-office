package fr.avenirsesr.portfolio.backoffice.websitecontent.application.controller;

import fr.avenirsesr.portfolio.backoffice.websitecontent.application.dto.AlertConfigurationDTO;
import fr.avenirsesr.portfolio.backoffice.websitecontent.application.dto.BuildLifeProjectConfigDTO;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.AlertConfiguration;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.BuildLifeProjectConfiguration;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.port.input.WebsiteContentConfigurationService;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("back-office/config/website-content")
public class WebsiteContentController {
  private final WebsiteContentConfigurationService websiteContentConfigurationService;

  @PreAuthorize("hasAuthority('website-content-config:update')")
  @PostMapping(path = "/setup/build-life-project")
  public ResponseEntity<Void> postBuildLifeProjectConfig(
      @RequestBody Map<ELanguage, BuildLifeProjectConfigDTO> configurations) {
    log.debug("Received request to post build life project config : {}", configurations);

    websiteContentConfigurationService.postLifeProjectConfiguration(
        configurations.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> new BuildLifeProjectConfiguration(entry.getValue().html()))));

    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  @PreAuthorize("hasAuthority('website-content-config:read')")
  @GetMapping(path = "/setup/build-life-project")
  public ResponseEntity<Map<ELanguage, BuildLifeProjectConfigDTO>>
      getBuildLifeProjectConfigWithAllTranslations() {
    log.debug("Received request to get build life project config with all translations");

    Map<ELanguage, BuildLifeProjectConfiguration> config =
        websiteContentConfigurationService.getLifeProjectConfigurationWithAllTranslations();

    return ResponseEntity.ok(
        config.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> new BuildLifeProjectConfigDTO(entry.getValue().html()))));
  }

  @PreAuthorize("hasAuthority('website-content-config:read')")
  @GetMapping(path = "/build-life-project")
  public ResponseEntity<BuildLifeProjectConfigDTO> getBuildLifeProjectConfig() {
    log.debug("Received request to get build life project config");

    BuildLifeProjectConfiguration config =
        websiteContentConfigurationService.getLifeProjectConfiguration();

    return ResponseEntity.ok(new BuildLifeProjectConfigDTO(config.html()));
  }

  @PreAuthorize("hasAuthority('website-content-config:update')")
  @PutMapping(path = "/setup/alert")
  public ResponseEntity<AlertConfigurationDTO> putAlertConfig(
      @RequestBody AlertConfigurationDTO configuration) {
    log.debug("Received request to put alert config : {}", configuration);

    AlertConfiguration saved =
        websiteContentConfigurationService.putAlertConfiguration(
            new AlertConfiguration(
                configuration.message(),
                configuration.active(),
                configuration.startDate(),
                configuration.endDate()));

    return ResponseEntity.ok(toAlertConfigurationDTO(saved));
  }

  @PreAuthorize("hasAuthority('website-content-config:read')")
  @GetMapping(path = "/setup/alert")
  public ResponseEntity<AlertConfigurationDTO> getAlertConfig() {
    log.debug("Received request to get alert config");

    return ResponseEntity.ok(
        websiteContentConfigurationService
            .getAlertConfiguration()
            .map(WebsiteContentController::toAlertConfigurationDTO)
            .orElseGet(() -> new AlertConfigurationDTO(null, false, null, null)));
  }

  private static AlertConfigurationDTO toAlertConfigurationDTO(AlertConfiguration configuration) {
    return new AlertConfigurationDTO(
        configuration.message(),
        configuration.active(),
        configuration.startDate(),
        configuration.endDate());
  }
}
