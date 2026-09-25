package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationData;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationImportSummary;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationScope;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Wraps the domain {@link ExternalUserAffiliationService} to add transaction boundaries around the
 * find-or-create logic, keeping the Spring transaction dependency out of the domain layer.
 */
@AllArgsConstructor
public class TransactionalExternalUserAffiliationService implements ExternalUserAffiliationService {
  private final ExternalUserAffiliationService delegate;

  @Override
  @Transactional
  public ExternalUserAffiliation addAffiliation(
      UUID externalUserId, UUID institutionId, UUID groupId, EUserCategory category) {
    return delegate.addAffiliation(externalUserId, institutionId, groupId, category);
  }

  @Override
  @Transactional
  public void removeAffiliation(UUID externalUserId, UUID affiliationId) {
    delegate.removeAffiliation(externalUserId, affiliationId);
  }

  @Override
  public List<ExternalUserAffiliation> getAffiliations(UUID externalUserId) {
    return delegate.getAffiliations(externalUserId);
  }

  @Override
  @Transactional
  public ExternalUserAffiliationImportSummary createAll(
      List<ExternalUserAffiliationData> affiliations) {
    return delegate.createAll(affiliations);
  }

  @Override
  public boolean staffHasAccess(
      String eppn, List<UUID> targetInstitutionIds, List<UUID> targetGroupIds) {
    return delegate.staffHasAccess(eppn, targetInstitutionIds, targetGroupIds);
  }

  @Override
  public ExternalUserAffiliationScope studentScope(String eppn) {
    return delegate.studentScope(eppn);
  }
}
