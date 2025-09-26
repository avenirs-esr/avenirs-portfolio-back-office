package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder;

import fr.avenirsesr.portfolio.backoffice.additionalskill.infrastructure.seeder.AdditionalSkillConfigSeeder;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationScope;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.output.repository.ConfigurationRepository;
import fr.avenirsesr.portfolio.backoffice.trace.infrastructure.seeder.TraceConfigSeeder;
import fr.avenirsesr.portfolio.backoffice.websitecontent.infrastructure.seeder.WebsiteContentConfigurationSeeder;
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
  private final ConfigurationRepository configurationRepository;

  @Value("${seeder.enabled:false}")
  private boolean seedEnabled;

  public SeederRunner(
      AdditionalSkillConfigSeeder additionalSkillConfigSeeder,
      TraceConfigSeeder traceConfigSeeder,
      WebsiteContentConfigurationSeeder websiteContentConfigurationSeeder,
      ConfigurationRepository configurationRepository) {
    this.additionalSkillConfigSeeder = additionalSkillConfigSeeder;
    this.traceConfigSeeder = traceConfigSeeder;
    this.websiteContentConfigurationSeeder = websiteContentConfigurationSeeder;
    this.configurationRepository = configurationRepository;
  }

  @Transactional
  @Override
  public void run(String... args) {
    long websiteConfCount =
        configurationRepository.inScope(EConfigurationScope.WEBSITE_CONTENT).size();

    if (seedEnabled && websiteConfCount == 0) {
      log.info("Seeding enabled and starting...");

      additionalSkillConfigSeeder.seed();
      traceConfigSeeder.seed();
      websiteContentConfigurationSeeder.seed();

      log.info("✔ Seeding successfully finished");
    } else
      log.info(
          "{} website configurations found. Seeder is disabled: seeding skipped", websiteConfCount);
  }
}
