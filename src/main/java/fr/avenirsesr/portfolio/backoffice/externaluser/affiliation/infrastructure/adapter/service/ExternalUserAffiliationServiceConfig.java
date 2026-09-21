package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.output.repository.ExternalUserAffiliationRepository;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.service.ExternalUserAffiliationServiceImpl;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ExternalUserAffiliationServiceConfig {
  private final ExternalUserAffiliationRepository externalUserAffiliationRepository;
  private final ExternalUserRepository externalUserRepository;
  private final InstitutionRepository institutionRepository;
  private final GroupRepository groupRepository;

  @Bean
  public ExternalUserAffiliationService externalUserAffiliationService() {
    return new TransactionalExternalUserAffiliationService(
        new ExternalUserAffiliationServiceImpl(
            externalUserAffiliationRepository,
            externalUserRepository,
            institutionRepository,
            groupRepository));
  }
}
