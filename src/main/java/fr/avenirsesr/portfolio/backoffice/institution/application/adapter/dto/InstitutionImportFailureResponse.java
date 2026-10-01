package fr.avenirsesr.portfolio.backoffice.institution.application.adapter.dto;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionImportFailure;

public record InstitutionImportFailureResponse(String uai, String message) {

  public static InstitutionImportFailureResponse from(InstitutionImportFailure failure) {
    return new InstitutionImportFailureResponse(failure.uai(), failure.message());
  }
}
