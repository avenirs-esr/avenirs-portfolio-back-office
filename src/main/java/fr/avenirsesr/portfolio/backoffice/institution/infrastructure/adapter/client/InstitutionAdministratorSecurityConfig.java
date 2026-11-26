package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.client;

import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.client.InstitutionAdministratorRoleGranter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InstitutionAdministratorSecurityConfig {

  @Bean
  @ConditionalOnMissingBean(InstitutionAdministratorRoleGranter.class)
  public InstitutionAdministratorRoleGranter institutionAdministratorRoleGranter() {
    return new NoOpInstitutionAdministratorRoleGranter();
  }
}
