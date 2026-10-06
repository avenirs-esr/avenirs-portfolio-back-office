package fr.avenirsesr.portfolio.backoffice.externaluser.domain.model;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalUserRole;
import java.util.Set;

public record ExternalUserData(
    String eppn,
    String firstName,
    String lastName,
    String email,
    Set<EExternalUserRole> roles,
    String externalId,
    String institutionUAI) {}
