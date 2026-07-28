package fr.avenirsesr.portfolio.backoffice.group.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class GroupProgramCannotHaveParentException extends BusinessException {
  public GroupProgramCannotHaveParentException() {
    super(EErrorCode.GROUP_PROGRAM_CANNOT_HAVE_PARENT);
  }

  public GroupProgramCannotHaveParentException(String customMessage) {
    super(EErrorCode.GROUP_PROGRAM_CANNOT_HAVE_PARENT, customMessage);
  }
}
