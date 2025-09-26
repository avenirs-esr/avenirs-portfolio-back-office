package fr.avenirsesr.portfolio.backoffice.trace.infrastructure.seeder;

import fr.avenirsesr.portfolio.backoffice.trace.domain.port.input.TraceConfigurationService;
import fr.avenirsesr.portfolio.common.configuration.domain.model.TraceConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TraceConfigSeeder {
  private final TraceConfigurationService traceConfigurationService;

  @Transactional
  public void seed() {
    log.info("Seeding trace configuration...");
    var config = new TraceConfiguration(90, 10, 5);

    traceConfigurationService.postTraceConfiguration(config);

    log.info("✔ traces configuration saved : {}", config);
  }
}
