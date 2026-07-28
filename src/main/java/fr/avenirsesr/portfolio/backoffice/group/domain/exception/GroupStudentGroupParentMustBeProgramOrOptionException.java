package fr.avenirsesr.portfolio.backoffice.group.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class GroupStudentGroupParentMustBeProgramOrOptionException extends BusinessException {
  public GroupStudentGroupParentMustBeProgramOrOptionException() {
    super(EErrorCode.GROUP_STUDENT_GROUP_PARENT_MUST_BE_PROGRAM_OR_OPTION);
  }

  public GroupStudentGroupParentMustBeProgramOrOptionException(String customMessage) {
    super(EErrorCode.GROUP_STUDENT_GROUP_PARENT_MUST_BE_PROGRAM_OR_OPTION, customMessage);
  }
}
