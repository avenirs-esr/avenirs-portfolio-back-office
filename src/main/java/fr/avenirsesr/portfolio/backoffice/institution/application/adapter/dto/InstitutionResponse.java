package fr.avenirsesr.portfolio.backoffice.institution.application.adapter.dto;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import java.time.Instant;
import java.util.UUID;

public record InstitutionResponse(
    UUID id,
    String name,
    String hai,
    String siret,
    String siren,
    EInstitutionType type,
    String parentHai,
    Instant createdAt,
    Instant updatedAt) {

  public static InstitutionResponse from(Institution institution) {
    return new InstitutionResponse(
        institution.getId(),
        institution.getName(),
        institution.getHai(),
        institution.getSiret(),
        institution.getSiren(),
        institution.getType(),
        institution.getParent().map(Institution::getHai).orElse(null),
        institution.getCreatedAt(),
        institution.getUpdatedAt());
  }
}
