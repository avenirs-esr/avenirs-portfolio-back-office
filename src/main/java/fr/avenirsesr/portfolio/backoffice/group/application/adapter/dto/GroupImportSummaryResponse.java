package fr.avenirsesr.portfolio.backoffice.group.application.adapter.dto;

import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupImportSummary;
import java.util.List;

public record GroupImportSummaryResponse(
    int createdCount,
    int updatedCount,
    int failedCount,
    List<GroupResponse> created,
    List<GroupResponse> updated,
    List<GroupImportFailureResponse> failed) {

  public static GroupImportSummaryResponse from(GroupImportSummary summary) {
    List<GroupResponse> created = summary.created().stream().map(GroupResponse::from).toList();
    List<GroupResponse> updated = summary.updated().stream().map(GroupResponse::from).toList();
    List<GroupImportFailureResponse> failed =
        summary.failed().stream().map(GroupImportFailureResponse::from).toList();

    return new GroupImportSummaryResponse(
        created.size(), updated.size(), failed.size(), created, updated, failed);
  }
}
