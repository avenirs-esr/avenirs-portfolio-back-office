package fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.client;

import java.util.UUID;

public interface InstitutionAdministratorRoleAssigner {

  void grantAdminRole(UUID userId);
}
