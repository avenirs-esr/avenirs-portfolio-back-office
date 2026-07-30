package fr.avenirsesr.portfolio.backoffice.institution.domain.model;

import java.util.List;

public record InstitutionImportSummary(
    List<Institution> created, List<Institution> updated, List<InstitutionImportFailure> failed) {}
