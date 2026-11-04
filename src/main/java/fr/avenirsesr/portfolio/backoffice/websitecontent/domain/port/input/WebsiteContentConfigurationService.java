package fr.avenirsesr.portfolio.backoffice.websitecontent.domain.port.input;

import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.AlertConfiguration;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.BuildLifeProjectConfiguration;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import java.util.Map;
import java.util.Optional;

public interface WebsiteContentConfigurationService {
  BuildLifeProjectConfiguration getLifeProjectConfiguration();

  Map<ELanguage, BuildLifeProjectConfiguration> getLifeProjectConfigurationWithAllTranslations();

  void postLifeProjectConfiguration(Map<ELanguage, BuildLifeProjectConfiguration> configuration);

  Optional<AlertConfiguration> getAlertConfiguration();

  AlertConfiguration putAlertConfiguration(AlertConfiguration alertConfiguration);

  Optional<String> getDisplayedAlertMessage();
}
