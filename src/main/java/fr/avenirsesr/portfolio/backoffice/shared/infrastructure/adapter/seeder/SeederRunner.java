package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder;

import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationScope;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.output.repository.ConfigurationRepository;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.configuration.SeedingState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeederRunner implements CommandLineRunner {
  private final ConfigurationRepository configurationRepository;
  private final SeederOrchestrator seederOrchestrator;
  private final SeedingState seedingState;

  @Value("${seeder.enabled:false}")
  private boolean seedEnabled;

  @Override
  public void run(String... args) {
    long websiteConfCount =
        configurationRepository.countAllInScope(EConfigurationScope.WEBSITE_CONTENT);
    if (!seedEnabled) {
      log.info("Seeder disabled: skipped");
      seedingState.markCompleted();
      return;
    }

    if (websiteConfCount > 0) {
      log.info("{} website configurations found. Seeder skipped.", websiteConfCount);
      seedingState.markCompleted();
      return;
    }
    seederOrchestrator.seedAll();
  }
}
