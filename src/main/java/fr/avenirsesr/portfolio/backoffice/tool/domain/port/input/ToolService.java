package fr.avenirsesr.portfolio.backoffice.tool.domain.port.input;

import fr.avenirsesr.portfolio.backoffice.tool.domain.model.EToolType;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.file.domain.model.File;
import fr.avenirsesr.portfolio.common.file.domain.model.FileContent;
import java.util.List;
import java.util.UUID;

public interface ToolService {
  Tool createNationalTool(String name, String description, String url);

  Tool createLocalTool(String name, String description, String url, UUID institutionId);

  Tool updateNationalTool(UUID toolId, String name, String description, String url);

  Tool updateLocalTool(UUID toolId, String name, String description, String url);

  File uploadNationalToolBanner(
      UUID toolId, String fileName, String mimeType, long size, FileContent content);

  File uploadLocalToolBanner(
      UUID toolId, String fileName, String mimeType, long size, FileContent content);

  void deleteNationalToolBanner(UUID toolId);

  void deleteLocalToolBanner(UUID toolId);

  void deleteNationalTools(List<UUID> toolsIds);

  void deleteLocalTools(List<UUID> toolsIds);

  PagedResult<Tool> findNationalTools(PageCriteria pageCriteria);

  PagedResult<Tool> findLocalTools(EToolType type, PageCriteria pageCriteria);

  Tool getNationalTool(UUID toolId);

  Tool getLocalTool(UUID toolId);

  PagedResult<Tool> findAvailableTools(EToolType type, PageCriteria pageCriteria);

  Tool getAvailableTool(UUID toolId);
}
