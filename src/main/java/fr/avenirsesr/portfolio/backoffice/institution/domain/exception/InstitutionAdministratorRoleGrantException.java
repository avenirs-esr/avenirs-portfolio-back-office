package fr.avenirsesr.portfolio.backoffice.institution.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class InstitutionAdministratorRoleGrantException extends BusinessException {
  public InstitutionAdministratorRoleGrantException() {
    super(EErrorCode.ADMIN_ROLE_GRANT_FAILED);
  }
}
