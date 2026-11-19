package fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.tool.domain.model.EToolType;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.backoffice.tool.domain.port.input.ToolService;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.file.domain.model.File;
import fr.avenirsesr.portfolio.common.file.domain.model.FileContent;
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

  @Override
  @Transactional
  public File uploadNationalToolBanner(
      UUID toolId, String fileName, String mimeType, long size, FileContent content) {
    return delegate.uploadNationalToolBanner(toolId, fileName, mimeType, size, content);
  }

  @Override
  @Transactional
  public File uploadLocalToolBanner(
      UUID toolId, String fileName, String mimeType, long size, FileContent content) {
    return delegate.uploadLocalToolBanner(toolId, fileName, mimeType, size, content);
  }

  @Override
  @Transactional
  public void deleteNationalToolBanner(UUID toolId) {
    delegate.deleteNationalToolBanner(toolId);
  }

  @Override
  @Transactional
  public void deleteLocalToolBanner(UUID toolId) {
    delegate.deleteLocalToolBanner(toolId);
  }

  @Override
  @Transactional
  public void deleteNationalTool(UUID toolId) {
    delegate.deleteNationalTool(toolId);
  }

  @Override
  @Transactional
  public void deleteLocalTool(UUID toolId) {
    delegate.deleteLocalTool(toolId);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResult<Tool> findNationalTools(PageCriteria pageCriteria) {
    return delegate.findNationalTools(pageCriteria);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResult<Tool> findLocalTools(EToolType type, PageCriteria pageCriteria) {
    return delegate.findLocalTools(type, pageCriteria);
  }

  @Override
  @Transactional(readOnly = true)
  public Tool getNationalTool(UUID toolId) {
    return delegate.getNationalTool(toolId);
  }

  @Override
  @Transactional(readOnly = true)
  public Tool getLocalTool(UUID toolId) {
    return delegate.getLocalTool(toolId);
  }
}
