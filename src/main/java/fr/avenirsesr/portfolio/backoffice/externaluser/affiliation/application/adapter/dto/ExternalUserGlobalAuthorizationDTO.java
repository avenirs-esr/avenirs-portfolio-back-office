package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.ERole;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.EUniverse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(requiredProperties = {"roles", "universes"})
public record ExternalUserGlobalAuthorizationDTO(
    @ArraySchema(schema = @Schema(ref = "#/components/schemas/ERole")) Set<ERole> roles,
    @ArraySchema(schema = @Schema(ref = "#/components/schemas/EUniverse"))
        Set<EUniverse> universes) {}
