package fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionAdministrator;
import java.util.List;
import java.util.UUID;

public interface InstitutionAdministratorRepository {
  InstitutionAdministrator save(InstitutionAdministrator administrator);

  void deleteByUserAndInstitution(UUID userId, UUID institutionId);

  boolean existsByUserAndInstitution(UUID userId, UUID institutionId);

  List<UUID> findInstitutionIdsByUser(UUID userId);

  List<InstitutionAdministrator> findByInstitution(UUID institutionId);
}
