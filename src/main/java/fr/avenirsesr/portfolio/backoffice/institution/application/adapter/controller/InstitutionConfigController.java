package fr.avenirsesr.portfolio.backoffice.institution.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.institution.application.adapter.InstitutionConfigMapper;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionConfig;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionConfigService;
import fr.avenirsesr.portfolio.common.configuration.domain.model.InstitutionConfigurationElements;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("back-office/config/institution")
public class InstitutionConfigController {
  private final InstitutionConfigService institutionConfigService;

  @GetMapping("/{institutionId}")
  public ResponseEntity<InstitutionConfigurationElements> getInstitutionConfig(
      @PathVariable UUID institutionId) {
    log.info("Fetching institution configuration");
    InstitutionConfig config = institutionConfigService.getInstitutionConfig(institutionId);
    return ResponseEntity.ok(InstitutionConfigMapper.toDTO(config));
  }
}
