package fr.avenirsesr.portfolio.backoffice.trace.domain.model;

import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfiguration;

public enum ETraceConfiguration implements EConfiguration {
  MAX_REMINING_DAYS,
  MAX_REMINING_DAYS_BEFORE_WARNING,
  MAX_REMINING_DAYS_BEFORE_CRITICAL
}
