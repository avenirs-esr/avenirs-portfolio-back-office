package fr.avenirsesr.portfolio.backoffice.group.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class GroupProgramOptionRequiresParentException extends BusinessException {
  public GroupProgramOptionRequiresParentException() {
    super(EErrorCode.GROUP_PROGRAM_OPTION_REQUIRES_PARENT);
  }

  public GroupProgramOptionRequiresParentException(String customMessage) {
    super(EErrorCode.GROUP_PROGRAM_OPTION_REQUIRES_PARENT, customMessage);
  }
}
