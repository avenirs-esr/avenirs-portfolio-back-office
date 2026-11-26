package fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.client;

import java.util.UUID;

public interface InstitutionAdministratorRoleGranter {

  void grantAdminRole(UUID userId);
}
