package fr.avenirsesr.portfolio.backoffice.institution.domain.service;

import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionIdNullException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionConfig;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionConfigService;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionConfigRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class InstitutionConfigServiceImpl implements InstitutionConfigService {
  private final InstitutionConfigRepository institutionConfigRepository;

  @Override
  public InstitutionConfig getInstitutionConfig(UUID institutionId) {
    if (institutionId == null) {
      throw new InstitutionIdNullException();
    }
    return institutionConfigRepository.findByInstitutionId(institutionId);
  }

  @Override
  public InstitutionConfig create(UUID institutionId, boolean apcEnabled, boolean programEnabled) {
    InstitutionConfig institutionConfig =
        InstitutionConfig.create(institutionId, apcEnabled, programEnabled);
    return institutionConfigRepository.save(institutionConfig);
  }
}
