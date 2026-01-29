package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder;

import fr.avenirsesr.portfolio.backoffice.additionalskill.infrastructure.seeder.AdditionalSkillConfigSeeder;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.seeder.InstitutionConfigSeeder;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationScope;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.output.repository.ConfigurationRepository;
import fr.avenirsesr.portfolio.backoffice.trace.infrastructure.seeder.TraceConfigSeeder;
import fr.avenirsesr.portfolio.backoffice.websitecontent.infrastructure.seeder.WebsiteContentConfigurationSeeder;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.configuration.SeedingState;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SeederRunner implements CommandLineRunner {
  private final AdditionalSkillConfigSeeder additionalSkillConfigSeeder;
  private final TraceConfigSeeder traceConfigSeeder;
  private final WebsiteContentConfigurationSeeder websiteContentConfigurationSeeder;
  private final InstitutionConfigSeeder institutionConfigSeeder;
  private final ConfigurationRepository configurationRepository;
  private final SeedingState seedingState;

  @Value("${seeder.enabled:false}")
  private boolean seedEnabled;

  public SeederRunner(
      AdditionalSkillConfigSeeder additionalSkillConfigSeeder,
      TraceConfigSeeder traceConfigSeeder,
      WebsiteContentConfigurationSeeder websiteContentConfigurationSeeder,
      InstitutionConfigSeeder institutionConfigSeeder,
      ConfigurationRepository configurationRepository,
      SeedingState seedingState) {
    this.additionalSkillConfigSeeder = additionalSkillConfigSeeder;
    this.traceConfigSeeder = traceConfigSeeder;
    this.websiteContentConfigurationSeeder = websiteContentConfigurationSeeder;
    this.institutionConfigSeeder = institutionConfigSeeder;
    this.configurationRepository = configurationRepository;
    this.seedingState = seedingState;
  }

  @Transactional
  @Override
  public void run(String... args) {
    try {
      long websiteConfCount =
          configurationRepository.inScope(EConfigurationScope.WEBSITE_CONTENT).size();

      if (seedEnabled && websiteConfCount == 0) {
        log.info("Seeding enabled and starting...");

        additionalSkillConfigSeeder.seed();
        traceConfigSeeder.seed();
        websiteContentConfigurationSeeder.seed();
        institutionConfigSeeder.seed(List.of());

        log.info("✔ Seeding successfully finished");
      } else {
        log.info(
            "{} website configurations found. Seeder is disabled: seeding skipped",
            websiteConfCount);
      }
      seedingState.markCompleted();
    } catch (Exception e) {
      seedingState.markFailed(e);
      log.error("✘ Seeding failed", e);
      throw e;
    }
  }
}
