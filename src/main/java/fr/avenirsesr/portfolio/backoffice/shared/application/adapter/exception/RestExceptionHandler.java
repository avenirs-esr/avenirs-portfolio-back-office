package fr.avenirsesr.portfolio.backoffice.shared.application.adapter.exception;

import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionConfigNotFoundException;
import fr.avenirsesr.portfolio.common.error.application.adapter.exception.BaseRestExceptionHandler;
import fr.avenirsesr.portfolio.common.error.application.adapter.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler extends BaseRestExceptionHandler {
  @ExceptionHandler(InstitutionConfigNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleInstitutionConfigNotFoundException(
      InstitutionConfigNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(ex.getErrorCode().name(), ex.getMessage()));
  }
}
