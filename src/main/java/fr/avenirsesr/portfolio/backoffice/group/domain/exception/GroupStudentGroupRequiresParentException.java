package fr.avenirsesr.portfolio.backoffice.group.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class GroupStudentGroupRequiresParentException extends BusinessException {
  public GroupStudentGroupRequiresParentException() {
    super(EErrorCode.GROUP_STUDENT_GROUP_REQUIRES_PARENT);
  }

  public GroupStudentGroupRequiresParentException(String customMessage) {
    super(EErrorCode.GROUP_STUDENT_GROUP_REQUIRES_PARENT, customMessage);
  }
}
