package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.ERole;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.EUniverse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(requiredProperties = {"roles", "universes"})
public record ExternalUserGlobalAuthorizationDTO(Set<ERole> roles, Set<EUniverse> universes) {}
