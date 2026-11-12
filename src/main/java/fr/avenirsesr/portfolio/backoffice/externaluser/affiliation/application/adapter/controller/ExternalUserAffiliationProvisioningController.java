package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.AddExternalUserAffiliationRequest;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserAffiliationDTO;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserAffiliationImportSummaryResponse;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.mapper.ExternalUserAffiliationApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationData;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationImportSummary;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.dto.ExternalUserImportSummaryResponse;
import fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.dto.ProvisionExternalUserRequest;
import fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.dto.ProvisionExternalUserResponse;
import fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.mapper.ExternalUserApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUserImportSummary;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.input.ExternalUserService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Write-only endpoints letting an institution provision the affiliations of its own users,
 * typically from an automated feed on its side, hence the API key authentication. Reads are exposed
 * to the logged-in staff by {@link ExternalUserAffiliationQueryController}.
 *
 * <p>The bulk endpoint resolves its payload by natural keys (eppn / institution uai / group
 * idSISco), for institutions importing their affiliations in batch (e.g. from a CSV export) without
 * knowing the internal identifiers upfront.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("back-office/admin/external-users")
public class ExternalUserAffiliationProvisioningController {

  private final ExternalUserAffiliationService externalUserAffiliationService;
  private final ExternalUserService externalUserService;

  @Value("${admin.token:}")
  private String adminToken;

  @PreAuthorize("hasAuthority('external-user-affiliation:import')")
  @PostMapping("/affiliations")
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

  @PreAuthorize("hasAuthority('external-user:import')")
  @PostMapping("/provision")
  public ResponseEntity<ProvisionExternalUserResponse> provision(
      @RequestBody ProvisionExternalUserRequest request,
      @RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    log.info("Provisioning external user {} via admin endpoint", request.eppn());
    ExternalUserImportSummary userSummary =
        externalUserService.createAll(
            List.of(ExternalUserApplicationMapper.toExternalUserData(request)));
    ExternalUserAffiliationImportSummary affiliationSummary =
        externalUserAffiliationService.createAll(
            ExternalUserApplicationMapper.toExternalUserAffiliationData(request));

    ProvisionExternalUserResponse response =
        new ProvisionExternalUserResponse(
            ExternalUserImportSummaryResponse.from(userSummary, externalUserAffiliationService),
            ExternalUserAffiliationImportSummaryResponse.from(affiliationSummary));

    return userSummary.failed().isEmpty() && affiliationSummary.failed().isEmpty()
        ? ResponseEntity.ok(response)
        : ResponseEntity.unprocessableEntity().body(response);
  }

  @PreAuthorize("hasAuthority('external-user:update')")
  @PostMapping("/{externalUserId}/affiliations")
  public ResponseEntity<ExternalUserAffiliationDTO> addAffiliation(
      @PathVariable UUID externalUserId,
      @RequestBody AddExternalUserAffiliationRequest request,
      @RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    log.debug(
        "Adding affiliation for external user: {} to institution: {}, group: {}",
        externalUserId,
        request.institutionId(),
        request.groupId());

    ExternalUserAffiliation affiliation =
        externalUserAffiliationService.addAffiliation(
            externalUserId, request.institutionId(), request.groupId(), request.category());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ExternalUserAffiliationApplicationMapper.toExternalUserAffiliationDTO(affiliation));
  }

  @PreAuthorize("hasAuthority('external-user:update')")
  @DeleteMapping("/{externalUserId}/affiliations/{affiliationId}")
  public ResponseEntity<Void> removeAffiliation(
      @PathVariable UUID externalUserId,
      @PathVariable UUID affiliationId,
      @RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    log.debug("Removing affiliation: {} for external user: {}", affiliationId, externalUserId);

    externalUserAffiliationService.removeAffiliation(externalUserId, affiliationId);

    return ResponseEntity.noContent().build();
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
