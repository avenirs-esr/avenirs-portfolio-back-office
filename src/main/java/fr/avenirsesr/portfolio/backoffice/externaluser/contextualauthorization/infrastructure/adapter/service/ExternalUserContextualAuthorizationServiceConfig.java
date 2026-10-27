package fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.port.input.ExternalUserContextualAuthorizationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.port.output.provider.ContextualAuthorizationProvider;
import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.service.ExternalUserContextualAuthorizationServiceImpl;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalUserContextualAuthorizationServiceConfig {

  @Bean
  public ExternalUserContextualAuthorizationService externalUserContextualAuthorizationService(
      List<ContextualAuthorizationProvider> providers,
      ExternalUserAffiliationService externalUserAffiliationService) {
    return new ExternalUserContextualAuthorizationServiceImpl(
        providers, externalUserAffiliationService);
  }
}
