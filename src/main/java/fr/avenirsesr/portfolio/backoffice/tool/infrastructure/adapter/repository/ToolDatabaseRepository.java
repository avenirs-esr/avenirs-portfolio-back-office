package fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.backoffice.tool.domain.port.output.repository.ToolRepository;
import fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.mapper.ToolMapper;
import fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.model.ToolEntity;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.repository.GenericJpaRepositoryAdapter;
import org.springframework.stereotype.Component;

@Component
public class ToolDatabaseRepository extends GenericJpaRepositoryAdapter<Tool, ToolEntity>
    implements ToolRepository {

  public ToolDatabaseRepository(ToolJpaRepository toolJpaRepository) {
    super(toolJpaRepository, toolJpaRepository, ToolEntity.class, ToolMapper.INSTANCE);
  }
}
