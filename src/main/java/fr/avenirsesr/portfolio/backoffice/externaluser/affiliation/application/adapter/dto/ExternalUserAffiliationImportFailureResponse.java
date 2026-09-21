package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationImportFailure;

public record ExternalUserAffiliationImportFailureResponse(
    String eppn, String institutionHai, String groupIdSiSco, String message) {

  public static ExternalUserAffiliationImportFailureResponse from(
      ExternalUserAffiliationImportFailure failure) {
    return new ExternalUserAffiliationImportFailureResponse(
        failure.eppn(), failure.institutionHai(), failure.groupIdSiSco(), failure.message());
  }
}
