package fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.client;

import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.ERole;
import java.util.Set;

public interface SecurityClient {

  Set<ERole> findRolesByLogin(String login);
}
