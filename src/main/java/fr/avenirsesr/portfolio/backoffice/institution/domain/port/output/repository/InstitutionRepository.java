package fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.common.data.domain.port.output.repository.GenericRepositoryPort;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstitutionRepository extends GenericRepositoryPort<Institution> {
  boolean existsByUai(String uai);

  Optional<Institution> findByUai(String uai);

  List<Institution> findAll(UUID parentId, EInstitutionType type);

  List<Institution> findAllByParentIds(Collection<UUID> parentIds);
}
