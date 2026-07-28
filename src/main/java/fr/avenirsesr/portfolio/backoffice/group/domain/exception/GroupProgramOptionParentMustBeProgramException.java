package fr.avenirsesr.portfolio.backoffice.group.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class GroupProgramOptionParentMustBeProgramException extends BusinessException {
  public GroupProgramOptionParentMustBeProgramException() {
    super(EErrorCode.GROUP_PROGRAM_OPTION_PARENT_MUST_BE_PROGRAM);
  }

  public GroupProgramOptionParentMustBeProgramException(String customMessage) {
    super(EErrorCode.GROUP_PROGRAM_OPTION_PARENT_MUST_BE_PROGRAM, customMessage);
  }
}
