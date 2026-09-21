package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserAffiliationImportSummaryResponse;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationData;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationImportSummary;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Bulk import of affiliations by natural keys (eppn / institution hai / group idSiSco), for
 * institutions importing their affiliations in batch (e.g. from a CSV export) without knowing the
 * internal external-user identifiers upfront.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping({"back-office/external-user-affiliations"})
public class ExternalUserAffiliationImportController {

  private final ExternalUserAffiliationService externalUserAffiliationService;

  @Value("${admin.token:}")
  private String adminToken;

  @PreAuthorize("hasAuthority('external-user-affiliation:import')")
  @PostMapping
  public ResponseEntity<ExternalUserAffiliationImportSummaryResponse> createAll(
      @RequestBody List<ExternalUserAffiliationData> affiliations,
      @RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    log.info("Importing {} external user affiliation(s) via admin endpoint", affiliations.size());
    ExternalUserAffiliationImportSummary summary =
        externalUserAffiliationService.createAll(affiliations);
    log.info(
        "External user affiliation import summary: {} created, {} existing, {} failed",
        summary.created().size(),
        summary.existing().size(),
        summary.failed().size());

    return ResponseEntity.ok(ExternalUserAffiliationImportSummaryResponse.from(summary));
  }

  private HttpStatus checkAdminToken(String token) {
    if (adminToken == null || adminToken.isBlank()) {
      return HttpStatus.SERVICE_UNAVAILABLE;
    }
    if (!adminToken.equals(token)) {
      return HttpStatus.FORBIDDEN;
    }
    return null;
  }
}
