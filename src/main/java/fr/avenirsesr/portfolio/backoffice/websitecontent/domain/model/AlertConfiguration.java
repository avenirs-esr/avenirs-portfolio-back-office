package fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model;

import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.exception.AlertActivationModeConflictException;
import fr.avenirsesr.portfolio.common.error.domain.exception.FieldValidationException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;
import fr.avenirsesr.portfolio.common.validation.domain.utils.FieldValidationUtils;
import java.time.Instant;

public record AlertConfiguration(
    String message, Boolean active, Instant startDate, Instant endDate) {

  public AlertConfiguration {
    if (message == null || message.isBlank()) {
      throw new FieldValidationException(EErrorCode.NOT_BLANK, "message");
    }
    if (startDate == null && endDate == null) {
      FieldValidationUtils.requireNotNull("active", active);
    } else {
      if (active != null) {
        throw new AlertActivationModeConflictException();
      }
      FieldValidationUtils.requireNotNull("startDate", startDate);
      FieldValidationUtils.requireNotNull("endDate", endDate);
      if (!startDate.isBefore(endDate)) {
        throw new FieldValidationException(EErrorCode.END_DATE_BEFORE_START_DATE);
      }
    }
  }
}
