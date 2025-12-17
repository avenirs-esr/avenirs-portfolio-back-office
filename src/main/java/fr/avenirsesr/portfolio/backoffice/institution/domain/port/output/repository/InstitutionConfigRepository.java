package fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionConfig;
import fr.avenirsesr.portfolio.common.data.domain.port.output.repository.GenericRepositoryPort;
import java.util.UUID;

public interface InstitutionConfigRepository extends GenericRepositoryPort<InstitutionConfig> {
  InstitutionConfig findByInstitutionId(UUID institutionId);
}
