package fr.avenirsesr.portfolio.backoffice.cgu.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class CguUploadException extends BusinessException {

  public CguUploadException(String message) {
    super(EErrorCode.FILE_STORAGE_ERROR, message);
  }
}
