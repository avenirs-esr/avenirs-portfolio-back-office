package fr.avenirsesr.portfolio.backoffice.institution.domain.port.input;

import fr.avenirsesr.portfolio.common.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;

public interface InstitutionService {
  Institution create(
      String name, String hai, String siret, String siren, EInstitutionType type, String parentHai);
}
