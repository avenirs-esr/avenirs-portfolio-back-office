package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model;

import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;

public record ExternalUserAffiliationImportFailure(
    String eppn,
    String institutionHai,
    String groupIdSiSco,
    EUserCategory category,
    String message) {}
