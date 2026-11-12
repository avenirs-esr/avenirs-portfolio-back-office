package fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.dto;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserAffiliationImportSummaryResponse;

public record ProvisionExternalUserResponse(
    ExternalUserImportSummaryResponse user,
    ExternalUserAffiliationImportSummaryResponse affiliations) {}
