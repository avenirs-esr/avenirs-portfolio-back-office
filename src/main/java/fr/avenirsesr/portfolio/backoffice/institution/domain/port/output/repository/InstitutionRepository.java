package fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository;

import fr.avenirsesr.portfolio.common.data.domain.port.output.repository.GenericRepositoryPort;
import fr.avenirsesr.portfolio.common.institution.domain.model.Institution;
import java.util.Optional;

public interface InstitutionRepository extends GenericRepositoryPort<Institution> {
  boolean existsByHai(String hai);

  Optional<Institution> findByHai(String hai);
}
