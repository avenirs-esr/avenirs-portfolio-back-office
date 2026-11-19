package fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserAffiliationImportSummaryResponse;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationImportSummary;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.dto.ExternalUserImportSummaryResponse;
import fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.dto.ProvisionExternalUserRequest;
import fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.dto.ProvisionExternalUserResponse;
import fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.mapper.ExternalUserApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUserImportSummary;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.input.ExternalUserService;
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
 * Provisions an external user and its affiliations in a single call, typically when an account is
 * first seen through a federation login.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("back-office/admin/external-users")
public class ExternalUserProvisioningController {

  private final ExternalUserService externalUserService;
  private final ExternalUserAffiliationService externalUserAffiliationService;

  @Value("${admin.token:}")
  private String adminToken;

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
