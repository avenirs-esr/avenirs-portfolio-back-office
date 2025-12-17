package fr.avenirsesr.portfolio.backoffice.institution.application.adapter;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionConfig;
import fr.avenirsesr.portfolio.common.configuration.domain.model.InstitutionConfigurationElements;

public interface InstitutionConfigMapper {
  static InstitutionConfigurationElements toDTO(InstitutionConfig institutionConfig) {
    return new InstitutionConfigurationElements(
        institutionConfig.isApcEnabled(), institutionConfig.isLifeProjectEnabled());
  }
}
