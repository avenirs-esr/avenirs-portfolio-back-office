package fr.avenirsesr.portfolio.backoffice.institution.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class ExternalUserNotStaffException extends BusinessException {
  public ExternalUserNotStaffException() {
    super(EErrorCode.USER_IS_NOT_STAFF_EXCEPTION);
  }
}
