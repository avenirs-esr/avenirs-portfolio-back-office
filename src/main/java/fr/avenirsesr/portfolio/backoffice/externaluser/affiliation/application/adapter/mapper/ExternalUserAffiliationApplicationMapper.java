package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserAffiliationDTO;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;

public class ExternalUserAffiliationApplicationMapper {

  private ExternalUserAffiliationApplicationMapper() {}

  public static ExternalUserAffiliationDTO toExternalUserAffiliationDTO(
      ExternalUserAffiliation affiliation) {
    return new ExternalUserAffiliationDTO(
        affiliation.getId(),
        affiliation.getExternalUser().getId(),
        affiliation.getInstitution().getId(),
        affiliation.getGroup() != null ? affiliation.getGroup().getId() : null,
        affiliation.getCategory(),
        affiliation.getCreatedAt());
  }
}
