package fr.avenirsesr.portfolio.backoffice.institution.application.adapter.dto;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionImportFailure;

public record InstitutionImportFailureResponse(String hai, String message) {

  public static InstitutionImportFailureResponse from(InstitutionImportFailure failure) {
    return new InstitutionImportFailureResponse(failure.hai(), failure.message());
  }
}
