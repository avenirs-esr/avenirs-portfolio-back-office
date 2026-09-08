package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder;

import fr.avenirsesr.portfolio.backoffice.additionalskill.infrastructure.seeder.AdditionalSkillConfigSeeder;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.repository.ExternalUserJpaRepository;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.seeder.ExternalUserSeeder;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.repository.GroupJpaRepository;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.seeder.GroupSeeder;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.repository.InstitutionConfigJpaRepository;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.repository.InstitutionJpaRepository;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.seeder.InstitutionConfigSeeder;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.seeder.InstitutionSeeder;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.repository.ConfigurationJpaRepository;
import fr.avenirsesr.portfolio.backoffice.trace.infrastructure.seeder.TraceConfigSeeder;
import fr.avenirsesr.portfolio.backoffice.websitecontent.infrastructure.seeder.WebsiteContentConfigurationSeeder;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.configuration.SeedingState;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeederOrchestrator {
  private final AdditionalSkillConfigSeeder additionalSkillConfigSeeder;
  private final TraceConfigSeeder traceConfigSeeder;
  private final WebsiteContentConfigurationSeeder websiteContentConfigurationSeeder;
  private final InstitutionSeeder institutionSeeder;
  private final InstitutionConfigSeeder institutionConfigSeeder;
  private final GroupSeeder groupSeeder;
  private final ExternalUserSeeder externalUserSeeder;
  private final SeedingState seedingState;
  private final ExternalUserJpaRepository externalUserJpaRepository;
  private final InstitutionConfigJpaRepository institutionConfigJpaRepository;
  private final GroupJpaRepository groupJpaRepository;
  private final InstitutionJpaRepository institutionJpaRepository;
  private final ConfigurationJpaRepository configurationJpaRepository;

  @Transactional()
  public void seedAll() {
    try {
      log.info("Seeding enabled and starting...");

      additionalSkillConfigSeeder.seed();
      traceConfigSeeder.seed();
      websiteContentConfigurationSeeder.seed();
      List<UUID> savedInstitutionIds = institutionSeeder.seed();
      institutionConfigSeeder.seed(savedInstitutionIds);
      List<UUID> savedGroupIds = groupSeeder.seed(savedInstitutionIds);
      externalUserSeeder.seed(savedInstitutionIds, savedGroupIds);

      log.info("✔ Seeding successfully finished");
    } catch (Exception e) {
      seedingState.markFailed(e);
      log.error("✘ Seeding failed", e);
      throw e;
    }
  }

  @Transactional
  public void resetAll() {
    log.warn("Resetting all seeded tables...");
    externalUserJpaRepository.deleteAllInBatch();
    institutionConfigJpaRepository.deleteAllInBatch();
    groupJpaRepository.deleteAllInBatch();
    institutionJpaRepository.deleteAllInBatch();
    configurationJpaRepository.deleteAll();
    log.info("✔ All tables reset");
  }
}
