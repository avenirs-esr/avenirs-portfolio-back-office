package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionAdministrator;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionAdministratorEntity;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.mapper.Mapper;

public class InstitutionAdministratorMapper
    implements Mapper<InstitutionAdministratorEntity, InstitutionAdministrator> {

  public static final InstitutionAdministratorMapper INSTANCE =
      new InstitutionAdministratorMapper();

  @Override
  public InstitutionAdministratorEntity fromDomain(InstitutionAdministrator administrator) {

    return InstitutionAdministratorEntity.of(
        administrator.getId(),
        administrator.getExternalUserId(),
        InstitutionMapper.INSTANCE.fromDomain(administrator.getInstitution()),
        administrator.getCreatedBy(),
        administrator.getCreatedAt(),
        administrator.getUpdatedAt());
  }

  @Override
  public InstitutionAdministrator toDomain(InstitutionAdministratorEntity entity) {
    return InstitutionAdministrator.toDomain(
        entity.getId(),
        entity.getExternalUserId(),
        InstitutionMapper.INSTANCE.toDomain(entity.getInstitution()),
        entity.getCreatedAt(),
        entity.getCreatedBy());
  }
}
