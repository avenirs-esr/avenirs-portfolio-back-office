package fr.avenirsesr.portfolio.backoffice.institution.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class InstitutionNotPrimaryException extends BusinessException {
  public InstitutionNotPrimaryException() {
    super(EErrorCode.INSTITUTION_NOT_PRIMARY);
  }
}
