package fr.avenirsesr.portfolio.backoffice.tool.application.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto.ToolDTO;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;

public class ToolApplicationMapper {

  private ToolApplicationMapper() {}

  public static ToolDTO toToolDTO(Tool tool) {
    return new ToolDTO(
        tool.getId(),
        tool.getName(),
        tool.getDescription(),
        tool.getUrl(),
        tool.getType(),
        tool.getInstitution().map(Institution::getId).orElse(null));
  }
}
