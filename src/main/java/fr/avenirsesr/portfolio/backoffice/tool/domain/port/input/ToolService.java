package fr.avenirsesr.portfolio.backoffice.tool.domain.port.input;

import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.common.file.domain.model.File;
import fr.avenirsesr.portfolio.common.file.domain.model.FileContent;
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
}
