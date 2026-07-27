package fr.avenirsesr.portfolio.backoffice.institution.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class InstitutionPrimaryCannotHaveParentException extends BusinessException {
  public InstitutionPrimaryCannotHaveParentException() {
    super(EErrorCode.INSTITUTION_PRIMARY_CANNOT_HAVE_PARENT);
  }

  public InstitutionPrimaryCannotHaveParentException(String customMessage) {
    super(EErrorCode.INSTITUTION_PRIMARY_CANNOT_HAVE_PARENT, customMessage);
  }
}
