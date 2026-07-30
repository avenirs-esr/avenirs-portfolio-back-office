package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionEntity;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.mapper.Mapper;

public class InstitutionMapper implements Mapper<InstitutionEntity, Institution> {
  public static final InstitutionMapper INSTANCE = new InstitutionMapper();

  @Override
  public InstitutionEntity fromDomain(Institution institution) {
    InstitutionEntity parentEntity = institution.getParent().map(this::fromDomain).orElse(null);

    return InstitutionEntity.of(
        institution.getId(),
        institution.getName(),
        institution.getHai(),
        institution.getSiret(),
        institution.getSiren(),
        institution.getType(),
        parentEntity,
        institution.getCreatedAt(),
        institution.getUpdatedAt());
  }

  @Override
  public Institution toDomain(InstitutionEntity entity) {
    Institution parent = entity.getParent() == null ? null : toDomain(entity.getParent());

    return Institution.toDomain(
        entity.getId(),
        entity.getName(),
        entity.getHai(),
        entity.getSiret(),
        entity.getSiren(),
        entity.getType(),
        parent,
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
