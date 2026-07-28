package fr.avenirsesr.portfolio.backoffice.group.application.adapter.dto;

import fr.avenirsesr.portfolio.common.group.domain.model.Group;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record GroupResponse(
    UUID id,
    String name,
    String idSiSco,
    UUID institutionId,
    String codeSise,
    LocalDate startDate,
    LocalDate endDate,
    EGroupType type,
    String parentIdSiSco,
    Instant createdAt,
    Instant updatedAt) {

  public static GroupResponse from(Group group) {
    return new GroupResponse(
        group.getId(),
        group.getName(),
        group.getIdSiSco(),
        group.getInstitution().getId(),
        group.getCodeSise(),
        group.getStartDate(),
        group.getEndDate(),
        group.getType(),
        group.getParent().map(Group::getIdSiSco).orElse(null),
        group.getCreatedAt(),
        group.getUpdatedAt());
  }
}
