package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationData;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationImportSummary;
import java.util.List;
import java.util.UUID;

public interface ExternalUserAffiliationService {

  ExternalUserAffiliation addAffiliation(UUID externalUserId, UUID institutionId, UUID groupId);

  void removeAffiliation(UUID externalUserId, UUID affiliationId);

  List<ExternalUserAffiliation> getAffiliations(UUID externalUserId);

  ExternalUserAffiliationImportSummary createAll(List<ExternalUserAffiliationData> affiliations);
}
