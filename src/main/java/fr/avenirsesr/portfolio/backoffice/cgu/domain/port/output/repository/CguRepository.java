package fr.avenirsesr.portfolio.backoffice.cgu.domain.port.output.repository;

import fr.avenirsesr.portfolio.backoffice.cgu.domain.model.Cgu;
import fr.avenirsesr.portfolio.common.data.domain.port.output.repository.GenericRepositoryPort;
import java.util.Optional;

public interface CguRepository extends GenericRepositoryPort<Cgu> {
  Optional<Cgu> findLatest();
}
