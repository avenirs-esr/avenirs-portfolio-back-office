package fr.avenirsesr.portfolio.backoffice.group.domain.port.input;

import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupData;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupImportSummary;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.enums.EGroupType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GroupService {
  Group create(
      String name,
      String idSiSco,
      UUID institutionId,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      String parentIdSiSco);

  GroupImportSummary createAll(List<GroupData> groups);

  Group update(
      String idSiSco,
      String name,
      UUID institutionId,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      String parentIdSiSco);

  List<Group> updateAll(List<GroupData> groups);

  List<Group> findAll();

  Group findById(UUID id);

  void delete(UUID id);
}
