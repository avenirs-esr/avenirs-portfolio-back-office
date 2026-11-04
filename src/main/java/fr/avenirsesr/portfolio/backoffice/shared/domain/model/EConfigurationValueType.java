package fr.avenirsesr.portfolio.backoffice.shared.domain.model;

import fr.avenirsesr.portfolio.common.configuration.domain.model.EConfiguration;

public enum EConfigurationValueType {
  TEXT,
  JSON;

  public static EConfigurationValueType of(EConfiguration key) {
    return key instanceof ETypedConfiguration typedKey ? typedKey.getValueType() : TEXT;
  }
}
