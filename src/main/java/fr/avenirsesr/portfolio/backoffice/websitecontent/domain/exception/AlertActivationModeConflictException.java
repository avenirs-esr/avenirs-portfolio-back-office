package fr.avenirsesr.portfolio.backoffice.websitecontent.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class AlertActivationModeConflictException extends BusinessException {
  public AlertActivationModeConflictException() {
    super(
        EErrorCode.INVALID_ARGUMENT_TYPE,
        "An alert is either activated manually or scheduled, not both");
  }
}
