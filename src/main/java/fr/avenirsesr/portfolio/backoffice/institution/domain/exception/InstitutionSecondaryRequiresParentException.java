package fr.avenirsesr.portfolio.backoffice.institution.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class InstitutionSecondaryRequiresParentException extends BusinessException {
  public InstitutionSecondaryRequiresParentException() {
    super(EErrorCode.INSTITUTION_SECONDARY_REQUIRES_PARENT);
  }

  public InstitutionSecondaryRequiresParentException(String customMessage) {
    super(EErrorCode.INSTITUTION_SECONDARY_REQUIRES_PARENT, customMessage);
  }
}
