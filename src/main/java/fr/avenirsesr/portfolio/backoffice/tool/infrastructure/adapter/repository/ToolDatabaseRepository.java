package fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.repository;

import fr.avenirsesr.portfolio.backoffice.tool.domain.model.EToolType;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.backoffice.tool.domain.port.output.repository.ToolRepository;
import fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.mapper.ToolMapper;
import fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.model.ToolEntity;
import fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.specification.ToolSpecification;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.repository.GenericJpaRepositoryAdapter;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class ToolDatabaseRepository extends GenericJpaRepositoryAdapter<Tool, ToolEntity>
    implements ToolRepository {

  public ToolDatabaseRepository(ToolJpaRepository toolJpaRepository) {
    super(toolJpaRepository, toolJpaRepository, ToolEntity.class, ToolMapper.INSTANCE);
  }

  @Override
  public PagedResult<Tool> findAllNational(PageCriteria pageCriteria) {
    return findAll(ToolSpecification.isNational(), sortedByName(pageCriteria));
  }

  @Override
  public PagedResult<Tool> findAllOfInstitutions(
      Collection<UUID> institutionIds, EToolType type, PageCriteria pageCriteria) {
    return findAll(
        ToolSpecification.withInstitutionIds(institutionIds).and(ToolSpecification.withType(type)),
        sortedByName(pageCriteria));
  }

  @Override
  public PagedResult<Tool> findAllNationalOrOfInstitutions(
      Collection<UUID> institutionIds, EToolType type, PageCriteria pageCriteria) {
    return findAll(
        ToolSpecification.isNational()
            .or(ToolSpecification.withInstitutionIds(institutionIds))
            .and(ToolSpecification.withType(type)),
        sortedByName(pageCriteria));
  }

  private static PageRequest sortedByName(PageCriteria pageCriteria) {
    return PageRequest.of(
        pageCriteria.page(),
        pageCriteria.pageSize(),
        Sort.by(Sort.Order.asc("name").ignoreCase(), Sort.Order.asc("id")));
  }
}
