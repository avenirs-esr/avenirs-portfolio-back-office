package fr.avenirsesr.portfolio.backoffice.institution.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class InstitutionNotFoundException extends BusinessException {
  public InstitutionNotFoundException() {
    super(EErrorCode.INSTITUTION_NOT_FOUND);
  }

  public InstitutionNotFoundException(String customMessage) {
    super(EErrorCode.INSTITUTION_NOT_FOUND, customMessage);
  }
}
