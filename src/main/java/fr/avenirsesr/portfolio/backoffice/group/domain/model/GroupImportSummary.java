package fr.avenirsesr.portfolio.backoffice.group.domain.model;

import java.util.List;

public record GroupImportSummary(
    List<Group> created, List<Group> updated, List<GroupImportFailure> failed) {}
