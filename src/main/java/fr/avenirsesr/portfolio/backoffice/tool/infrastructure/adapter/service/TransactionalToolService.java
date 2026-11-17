package fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.backoffice.tool.domain.port.input.ToolService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
public class TransactionalToolService implements ToolService {
  private final ToolService delegate;

  @Override
  @Transactional
  public Tool createNationalTool(String name, String description, String url) {
    return delegate.createNationalTool(name, description, url);
  }

  @Override
  @Transactional
  public Tool createLocalTool(String name, String description, String url, UUID institutionId) {
    return delegate.createLocalTool(name, description, url, institutionId);
  }

  @Override
  @Transactional
  public Tool updateNationalTool(UUID toolId, String name, String description, String url) {
    return delegate.updateNationalTool(toolId, name, description, url);
  }

  @Override
  @Transactional
  public Tool updateLocalTool(UUID toolId, String name, String description, String url) {
    return delegate.updateLocalTool(toolId, name, description, url);
  }
}
