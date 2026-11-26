package fr.avenirsesr.portfolio.backoffice.institution.application.adapter.mapper;

import fr.avenirsesr.portfolio.backoffice.institution.application.adapter.dto.InstitutionAdministratorDTO;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import java.util.List;

public class InstitutionAdministratorApplicationMapper {

  private InstitutionAdministratorApplicationMapper() {}
  ;

  public static InstitutionAdministratorDTO toDto(Institution institution) {
    return new InstitutionAdministratorDTO(
        institution.getId(),
        institution.getName(),
        institution.getSigle(),
        institution.getUai(),
        institution.getType());
  }

  public static List<InstitutionAdministratorDTO> toDTOs(List<Institution> institutions) {
    return institutions.stream().map(InstitutionAdministratorApplicationMapper::toDto).toList();
  }
}
