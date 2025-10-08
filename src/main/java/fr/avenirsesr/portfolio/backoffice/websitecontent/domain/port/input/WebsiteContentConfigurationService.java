package fr.avenirsesr.portfolio.backoffice.websitecontent.domain.port.input;

import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.BuildLifeProjectConfiguration;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import java.util.Map;

public interface WebsiteContentConfigurationService {
  BuildLifeProjectConfiguration getLifeProjectConfiguration();

  Map<ELanguage, BuildLifeProjectConfiguration> getLifeProjectConfigurationWithAllTranslations();

  void postLifeProjectConfiguration(Map<ELanguage, BuildLifeProjectConfiguration> configuration);
}
