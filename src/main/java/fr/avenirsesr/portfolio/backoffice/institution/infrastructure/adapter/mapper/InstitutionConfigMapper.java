package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionConfig;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionConfigEntity;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.mapper.Mapper;

public class InstitutionConfigMapper implements Mapper<InstitutionConfigEntity, InstitutionConfig> {
  public static final InstitutionConfigMapper INSTANCE = new InstitutionConfigMapper();

  @Override
  public InstitutionConfigEntity fromDomain(InstitutionConfig institutionConfig) {
    return InstitutionConfigEntity.of(
        institutionConfig.getId(),
        institutionConfig.getInstitutionId(),
        institutionConfig.isApcEnabled(),
        institutionConfig.isLifeProjectEnabled(),
        institutionConfig.getCreatedAt(),
        institutionConfig.getUpdatedAt());
  }

  @Override
  public InstitutionConfig toDomain(InstitutionConfigEntity entity) {
    return InstitutionConfig.of(
        entity.getId(),
        entity.getInstitutionId(),
        entity.isApcEnabled(),
        entity.isLifeProjectEnabled(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
