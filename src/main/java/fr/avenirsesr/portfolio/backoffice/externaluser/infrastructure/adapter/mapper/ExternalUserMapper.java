package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.model.ExternalUserEntity;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.mapper.GroupMapper;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.model.GroupEntity;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.mapper.InstitutionMapper;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.mapper.Mapper;

public class ExternalUserMapper implements Mapper<ExternalUserEntity, ExternalUser> {

  public static final ExternalUserMapper INSTANCE = new ExternalUserMapper();

  @Override
  public ExternalUserEntity fromDomain(ExternalUser externalUser) {
    GroupEntity groupEntity =
        externalUser.getGroup() == null
            ? null
            : GroupMapper.INSTANCE.fromDomain(externalUser.getGroup());

    return ExternalUserEntity.of(
        externalUser.getId(),
        externalUser.getEppn(),
        externalUser.getExternalId(),
        externalUser.getSource(),
        externalUser.getCategory(),
        externalUser.getEmail(),
        externalUser.getFirstName(),
        externalUser.getLastName(),
        InstitutionMapper.INSTANCE.fromDomain(externalUser.getInstitution()),
        groupEntity,
        externalUser.getStatus(),
        externalUser.getCreatedAt(),
        externalUser.getUpdatedAt());
  }

  @Override
  public ExternalUser toDomain(ExternalUserEntity externalUserEntity) {
    Group group =
        externalUserEntity.getGroup() == null
            ? null
            : GroupMapper.INSTANCE.toDomain(externalUserEntity.getGroup());

    return ExternalUser.toDomain(
        externalUserEntity.getId(),
        externalUserEntity.getCreatedAt(),
        externalUserEntity.getUpdatedAt(),
        externalUserEntity.getEppn(),
        externalUserEntity.getExternalId(),
        externalUserEntity.getSource(),
        externalUserEntity.getCategory(),
        externalUserEntity.getEmail(),
        externalUserEntity.getFirstName(),
        externalUserEntity.getLastName(),
        InstitutionMapper.INSTANCE.toDomain(externalUserEntity.getInstitution()),
        group,
        externalUserEntity.getStatus());
  }
}
