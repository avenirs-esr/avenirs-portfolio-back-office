package fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.common.user.application.adapter.dto.ExternalUserDTO;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ExternalUserApplicationMapper {

  private ExternalUserApplicationMapper() {}

  public static ExternalUserDTO toExternalUserDTO(
      ExternalUser externalUser, List<ExternalUserAffiliation> affiliations) {
    List<UUID> institutionIds =
        affiliations.stream().map(a -> a.getInstitution().getId()).distinct().toList();
    List<UUID> groupIds =
        affiliations.stream()
            .map(ExternalUserAffiliation::getGroup)
            .filter(Objects::nonNull)
            .map(Group::getId)
            .toList();

    return new ExternalUserDTO(
        externalUser.getEppn(),
        externalUser.getFirstName(),
        externalUser.getLastName(),
        externalUser.getEmail(),
        externalUser.getCategories(),
        externalUser.getExternalId(),
        externalUser.getSource().name(),
        institutionIds,
        groupIds,
        externalUser.getStatus());
  }
}
