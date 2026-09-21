package fr.avenirsesr.portfolio.backoffice.externaluser.domain.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUserData;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUserImportFailure;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUserImportSummary;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.input.ExternalUserService;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.user.domain.exceptions.ExternalUserNotFoundException;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ExternalUserServiceImpl implements ExternalUserService {

  private final ExternalUserRepository externalUserRepository;

  @Override
  public ExternalUser importExternalUser(
      String eppn,
      String firstName,
      String lastName,
      String email,
      Set<EUserCategory> categories,
      String externalId,
      EExternalSource source,
      EUserStatus status) {

    var externalUser =
        ExternalUser.create(
            eppn,
            externalId,
            source,
            categories,
            email,
            firstName,
            lastName,
            status != null ? status : EUserStatus.ACTIVE);

    return externalUserRepository.save(externalUser);
  }

  @Override
  public ExternalUserImportSummary createAll(List<ExternalUserData> externalUsers) {
    List<ExternalUser> created = new ArrayList<>();
    List<ExternalUser> updated = new ArrayList<>();
    List<ExternalUserImportFailure> failed = new ArrayList<>();

    for (ExternalUserData data : externalUsers) {
      try {
        UpsertResult result = upsert(data);
        if (result.created()) {
          created.add(result.externalUser());
        } else {
          updated.add(result.externalUser());
        }
      } catch (BusinessException e) {
        log.warn("Failed to import external user with eppn {}: {}", data.eppn(), e.getMessage());
        failed.add(new ExternalUserImportFailure(data.eppn(), e.getMessage()));
      }
    }

    return new ExternalUserImportSummary(created, updated, failed);
  }

  /** Creates the external user, or updates the existing one matching the given eppn. */
  private UpsertResult upsert(ExternalUserData data) {
    if (externalUserRepository.findByEppn(data.eppn()).isPresent()) {
      ExternalUser updated =
          update(
              data.eppn(),
              data.firstName(),
              data.lastName(),
              data.email(),
              data.categories(),
              data.externalId(),
              data.source(),
              data.status());
      return new UpsertResult(updated, false);
    }

    ExternalUser created =
        importExternalUser(
            data.eppn(),
            data.firstName(),
            data.lastName(),
            data.email(),
            data.categories(),
            data.externalId(),
            data.source(),
            data.status());
    return new UpsertResult(created, true);
  }

  private record UpsertResult(ExternalUser externalUser, boolean created) {}

  @Override
  public ExternalUser update(
      String eppn,
      String firstName,
      String lastName,
      String email,
      Set<EUserCategory> categories,
      String externalId,
      EExternalSource source,
      EUserStatus status) {
    ExternalUser externalUser =
        externalUserRepository.findByEppn(eppn).orElseThrow(ExternalUserNotFoundException::new);

    externalUser.setFirstName(firstName);
    externalUser.setLastName(lastName);
    externalUser.setEmail(email);
    externalUser.setCategories(categories);
    externalUser.setExternalId(externalId);
    externalUser.setSource(source);
    externalUser.setStatus(status != null ? status : externalUser.getStatus());

    return externalUserRepository.save(externalUser);
  }

  @Override
  public List<ExternalUser> updateAll(List<ExternalUserData> externalUsers) {
    return externalUsers.stream()
        .map(
            data ->
                update(
                    data.eppn(),
                    data.firstName(),
                    data.lastName(),
                    data.email(),
                    data.categories(),
                    data.externalId(),
                    data.source(),
                    data.status()))
        .toList();
  }

  @Override
  public List<ExternalUser> getAllExternalUsers(UUID institutionId, UUID groupId) {
    return externalUserRepository.findAll(institutionId, groupId);
  }

  @Override
  public Optional<ExternalUser> getById(UUID id) {
    return externalUserRepository.findById(id);
  }

  @Override
  public Optional<ExternalUser> getByEppn(String eppn) {
    return externalUserRepository.findByEppn(eppn);
  }

  @Override
  public ExternalUser activateByEppn(String eppn) {
    var externalUser =
        externalUserRepository.findByEppn(eppn).orElseThrow(ExternalUserNotFoundException::new);

    if (!externalUser.isActive()) {
      externalUser.setStatus(EUserStatus.ACTIVE);
      externalUserRepository.save(externalUser);
    }
    return externalUser;
  }

  @Override
  public void delete(UUID id) {
    ExternalUser externalUser =
        externalUserRepository.findById(id).orElseThrow(ExternalUserNotFoundException::new);
    externalUserRepository.removeFromDatabase(externalUser);
  }
}
