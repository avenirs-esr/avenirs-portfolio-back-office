package fr.avenirsesr.portfolio.backoffice.externaluser.domain.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.input.ExternalUserService;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.user.domain.exceptions.ExternalUserNotFoundException;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import java.util.List;
import java.util.Optional;
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
      EUserCategory category,
      String externalId,
      EExternalSource source,
      EUserStatus status) {

    var externalUser =
        ExternalUser.create(
            eppn,
            externalId,
            source,
            category,
            email,
            firstName,
            lastName,
            status != null ? status : EUserStatus.ACTIVE);

    externalUserRepository.save(externalUser);

    return externalUser;
  }

  @Override
  public List<ExternalUser> getAllExternalUsers() {
    return externalUserRepository.findAll();
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
}
