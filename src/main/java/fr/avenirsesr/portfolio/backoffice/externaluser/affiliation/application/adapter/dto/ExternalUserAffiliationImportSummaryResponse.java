package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.mapper.ExternalUserAffiliationApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationImportSummary;
import java.util.List;

public record ExternalUserAffiliationImportSummaryResponse(
    int createdCount,
    int existingCount,
    int failedCount,
    List<ExternalUserAffiliationDTO> created,
    List<ExternalUserAffiliationDTO> existing,
    List<ExternalUserAffiliationImportFailureResponse> failed) {

  public static ExternalUserAffiliationImportSummaryResponse from(
      ExternalUserAffiliationImportSummary summary) {
    List<ExternalUserAffiliationDTO> created =
        summary.created().stream()
            .map(ExternalUserAffiliationApplicationMapper::toExternalUserAffiliationDTO)
            .toList();
    List<ExternalUserAffiliationDTO> existing =
        summary.existing().stream()
            .map(ExternalUserAffiliationApplicationMapper::toExternalUserAffiliationDTO)
            .toList();
    List<ExternalUserAffiliationImportFailureResponse> failed =
        summary.failed().stream().map(ExternalUserAffiliationImportFailureResponse::from).toList();

    return new ExternalUserAffiliationImportSummaryResponse(
        created.size(), existing.size(), failed.size(), created, existing, failed);
  }
}
