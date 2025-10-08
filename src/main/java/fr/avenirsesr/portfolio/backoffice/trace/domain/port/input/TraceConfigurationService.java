package fr.avenirsesr.portfolio.backoffice.trace.domain.port.input;

import fr.avenirsesr.portfolio.common.configuration.domain.model.TraceConfiguration;

public interface TraceConfigurationService {
  TraceConfiguration getTraceConfiguration();

  void postTraceConfiguration(TraceConfiguration traceConfiguration);
}
