package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model;

import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.ERole;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.EUniverse;
import java.util.Set;

public record ExternalUserGlobalAuthorization(Set<ERole> roles, Set<EUniverse> universes) {}
