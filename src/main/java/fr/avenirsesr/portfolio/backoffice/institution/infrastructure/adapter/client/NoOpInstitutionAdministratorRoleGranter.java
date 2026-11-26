package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.client;

import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.client.InstitutionAdministratorRoleGranter;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpInstitutionAdministratorRoleGranter
    implements InstitutionAdministratorRoleGranter {

  @Override
  public void grantAdminRole(UUID userId) {
    log.warn("ROLE_ADMIN grant skipped for user {}: /access-control/grant not wired yet ", userId);
  }
}
