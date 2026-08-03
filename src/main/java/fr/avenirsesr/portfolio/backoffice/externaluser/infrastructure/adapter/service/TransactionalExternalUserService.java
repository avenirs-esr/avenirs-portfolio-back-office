package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUserData;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUserImportSummary;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.input.ExternalUserService;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Wraps the domain {@link ExternalUserService} to add transaction boundaries around bulk
 * operations, keeping the Spring transaction dependency out of the domain layer.
 */
@AllArgsConstructor
public class TransactionalExternalUserService implements ExternalUserService {
  private final ExternalUserService delegate;

  @Override
  public ExternalUser importExternalUser(
      String eppn,
      String firstName,
      String lastName,
      String email,
      EUserCategory category,
      String externalId,
      EExternalSource source,
      UUID institutionId,
      UUID groupId,
      EUserStatus status) {
    return delegate.importExternalUser(
        eppn,
        firstName,
        lastName,
        email,
        category,
        externalId,
        source,
        institutionId,
        groupId,
        status);
  }

  @Override
  @Transactional
  public ExternalUserImportSummary createAll(List<ExternalUserData> externalUsers) {
    return delegate.createAll(externalUsers);
  }

  @Override
  public ExternalUser update(
      String eppn,
      String firstName,
      String lastName,
      String email,
      EUserCategory category,
      String externalId,
      EExternalSource source,
      UUID institutionId,
      UUID groupId,
      EUserStatus status) {
    return delegate.update(
        eppn,
        firstName,
        lastName,
        email,
        category,
        externalId,
        source,
        institutionId,
        groupId,
        status);
  }

  @Override
  @Transactional
  public List<ExternalUser> updateAll(List<ExternalUserData> externalUsers) {
    return delegate.updateAll(externalUsers);
  }

  @Override
  public List<ExternalUser> getAllExternalUsers(UUID institutionId, UUID groupId) {
    return delegate.getAllExternalUsers(institutionId, groupId);
  }

  @Override
  public Optional<ExternalUser> getById(UUID id) {
    return delegate.getById(id);
  }

  @Override
  public Optional<ExternalUser> getByEppn(String eppn) {
    return delegate.getByEppn(eppn);
  }

  @Override
  public ExternalUser activateByEppn(String eppn) {
    return delegate.activateByEppn(eppn);
  }

  @Override
  public void delete(UUID id) {
    delegate.delete(id);
  }
}
