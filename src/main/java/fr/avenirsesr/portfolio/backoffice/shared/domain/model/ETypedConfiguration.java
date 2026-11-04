package fr.avenirsesr.portfolio.backoffice.shared.domain.model;

import fr.avenirsesr.portfolio.common.configuration.domain.model.EConfiguration;

public interface ETypedConfiguration extends EConfiguration {
  EConfigurationValueType getValueType();
}
