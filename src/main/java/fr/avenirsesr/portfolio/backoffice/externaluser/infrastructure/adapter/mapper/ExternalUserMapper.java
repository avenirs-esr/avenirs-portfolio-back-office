package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.model.ExternalUserEntity;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.mapper.Mapper;

public class ExternalUserMapper implements Mapper<ExternalUserEntity, ExternalUser> {

  public static final ExternalUserMapper INSTANCE = new ExternalUserMapper();

  @Override
  public ExternalUserEntity fromDomain(ExternalUser externalUser) {
    return ExternalUserEntity.of(
        externalUser.getId(),
        externalUser.getEppn(),
        externalUser.getExternalId(),
        externalUser.getSource(),
        externalUser.getCategory(),
        externalUser.getEmail(),
        externalUser.getFirstName(),
        externalUser.getLastName(),
        externalUser.getStatus(),
        externalUser.getCreatedAt(),
        externalUser.getUpdatedAt());
  }

  @Override
  public ExternalUser toDomain(ExternalUserEntity externalUserEntity) {
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
        externalUserEntity.getStatus());
  }
}
