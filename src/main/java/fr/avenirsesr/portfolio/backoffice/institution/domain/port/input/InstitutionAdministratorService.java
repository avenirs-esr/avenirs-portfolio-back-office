package fr.avenirsesr.portfolio.backoffice.institution.domain.port.input;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import java.util.List;
import java.util.UUID;

public interface InstitutionAdministratorService {

  List<Institution> designate(UUID userId, List<UUID> institutionIds);
}
