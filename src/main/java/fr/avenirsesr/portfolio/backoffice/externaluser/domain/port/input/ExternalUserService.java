package fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.input;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUserData;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUserImportSummary;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExternalUserService {

  ExternalUser importExternalUser(
      String eppn,
      String firstName,
      String lastName,
      String email,
      EUserCategory category,
      String externalId,
      EExternalSource source,
      UUID institutionId,
      UUID groupId,
      EUserStatus status);

  ExternalUserImportSummary createAll(List<ExternalUserData> externalUsers);

  ExternalUser update(
      String eppn,
      String firstName,
      String lastName,
      String email,
      EUserCategory category,
      String externalId,
      EExternalSource source,
      UUID institutionId,
      UUID groupId,
      EUserStatus status);

  List<ExternalUser> updateAll(List<ExternalUserData> externalUsers);

  List<ExternalUser> getAllExternalUsers();

  Optional<ExternalUser> getById(UUID id);

  Optional<ExternalUser> getByEppn(String eppn);

  ExternalUser activateByEppn(String eppn);

  void delete(UUID id);
}
