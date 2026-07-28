package fr.avenirsesr.portfolio.backoffice.group.domain.port.input;

import fr.avenirsesr.portfolio.common.group.domain.model.Group;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import java.time.LocalDate;
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
}
