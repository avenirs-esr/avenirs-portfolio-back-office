package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.model.GroupEntity;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.mapper.InstitutionMapper;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.mapper.Mapper;
import fr.avenirsesr.portfolio.common.group.domain.model.Group;

public class GroupMapper implements Mapper<GroupEntity, Group> {
  public static final GroupMapper INSTANCE = new GroupMapper();

  @Override
  public GroupEntity fromDomain(Group group) {
    GroupEntity parentEntity = group.getParent().map(this::fromDomain).orElse(null);

    return GroupEntity.of(
        group.getId(),
        group.getName(),
        group.getIdSiSco(),
        InstitutionMapper.INSTANCE.fromDomain(group.getInstitution()),
        group.getCodeSise(),
        group.getStartDate(),
        group.getEndDate(),
        group.getType(),
        parentEntity,
        group.getCreatedAt(),
        group.getUpdatedAt());
  }

  @Override
  public Group toDomain(GroupEntity entity) {
    Group parent = entity.getParent() == null ? null : toDomain(entity.getParent());

    return Group.toDomain(
        entity.getId(),
        entity.getName(),
        entity.getIdSiSco(),
        InstitutionMapper.INSTANCE.toDomain(entity.getInstitution()),
        entity.getCodeSise(),
        entity.getStartDate(),
        entity.getEndDate(),
        entity.getType(),
        parent,
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
