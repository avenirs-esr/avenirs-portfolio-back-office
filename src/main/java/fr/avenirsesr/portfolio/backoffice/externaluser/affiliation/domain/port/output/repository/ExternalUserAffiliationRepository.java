package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.output.repository;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.data.domain.port.output.repository.GenericRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExternalUserAffiliationRepository
    extends GenericRepositoryPort<ExternalUserAffiliation> {
  PagedResult<ExternalUserAffiliation> findAll(
      String keyword,
      List<EUserCategory> categories,
      List<UUID> institutionIds,
      List<UUID> groupIds,
      PageCriteria pageCriteria);

  List<ExternalUserAffiliation> findAllByExternalUserId(UUID externalUserId);

  List<ExternalUserAffiliation> findAllByExternalUserIdAndCategory(
      UUID externalUserId, EUserCategory category);

  List<UUID> findDistinctInstitutionIds(UUID externalUserId, EUserCategory category);

  List<UUID> findDistinctGroupIds(UUID externalUserId, EUserCategory category);

  Optional<ExternalUserAffiliation> findByExternalUserIdAndInstitutionIdAndGroupIdAndCategory(
      UUID externalUserId, UUID institutionId, UUID groupId, EUserCategory category);
}
