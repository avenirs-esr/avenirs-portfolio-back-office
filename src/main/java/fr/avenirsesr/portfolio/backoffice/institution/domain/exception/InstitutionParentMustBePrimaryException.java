package fr.avenirsesr.portfolio.backoffice.institution.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class InstitutionParentMustBePrimaryException extends BusinessException {
  public InstitutionParentMustBePrimaryException() {
    super(EErrorCode.INSTITUTION_PARENT_MUST_BE_PRIMARY);
  }

  public InstitutionParentMustBePrimaryException(String customMessage) {
    super(EErrorCode.INSTITUTION_PARENT_MUST_BE_PRIMARY, customMessage);
  }
}
