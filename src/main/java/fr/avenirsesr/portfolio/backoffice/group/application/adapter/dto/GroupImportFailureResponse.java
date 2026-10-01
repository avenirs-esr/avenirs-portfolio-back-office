package fr.avenirsesr.portfolio.backoffice.group.application.adapter.dto;

import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupImportFailure;

public record GroupImportFailureResponse(String idSISco, String message) {

  public static GroupImportFailureResponse from(GroupImportFailure failure) {
    return new GroupImportFailureResponse(failure.idSISco(), failure.message());
  }
}
