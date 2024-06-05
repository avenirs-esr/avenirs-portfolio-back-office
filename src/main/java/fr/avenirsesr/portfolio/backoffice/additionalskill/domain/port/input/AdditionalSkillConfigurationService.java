package fr.avenirsesr.portfolio.backoffice.additionalskill.domain.port.input;

import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.model.AdditionalSkillConfiguration;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import java.util.Map;

public interface AdditionalSkillConfigurationService {
  AdditionalSkillConfiguration getConfiguration();

  Map<ELanguage, AdditionalSkillConfiguration> getConfigurationWithAllTranslations();

  void postConfiguration(Map<ELanguage, AdditionalSkillConfiguration> configurations);
}
