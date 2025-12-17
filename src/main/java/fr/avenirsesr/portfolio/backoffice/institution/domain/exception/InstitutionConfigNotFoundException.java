package fr.avenirsesr.portfolio.backoffice.institution.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class InstitutionConfigNotFoundException extends BusinessException {
  public InstitutionConfigNotFoundException() {
    super(EErrorCode.INSTITUTION_CONFIG_NOT_FOUND);
  }

  public InstitutionConfigNotFoundException(String customMessage) {
    super(EErrorCode.INSTITUTION_CONFIG_NOT_FOUND, customMessage);
  }
}
