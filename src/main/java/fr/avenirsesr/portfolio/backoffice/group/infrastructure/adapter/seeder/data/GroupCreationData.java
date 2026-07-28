package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.seeder.data;

import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import java.time.LocalDate;
import java.util.UUID;

public record GroupCreationData(
    String name,
    String idSiSco,
    UUID institutionId,
    String codeSise,
    LocalDate startDate,
    LocalDate endDate,
    EGroupType type,
    String parentIdSiSco) {}
