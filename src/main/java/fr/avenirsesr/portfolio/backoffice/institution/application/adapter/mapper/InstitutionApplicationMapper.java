package fr.avenirsesr.portfolio.backoffice.institution.application.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.common.institution.application.adapter.dto.InstitutionDTO;

public class InstitutionApplicationMapper {

  private InstitutionApplicationMapper() {}

  public static InstitutionDTO toInstitutionDTO(Institution institution) {
    return new InstitutionDTO(
        institution.getId(),
        institution.getName(),
        institution.getType(),
        institution.getParent().map(Institution::getId).orElse(null));
  }
}
