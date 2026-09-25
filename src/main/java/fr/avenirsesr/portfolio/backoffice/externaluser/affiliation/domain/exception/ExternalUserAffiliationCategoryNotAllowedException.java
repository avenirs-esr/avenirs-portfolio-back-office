package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class ExternalUserAffiliationCategoryNotAllowedException extends BusinessException {
  public ExternalUserAffiliationCategoryNotAllowedException() {
    super(EErrorCode.EXTERNAL_USER_AFFILIATION_CATEGORY_NOT_ALLOWED);
  }

  public ExternalUserAffiliationCategoryNotAllowedException(String customMessage) {
    super(EErrorCode.EXTERNAL_USER_AFFILIATION_CATEGORY_NOT_ALLOWED, customMessage);
  }
}
