package fr.avenirsesr.portfolio.backoffice.externaluser.domain.model;

import java.util.List;

public record ExternalUserImportSummary(
    List<ExternalUser> created,
    List<ExternalUser> updated,
    List<ExternalUserImportFailure> failed) {}
