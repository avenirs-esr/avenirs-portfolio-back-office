package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.output.repository;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.data.domain.port.output.repository.GenericRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExternalUserAffiliationRepository
    extends GenericRepositoryPort<ExternalUserAffiliation> {
  List<ExternalUserAffiliation> findAllByExternalUserId(UUID externalUserId);

  List<ExternalUserAffiliation> findAllByExternalUserIdAndCategory(
      UUID externalUserId, EUserCategory category);

  Optional<ExternalUserAffiliation> findByExternalUserIdAndInstitutionIdAndGroupIdAndCategory(
      UUID externalUserId, UUID institutionId, UUID groupId, EUserCategory category);
}
