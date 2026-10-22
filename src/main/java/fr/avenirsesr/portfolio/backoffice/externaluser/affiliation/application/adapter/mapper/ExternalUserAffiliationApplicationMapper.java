package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserAffiliationDTO;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserAffiliationOverviewDTO;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.CofolioProfile;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;

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

  /**
   * Builds the overview of an affiliation. A null {@code cofolioProfile} is read as somebody who
   * never logged in, which is what the api reports for an eppn it does not know.
   */
  public static ExternalUserAffiliationOverviewDTO toExternalUserAffiliationOverviewDTO(
      ExternalUserAffiliation affiliation, CofolioProfile cofolioProfile) {
    ExternalUser externalUser = affiliation.getExternalUser();
    Group group = affiliation.getGroup();

    return new ExternalUserAffiliationOverviewDTO(
        affiliation.getId(),
        externalUser.getId(),
        externalUser.getEppn(),
        externalUser.getFirstName(),
        externalUser.getLastName(),
        externalUser.getEmail(),
        cofolioProfile != null && cofolioProfile.hasConnected(),
        cofolioProfile == null ? null : cofolioProfile.profilePictureId(),
        affiliation.getCategory(),
        affiliation.getInstitution().getId(),
        affiliation.getInstitution().getName(),
        group != null ? group.getId() : null,
        group != null ? group.getName() : null,
        group != null ? group.getType() : null);
  }
}
