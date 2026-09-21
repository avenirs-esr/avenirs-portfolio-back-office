package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model;

import java.util.List;

public record ExternalUserAffiliationImportSummary(
    List<ExternalUserAffiliation> created,
    List<ExternalUserAffiliation> existing,
    List<ExternalUserAffiliationImportFailure> failed) {}
