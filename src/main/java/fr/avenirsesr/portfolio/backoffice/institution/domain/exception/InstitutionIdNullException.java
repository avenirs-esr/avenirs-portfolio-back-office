package fr.avenirsesr.portfolio.backoffice.institution.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class InstitutionIdNullException extends BusinessException {
  public InstitutionIdNullException() {
    super(EErrorCode.INSTITUTION_ID_NULL);
  }

  public InstitutionIdNullException(String customMessage) {
    super(EErrorCode.INSTITUTION_ID_NULL, customMessage);
  }
}
