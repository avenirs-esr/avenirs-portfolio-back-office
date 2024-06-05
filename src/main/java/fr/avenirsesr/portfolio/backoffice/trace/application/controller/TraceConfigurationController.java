package fr.avenirsesr.portfolio.backoffice.trace.application.controller;

import fr.avenirsesr.portfolio.backoffice.trace.application.dto.TraceConfigurationDTO;
import fr.avenirsesr.portfolio.backoffice.trace.domain.model.TraceConfiguration;
import fr.avenirsesr.portfolio.backoffice.trace.domain.port.input.TraceConfigurationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("back-office/config/traces")
public class TraceConfigurationController {
  private final TraceConfigurationService traceConfigurationService;

  @GetMapping
  public ResponseEntity<TraceConfigurationDTO> getTraceConfig() {
    log.debug("Received request to get trace config");

    var config = traceConfigurationService.getTraceConfiguration();

    return ResponseEntity.ok(TraceConfigurationDTO.of(config));
  }

  @PostMapping
  public ResponseEntity<Void> postTraceConfig(@RequestBody TraceConfigurationDTO config) {
    log.debug("Received request to post trace config : {}", config);

    traceConfigurationService.postTraceConfiguration(
        new TraceConfiguration(
            config.maxRemainingDays(),
            config.maxRemainingDaysBeforeWarning(),
            config.maxRemainingDaysBeforeCritical()));

    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }
}
