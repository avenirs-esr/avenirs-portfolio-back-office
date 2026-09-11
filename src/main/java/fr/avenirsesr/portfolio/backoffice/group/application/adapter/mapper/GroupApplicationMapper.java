package fr.avenirsesr.portfolio.backoffice.group.application.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.common.group.application.adapter.dto.GroupDTO;

public class GroupApplicationMapper {

  private GroupApplicationMapper() {}

  public static GroupDTO toGroupDTO(Group group) {
    return new GroupDTO(
        group.getId(),
        group.getName(),
        group.getType(),
        group.getParent().map(Group::getId).orElse(null));
  }
}
