package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationImportFailure;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;

public record ExternalUserAffiliationImportFailureResponse(
    String eppn,
    String institutionHai,
    String groupIdSiSco,
    EUserCategory category,
    String message) {

  public static ExternalUserAffiliationImportFailureResponse from(
      ExternalUserAffiliationImportFailure failure) {
    return new ExternalUserAffiliationImportFailureResponse(
        failure.eppn(),
        failure.institutionHai(),
        failure.groupIdSiSco(),
        failure.category(),
        failure.message());
  }
}
