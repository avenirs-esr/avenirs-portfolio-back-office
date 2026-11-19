package fr.avenirsesr.portfolio.backoffice.tool.application.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto.ToolDTO;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.common.data.application.adapter.dto.PageInfoDTO;
import fr.avenirsesr.portfolio.common.data.application.adapter.response.PagedResponse;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.file.application.adapter.dto.FileDTO;
import fr.avenirsesr.portfolio.common.institution.application.adapter.dto.InstitutionReferenceDTO;

public class ToolApplicationMapper {

  private ToolApplicationMapper() {}

  public static ToolDTO toToolDTO(Tool tool) {
    return new ToolDTO(
        tool.getId(),
        tool.getName(),
        tool.getDescription(),
        tool.getUrl(),
        tool.getType(),
        tool.getInstitution()
            .map(
                institution ->
                    new InstitutionReferenceDTO(institution.getId(), institution.getName()))
            .orElse(null),
        FileDTO.fromDomain(tool.getBanner()));
  }

  public static PagedResponse<ToolDTO> toPagedResponse(PagedResult<Tool> tools) {
    return new PagedResponse<>(
        tools.content().stream().map(ToolApplicationMapper::toToolDTO).toList(),
        PageInfoDTO.fromDomain(tools.pageInfo()));
  }
}
