package fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.dto;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.mapper.ExternalUserApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUserImportSummary;
import fr.avenirsesr.portfolio.common.user.application.adapter.dto.ExternalUserDTO;
import java.util.List;

public record ExternalUserImportSummaryResponse(
    int createdCount,
    int updatedCount,
    int failedCount,
    List<ExternalUserDTO> created,
    List<ExternalUserDTO> updated,
    List<ExternalUserImportFailureResponse> failed) {

  public static ExternalUserImportSummaryResponse from(
      ExternalUserImportSummary summary,
      ExternalUserAffiliationService externalUserAffiliationService) {
    List<ExternalUserDTO> created =
        summary.created().stream()
            .map(externalUser -> toExternalUserDTO(externalUser, externalUserAffiliationService))
            .toList();
    List<ExternalUserDTO> updated =
        summary.updated().stream()
            .map(externalUser -> toExternalUserDTO(externalUser, externalUserAffiliationService))
            .toList();
    List<ExternalUserImportFailureResponse> failed =
        summary.failed().stream().map(ExternalUserImportFailureResponse::from).toList();

    return new ExternalUserImportSummaryResponse(
        created.size(), updated.size(), failed.size(), created, updated, failed);
  }

  private static ExternalUserDTO toExternalUserDTO(
      ExternalUser externalUser, ExternalUserAffiliationService externalUserAffiliationService) {
    return ExternalUserApplicationMapper.toExternalUserDTO(
        externalUser, externalUserAffiliationService.getAffiliations(externalUser.getId()));
  }
}
