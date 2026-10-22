package fr.avenirsesr.portfolio.backoffice.group.application.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.common.group.application.adapter.dto.GroupDTO;
import fr.avenirsesr.portfolio.common.group.application.adapter.dto.GroupOverviewDTO;

public class GroupApplicationMapper {

  private GroupApplicationMapper() {}

  public static GroupDTO toGroupDTO(Group group, int externalUsersCount) {
    return new GroupDTO(
        group.getId(),
        group.getName(),
        group.getType(),
        group.getInstitution().getId(),
        group.getInstitution().getName(),
        group.getParent().map(Group::getId).orElse(null),
        group.getParent().map(Group::getName).orElse(null),
        externalUsersCount);
  }

  public static GroupOverviewDTO toGroupOverviewDTO(Group group, int externalUsersCount) {
    return new GroupOverviewDTO(
        group.getId(),
        group.getName(),
        group.getType(),
        externalUsersCount,
        group.getParent().map(Group::getId).orElse(null),
        group.getParent().map(Group::getName).orElse(null));
  }
}
