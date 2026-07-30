package fr.avenirsesr.portfolio.backoffice.externaluser.domain.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.input.ExternalUserService;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupNotFoundException;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
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
  private final InstitutionRepository institutionRepository;
  private final GroupRepository groupRepository;

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

    Institution institution =
        institutionRepository
            .findById(institutionId)
            .orElseThrow(InstitutionNotFoundException::new);
    Group group =
        groupId != null
            ? groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new)
            : null;

    var externalUser =
        ExternalUser.create(
            eppn,
            externalId,
            source,
            category,
            email,
            firstName,
            lastName,
            institution,
            group,
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
