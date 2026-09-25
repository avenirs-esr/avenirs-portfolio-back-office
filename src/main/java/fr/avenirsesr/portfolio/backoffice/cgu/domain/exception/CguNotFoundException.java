package fr.avenirsesr.portfolio.backoffice.cgu.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class CguNotFoundException extends BusinessException {
  public CguNotFoundException() {
    super(EErrorCode.CGU_NOT_FOUND);
  }
}
