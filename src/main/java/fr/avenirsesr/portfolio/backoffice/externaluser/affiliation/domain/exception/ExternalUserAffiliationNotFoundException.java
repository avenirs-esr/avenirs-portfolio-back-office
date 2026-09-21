package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class ExternalUserAffiliationNotFoundException extends BusinessException {
  public ExternalUserAffiliationNotFoundException() {
    super(EErrorCode.EXTERNAL_USER_AFFILIATION_NOT_FOUND);
  }

  public ExternalUserAffiliationNotFoundException(String customMessage) {
    super(EErrorCode.EXTERNAL_USER_AFFILIATION_NOT_FOUND, customMessage);
  }
}
