package fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.dto;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUserImportFailure;

public record ExternalUserImportFailureResponse(String eppn, String message) {

  public static ExternalUserImportFailureResponse from(ExternalUserImportFailure failure) {
    return new ExternalUserImportFailureResponse(failure.eppn(), failure.message());
  }
}
