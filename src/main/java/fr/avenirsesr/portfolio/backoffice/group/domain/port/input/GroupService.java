package fr.avenirsesr.portfolio.backoffice.group.domain.port.input;

import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupData;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupImportSummary;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GroupService {
  Group create(
      String name,
      String idSISco,
      String institutionUAI,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      String parentIdSISco);

  GroupImportSummary createAll(List<GroupData> groups);

  Group update(
      String idSISco,
      String name,
      String institutionUAI,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      String parentIdSISco);

  List<Group> updateAll(List<GroupData> groups);

  /** Lists the groups of the primary institution the logged-in staff belongs to. */
  PagedResult<Group> findAllOfStaffPrimaryInstitution(
      String keyword, EGroupType type, PageCriteria pageCriteria);

  Group findById(UUID id);

  Group findProgramOf(UUID groupId);

  boolean staffHasAccess(List<UUID> affiliatedIds, List<UUID> targetIds);

  List<UUID> studentAccessibleIds(List<UUID> affiliatedIds);

  void delete(UUID id);
}
