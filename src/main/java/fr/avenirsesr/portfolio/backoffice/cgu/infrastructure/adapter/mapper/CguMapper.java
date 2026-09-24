package fr.avenirsesr.portfolio.backoffice.cgu.infrastructure.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.cgu.domain.model.Cgu;
import fr.avenirsesr.portfolio.backoffice.cgu.infrastructure.adapter.model.CguEntity;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.mapper.Mapper;

public class CguMapper implements Mapper<CguEntity, Cgu> {

  public static final CguMapper INSTANCE = new CguMapper();

  @Override
  public CguEntity fromDomain(Cgu cgu) {
    return CguEntity.of(
        cgu.getId(),
        cgu.getFileId(),
        cgu.getVersion(),
        cgu.getUploadedAt(),
        cgu.getCreatedAt(),
        cgu.getUpdatedAt());
  }

  @Override
  public Cgu toDomain(CguEntity entity) {
    return Cgu.toDomain(
        entity.getId(),
        entity.getCreatedAt(),
        entity.getUpdatedAt(),
        entity.getFileId(),
        entity.getVersion(),
        entity.getUploadedAt());
  }
}
