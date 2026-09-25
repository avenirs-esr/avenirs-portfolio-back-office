package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model;

import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;

public record ExternalUserAffiliationData(
    String eppn, String institutionHai, String groupIdSiSco, EUserCategory category) {}
