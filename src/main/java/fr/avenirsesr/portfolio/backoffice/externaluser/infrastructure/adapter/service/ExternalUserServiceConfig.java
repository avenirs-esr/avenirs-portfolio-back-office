package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.input.ExternalUserService;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.service.ExternalUserServiceImpl;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ExternalUserServiceConfig {
  private final ExternalUserRepository externalUserRepository;
  private final InstitutionRepository institutionRepository;
  private final GroupRepository groupRepository;

  @Bean
  public ExternalUserService externalUserService() {
    return new TransactionalExternalUserService(
        new ExternalUserServiceImpl(externalUserRepository, institutionRepository, groupRepository));
  }
}
