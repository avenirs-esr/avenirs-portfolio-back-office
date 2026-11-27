package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionAdministratorService;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.client.InstitutionAdministratorRoleAssigner;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionAdministratorRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.service.InstitutionAdministratorServiceImpl;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.LoggedInExternalUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class InstitutionAdministratorServiceConfig {
  private final InstitutionAdministratorRepository institutionAdministratorRepository;
  private final InstitutionRepository institutionRepository;
  private final ExternalUserRepository externalUserRepository;
  private final LoggedInExternalUserService loggedInExternalUserService;
  private final InstitutionAdministratorRoleAssigner roleGranter;

  @Bean
  public InstitutionAdministratorService institutionAdministratorService() {
    return new TransactionalInstitutionAdministratorService(
        new InstitutionAdministratorServiceImpl(
            institutionAdministratorRepository,
            institutionRepository,
            externalUserRepository,
            loggedInExternalUserService,
            roleGranter));
  }
}
