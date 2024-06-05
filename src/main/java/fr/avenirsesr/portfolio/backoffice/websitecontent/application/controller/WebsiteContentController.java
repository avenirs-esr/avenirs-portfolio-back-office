package fr.avenirsesr.portfolio.backoffice.websitecontent.application.controller;

import fr.avenirsesr.portfolio.backoffice.websitecontent.application.dto.BuildLifeProjectConfigDTO;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.BuildLifeProjectConfiguration;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.port.input.WebsiteContentConfigurationService;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import java.security.Principal;
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
@RequestMapping("back-office/config/website-content")
public class WebsiteContentController {
  private final WebsiteContentConfigurationService service;

  @PostMapping(path = "/setup/build-life-project")
  public ResponseEntity<Void> postBuildLifeProjectConfig(
      @RequestBody Map<ELanguage, BuildLifeProjectConfigDTO> configurations) {
    log.debug("Received request to post build life project config : {}", configurations);

    service.postLifeProjectConfiguration(
        configurations.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> new BuildLifeProjectConfiguration(entry.getValue().html()))));

    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  @GetMapping(path = "/setup/build-life-project")
  public ResponseEntity<Map<ELanguage, BuildLifeProjectConfigDTO>>
      getBuildLifeProjectConfigWithAllTranslations() {
    log.debug("Received request to get build life project config");

    Map<ELanguage, BuildLifeProjectConfiguration> config =
        service.getLifeProjectConfigurationWithAllTranslations();

    return ResponseEntity.ok(
        config.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> new BuildLifeProjectConfigDTO(entry.getValue().html()))));
  }

  @GetMapping(path = "/build-life-project")
  public ResponseEntity<BuildLifeProjectConfigDTO> getBuildLifeProjectConfig(Principal principal) {
    log.debug("Received request from {} to get build life project config", principal.getName());

    BuildLifeProjectConfiguration config = service.getLifeProjectConfiguration();

    return ResponseEntity.ok(new BuildLifeProjectConfigDTO(config.html()));
  }
}
