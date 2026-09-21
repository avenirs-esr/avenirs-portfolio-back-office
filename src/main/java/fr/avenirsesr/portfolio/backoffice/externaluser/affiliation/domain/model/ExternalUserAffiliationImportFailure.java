package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model;

public record ExternalUserAffiliationImportFailure(
    String eppn, String institutionHai, String groupIdSiSco, String message) {}
