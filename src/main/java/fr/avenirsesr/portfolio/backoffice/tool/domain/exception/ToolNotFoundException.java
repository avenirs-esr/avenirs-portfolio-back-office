package fr.avenirsesr.portfolio.backoffice.tool.domain.exception;

import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;

public class ToolNotFoundException extends BusinessException {
  public ToolNotFoundException() {
    super(EErrorCode.TOOL_NOT_FOUND);
  }
}
