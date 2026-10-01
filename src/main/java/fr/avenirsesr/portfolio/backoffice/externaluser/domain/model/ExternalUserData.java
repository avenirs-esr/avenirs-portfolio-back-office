package fr.avenirsesr.portfolio.backoffice.externaluser.domain.model;

import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import java.util.Set;

public record ExternalUserData(
    String eppn,
    String firstName,
    String lastName,
    String email,
    Set<EUserCategory> categories,
    String externalId,
    String institutionUAI) {}
