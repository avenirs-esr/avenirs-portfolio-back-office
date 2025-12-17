package fr.avenirsesr.portfolio.backoffice.institution.domain.port.input;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionConfig;
import java.util.UUID;

public interface InstitutionConfigService {
  InstitutionConfig getInstitutionConfig(UUID institutionId);

  InstitutionConfig create(UUID institutionId, boolean apcEnabled, boolean programEnabled);
}
