package fr.avenirsesr.portfolio.backoffice.group.application.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.common.group.application.adapter.dto.GroupDTO;
import fr.avenirsesr.portfolio.common.group.application.adapter.dto.GroupOverviewDTO;
import fr.avenirsesr.portfolio.common.group.application.adapter.dto.GroupReferenceDTO;
import fr.avenirsesr.portfolio.common.institution.application.adapter.dto.InstitutionReferenceDTO;

public class GroupApplicationMapper {

  private GroupApplicationMapper() {}

  public static GroupDTO toGroupDTO(Group group, int externalUsersCount) {
    return new GroupDTO(
        group.getId(),
        group.getName(),
        group.getType(),
        new InstitutionReferenceDTO(
            group.getInstitution().getId(), group.getInstitution().getName()),
        toParentReference(group),
        externalUsersCount);
  }

  public static GroupOverviewDTO toGroupOverviewDTO(Group group, int externalUsersCount) {
    return new GroupOverviewDTO(
        group.getId(),
        group.getName(),
        group.getType(),
        externalUsersCount,
        toParentReference(group));
  }

  private static GroupReferenceDTO toParentReference(Group group) {
    return group
        .getParent()
        .map(parent -> new GroupReferenceDTO(parent.getId(), parent.getName(), parent.getType()))
        .orElse(null);
  }
}
