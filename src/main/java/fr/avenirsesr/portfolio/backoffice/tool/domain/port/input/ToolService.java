package fr.avenirsesr.portfolio.backoffice.tool.domain.port.input;

import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import java.util.UUID;

public interface ToolService {
  Tool createNationalTool(String name, String description, String url);

  Tool createLocalTool(String name, String description, String url, UUID institutionId);
}
