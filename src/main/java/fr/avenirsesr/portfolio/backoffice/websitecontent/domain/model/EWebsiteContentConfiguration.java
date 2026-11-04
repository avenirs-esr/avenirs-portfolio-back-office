package fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model;

import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationValueType;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.ETypedConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EWebsiteContentConfiguration implements ETypedConfiguration {
  BUILD_LIFE_PROJECT_CONTENT(EConfigurationValueType.TEXT),
  ALERT(EConfigurationValueType.JSON);

  private final EConfigurationValueType valueType;
}
