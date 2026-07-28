package fr.avenirsesr.portfolio.backoffice.group.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class GroupIdSiScoAlreadyExistsException extends BusinessException {
  public GroupIdSiScoAlreadyExistsException() {
    super(EErrorCode.GROUP_ID_SI_SCO_ALREADY_EXISTS);
  }

  public GroupIdSiScoAlreadyExistsException(String customMessage) {
    super(EErrorCode.GROUP_ID_SI_SCO_ALREADY_EXISTS, customMessage);
  }
}
