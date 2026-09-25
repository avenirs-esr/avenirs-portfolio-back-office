package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.model.ExternalUserAffiliationEntity;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.mapper.ExternalUserMapper;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.mapper.GroupMapper;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.model.GroupEntity;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.mapper.InstitutionMapper;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.mapper.Mapper;

public class ExternalUserAffiliationMapper
    implements Mapper<ExternalUserAffiliationEntity, ExternalUserAffiliation> {

  public static final ExternalUserAffiliationMapper INSTANCE = new ExternalUserAffiliationMapper();

  @Override
  public ExternalUserAffiliationEntity fromDomain(ExternalUserAffiliation affiliation) {
    GroupEntity groupEntity =
        affiliation.getGroup() == null
            ? null
            : GroupMapper.INSTANCE.fromDomain(affiliation.getGroup());

    return ExternalUserAffiliationEntity.of(
        affiliation.getId(),
        ExternalUserMapper.INSTANCE.fromDomain(affiliation.getExternalUser()),
        InstitutionMapper.INSTANCE.fromDomain(affiliation.getInstitution()),
        groupEntity,
        affiliation.getCategory(),
        affiliation.getCreatedAt(),
        affiliation.getUpdatedAt());
  }

  @Override
  public ExternalUserAffiliation toDomain(ExternalUserAffiliationEntity entity) {
    Group group =
        entity.getGroup() == null ? null : GroupMapper.INSTANCE.toDomain(entity.getGroup());

    return ExternalUserAffiliation.toDomain(
        entity.getId(),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        ExternalUserMapper.INSTANCE.toDomain(entity.getExternalUser()),
        InstitutionMapper.INSTANCE.toDomain(entity.getInstitution()),
        group,
        entity.getCategory());
  }
}
