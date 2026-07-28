package fr.avenirsesr.portfolio.backoffice.group.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class GroupNotFoundException extends BusinessException {
  public GroupNotFoundException() {
    super(EErrorCode.GROUP_NOT_FOUND);
  }

  public GroupNotFoundException(String customMessage) {
    super(EErrorCode.GROUP_NOT_FOUND, customMessage);
  }
}
