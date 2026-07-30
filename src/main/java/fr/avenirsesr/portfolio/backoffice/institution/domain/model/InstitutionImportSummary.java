package fr.avenirsesr.portfolio.backoffice.institution.domain.model;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import java.util.List;

public record InstitutionImportSummary(
    List<Institution> created, List<Institution> updated, List<InstitutionImportFailure> failed) {}
