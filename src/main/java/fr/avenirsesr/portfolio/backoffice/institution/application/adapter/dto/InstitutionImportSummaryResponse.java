package fr.avenirsesr.portfolio.backoffice.institution.application.adapter.dto;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionImportSummary;
import java.util.List;

public record InstitutionImportSummaryResponse(
    int createdCount,
    int updatedCount,
    int failedCount,
    List<InstitutionResponse> created,
    List<InstitutionResponse> updated,
    List<InstitutionImportFailureResponse> failed) {

  public static InstitutionImportSummaryResponse from(InstitutionImportSummary summary) {
    List<InstitutionResponse> created =
        summary.created().stream().map(InstitutionResponse::from).toList();
    List<InstitutionResponse> updated =
        summary.updated().stream().map(InstitutionResponse::from).toList();
    List<InstitutionImportFailureResponse> failed =
        summary.failed().stream().map(InstitutionImportFailureResponse::from).toList();

    return new InstitutionImportSummaryResponse(
        created.size(), updated.size(), failed.size(), created, updated, failed);
  }
}
