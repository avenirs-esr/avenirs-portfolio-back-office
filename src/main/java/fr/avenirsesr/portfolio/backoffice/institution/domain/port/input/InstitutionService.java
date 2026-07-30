package fr.avenirsesr.portfolio.backoffice.institution.domain.port.input;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionData;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionImportSummary;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.enums.EInstitutionType;
import java.util.List;
import java.util.UUID;

public interface InstitutionService {
  Institution create(
      String name, String hai, String siret, String siren, EInstitutionType type, String parentHai);

  InstitutionImportSummary createAll(List<InstitutionData> institutions);

  Institution update(
      String hai, String name, String siret, String siren, EInstitutionType type, String parentHai);

  List<Institution> updateAll(List<InstitutionData> institutions);

  List<Institution> findAll();

  Institution findById(UUID id);

  void delete(UUID id);
}
