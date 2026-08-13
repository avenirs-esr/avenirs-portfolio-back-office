package fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.common.user.application.adapter.dto.ExternalUserDTO;

public class ExternalUserApplicationMapper {

  private ExternalUserApplicationMapper() {}

  public static ExternalUserDTO toExternalUserDTO(ExternalUser externalUser) {
    return new ExternalUserDTO(
        externalUser.getEppn(),
        externalUser.getFirstName(),
        externalUser.getLastName(),
        externalUser.getEmail(),
        externalUser.getCategories(),
        externalUser.getExternalId(),
        externalUser.getSource().name(),
        externalUser.getInstitution().getId(),
        externalUser.getGroup() != null ? externalUser.getGroup().getId() : null,
        externalUser.getStatus());
  }
}
