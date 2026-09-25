package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationData;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationImportSummary;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationScope;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import java.util.List;
import java.util.UUID;

public interface ExternalUserAffiliationService {

  ExternalUserAffiliation addAffiliation(
      UUID externalUserId, UUID institutionId, UUID groupId, EUserCategory category);

  void removeAffiliation(UUID externalUserId, UUID affiliationId);

  List<ExternalUserAffiliation> getAffiliations(UUID externalUserId);

  ExternalUserAffiliationImportSummary createAll(List<ExternalUserAffiliationData> affiliations);

  boolean staffHasAccess(String eppn, List<UUID> targetInstitutionIds, List<UUID> targetGroupIds);

  ExternalUserAffiliationScope studentScope(String eppn);
}
