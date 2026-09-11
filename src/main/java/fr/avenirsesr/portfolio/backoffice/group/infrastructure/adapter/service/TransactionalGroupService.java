package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupData;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupImportSummary;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.input.GroupService;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Wraps the domain {@link GroupService} to add transaction boundaries around bulk operations,
 * keeping the Spring transaction dependency out of the domain layer.
 */
@AllArgsConstructor
public class TransactionalGroupService implements GroupService {
  private final GroupService delegate;

  @Override
  public Group create(
      String name,
      String idSiSco,
      UUID institutionId,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      String parentIdSiSco) {
    return delegate.create(
        name, idSiSco, institutionId, codeSise, startDate, endDate, type, parentIdSiSco);
  }

  @Override
  @Transactional
  public GroupImportSummary createAll(List<GroupData> groups) {
    return delegate.createAll(groups);
  }

  @Override
  public Group update(
      String idSiSco,
      String name,
      UUID institutionId,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      String parentIdSiSco) {
    return delegate.update(
        idSiSco, name, institutionId, codeSise, startDate, endDate, type, parentIdSiSco);
  }

  @Override
  @Transactional
  public List<Group> updateAll(List<GroupData> groups) {
    return delegate.updateAll(groups);
  }

  @Override
  public List<Group> findAll(
      UUID institutionId, UUID parentId, EGroupType type, LocalDate startDate, LocalDate endDate) {
    return delegate.findAll(institutionId, parentId, type, startDate, endDate);
  }

  @Override
  public Group findById(UUID id) {
    return delegate.findById(id);
  }

  @Override
  public Group findProgramOf(UUID groupId) {
    return delegate.findProgramOf(groupId);
  }

  @Override
  public void delete(UUID id) {
    delegate.delete(id);
  }
}
