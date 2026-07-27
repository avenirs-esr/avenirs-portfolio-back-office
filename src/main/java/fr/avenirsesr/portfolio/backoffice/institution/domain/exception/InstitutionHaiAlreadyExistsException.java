package fr.avenirsesr.portfolio.backoffice.institution.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class InstitutionHaiAlreadyExistsException extends BusinessException {
  public InstitutionHaiAlreadyExistsException() {
    super(EErrorCode.INSTITUTION_HAI_ALREADY_EXISTS);
  }

  public InstitutionHaiAlreadyExistsException(String customMessage) {
    super(EErrorCode.INSTITUTION_HAI_ALREADY_EXISTS, customMessage);
  }
}
