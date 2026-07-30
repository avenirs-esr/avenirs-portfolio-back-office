package fr.avenirsesr.portfolio.backoffice.group.domain.model;

import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import java.util.List;

public record GroupImportSummary(
    List<Group> created, List<Group> updated, List<GroupImportFailure> failed) {}
