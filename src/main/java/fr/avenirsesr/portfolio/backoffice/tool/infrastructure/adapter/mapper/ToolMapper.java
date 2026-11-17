package fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.mapper.InstitutionMapper;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionEntity;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.model.ToolEntity;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.mapper.Mapper;
import fr.avenirsesr.portfolio.common.file.domain.model.File;
import fr.avenirsesr.portfolio.common.file.infrastructure.adapter.mapper.FileMapper;
import fr.avenirsesr.portfolio.common.file.infrastructure.adapter.model.FileEntity;

public class ToolMapper implements Mapper<ToolEntity, Tool> {
  public static final ToolMapper INSTANCE = new ToolMapper();

  @Override
  public ToolEntity fromDomain(Tool tool) {
    FileEntity bannerEntity = tool.getBanner().map(FileMapper.INSTANCE::fromDomain).orElse(null);
    InstitutionEntity institutionEntity =
        tool.getInstitution().map(InstitutionMapper.INSTANCE::fromDomain).orElse(null);

    return ToolEntity.of(
        tool.getId(),
        tool.getName(),
        tool.getDescription(),
        tool.getUrl(),
        bannerEntity,
        institutionEntity,
        tool.getCreatedAt(),
        tool.getUpdatedAt());
  }

  @Override
  public Tool toDomain(ToolEntity entity) {
    File banner =
        entity.getBanner() == null ? null : FileMapper.INSTANCE.toDomain(entity.getBanner());
    Institution institution =
        entity.getInstitution() == null
            ? null
            : InstitutionMapper.INSTANCE.toDomain(entity.getInstitution());

    return Tool.toDomain(
        entity.getId(),
        entity.getName(),
        entity.getDescription(),
        entity.getUrl(),
        banner,
        institution,
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
