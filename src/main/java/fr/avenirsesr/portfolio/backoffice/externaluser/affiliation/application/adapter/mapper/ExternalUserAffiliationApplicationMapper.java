package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserAffiliationDTO;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserAffiliationOverviewDTO;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserDetailDTO;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.AffiliatedExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserDetail;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.CofolioProfile;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.group.application.adapter.mapper.GroupApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.institution.application.adapter.mapper.InstitutionApplicationMapper;
import fr.avenirsesr.portfolio.common.group.application.adapter.dto.GroupReferenceDTO;
import fr.avenirsesr.portfolio.common.institution.application.adapter.dto.InstitutionReferenceDTO;
import java.util.Map;
import java.util.UUID;

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

  public static ExternalUserDetailDTO toExternalUserDetailDTO(
      ExternalUserDetail detail, CofolioProfile cofolioProfile) {
    ExternalUser externalUser = detail.externalUser();

    return new ExternalUserDetailDTO(
        externalUser.getId(),
        externalUser.getFirstName(),
        externalUser.getLastName(),
        externalUser.getEmail(),
        cofolioProfile == null ? null : cofolioProfile.profilePictureId(),
        new InstitutionReferenceDTO(detail.institution().getId(), detail.institution().getName()),
        detail.groups().stream()
            .map(group -> new GroupReferenceDTO(group.getId(), group.getName(), group.getType()))
            .toList());
  }

  /**
   * Builds the overview of an affiliated user. A null {@code cofolioProfile} is read as somebody
   * who never logged in, which is what the api reports for an eppn it does not know. A group
   * missing from {@code usersCountsByGroupId} is read as an empty one.
   */
  public static ExternalUserAffiliationOverviewDTO toExternalUserAffiliationOverviewDTO(
      AffiliatedExternalUser affiliatedExternalUser,
      CofolioProfile cofolioProfile,
      Map<UUID, Integer> usersCountsByGroupId) {
    ExternalUser externalUser = affiliatedExternalUser.externalUser();

    return new ExternalUserAffiliationOverviewDTO(
        externalUser.getId(),
        externalUser.getFirstName(),
        externalUser.getLastName(),
        externalUser.getEmail(),
        cofolioProfile != null && cofolioProfile.hasConnected(),
        cofolioProfile == null ? null : cofolioProfile.profilePictureId(),
        affiliatedExternalUser.categories(),
        affiliatedExternalUser.institutions().stream()
            .map(InstitutionApplicationMapper::toInstitutionDTO)
            .toList(),
        affiliatedExternalUser.groups().stream()
            .map(
                group ->
                    GroupApplicationMapper.toGroupOverviewDTO(
                        group, usersCountsByGroupId.getOrDefault(group.getId(), 0)))
            .toList());
  }
}
