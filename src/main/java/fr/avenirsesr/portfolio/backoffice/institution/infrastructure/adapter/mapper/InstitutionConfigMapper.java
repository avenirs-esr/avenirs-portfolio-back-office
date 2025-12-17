package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionConfig;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionConfigEntity;

public interface InstitutionConfigMapper {
  static InstitutionConfigEntity fromDomain(InstitutionConfig institutionConfig) {
    return InstitutionConfigEntity.of(
        institutionConfig.getId(),
        institutionConfig.getInstitutionId(),
        institutionConfig.isApcEnabled(),
        institutionConfig.isLifeProjectEnabled(),
        institutionConfig.getCreatedAt(),
        institutionConfig.getUpdatedAt());
  }

  static InstitutionConfig toDomain(InstitutionConfigEntity entity) {
    return InstitutionConfig.of(
        entity.getId(),
        entity.getInstitutionId(),
        entity.isApcEnabled(),
        entity.isLifeProjectEnabled(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
