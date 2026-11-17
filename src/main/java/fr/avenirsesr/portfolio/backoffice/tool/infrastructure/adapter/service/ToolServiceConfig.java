package fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.LoggedInExternalUserService;
import fr.avenirsesr.portfolio.backoffice.tool.domain.port.input.ToolService;
import fr.avenirsesr.portfolio.backoffice.tool.domain.port.output.repository.ToolRepository;
import fr.avenirsesr.portfolio.backoffice.tool.domain.service.ToolServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ToolServiceConfig {
  private final ToolRepository toolRepository;
  private final InstitutionRepository institutionRepository;
  private final ExternalUserAffiliationService externalUserAffiliationService;
  private final LoggedInExternalUserService loggedInExternalUserService;

  @Bean
  public ToolService toolService() {
    return new TransactionalToolService(
        new ToolServiceImpl(
            toolRepository,
            institutionRepository,
            externalUserAffiliationService,
            loggedInExternalUserService));
  }
}
