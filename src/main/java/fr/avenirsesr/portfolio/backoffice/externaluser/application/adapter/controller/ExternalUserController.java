package fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.dto.ExternalUserImportSummaryResponse;
import fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.mapper.ExternalUserApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUserData;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUserImportSummary;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.input.ExternalUserService;
import fr.avenirsesr.portfolio.common.user.application.adapter.dto.ExternalUserDTO;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping({"back-office/external-users"})
public class ExternalUserController {

  private final ExternalUserService externalUserService;

  @Value("${admin.token:}")
  private String adminToken;

  @GetMapping
  public ResponseEntity<List<ExternalUserDTO>> getExternalUsers(
      @RequestParam(required = false) UUID institutionId,
      @RequestParam(required = false) UUID groupId) {
    log.debug("Getting external users for institutionId: {}, groupId: {}", institutionId, groupId);

    List<ExternalUser> externalUsers =
        externalUserService.getAllExternalUsers(institutionId, groupId);

    return ResponseEntity.ok(
        externalUsers.stream().map(ExternalUserApplicationMapper::toExternalUserDTO).toList());
  }

  @GetMapping(path = "/{id}")
  public ResponseEntity<ExternalUserDTO> getExternalUserById(@PathVariable UUID id) {
    log.debug("Getting external user for id: {}", id);

    return externalUserService
        .getById(id)
        .map(ExternalUserApplicationMapper::toExternalUserDTO)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping(path = "/eppn/{eppn}")
  public ResponseEntity<ExternalUserDTO> getExternalUserByEppn(@PathVariable String eppn) {
    log.debug("Getting external user for eppn: {}", eppn);

    return externalUserService
        .getByEppn(eppn)
        .map(ExternalUserApplicationMapper::toExternalUserDTO)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PatchMapping(path = "/eppn/{eppn}/activate")
  public ResponseEntity<ExternalUserDTO> activateExternalUserByEppn(@PathVariable String eppn) {
    log.debug("Activating external user for eppn: {}", eppn);

    ExternalUser externalUser = externalUserService.activateByEppn(eppn);

    return ResponseEntity.ok(ExternalUserApplicationMapper.toExternalUserDTO(externalUser));
  }

  @PostMapping
  public ResponseEntity<ExternalUserImportSummaryResponse> createAll(
      @RequestBody List<ExternalUserData> externalUsers,
      @RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    log.info("Importing {} external user(s) via admin endpoint", externalUsers.size());
    ExternalUserImportSummary summary = externalUserService.createAll(externalUsers);
    log.info(
        "External user import summary: {} created, {} updated, {} failed",
        summary.created().size(),
        summary.updated().size(),
        summary.failed().size());
    return ResponseEntity.ok(ExternalUserImportSummaryResponse.from(summary));
  }

  @PutMapping
  public ResponseEntity<List<ExternalUserDTO>> updateAll(
      @RequestBody List<ExternalUserData> externalUsers,
      @RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    log.info("Updating {} external user(s) via admin endpoint", externalUsers.size());
    List<ExternalUser> updated = externalUserService.updateAll(externalUsers);
    return ResponseEntity.ok(
        updated.stream().map(ExternalUserApplicationMapper::toExternalUserDTO).toList());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID id, @RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    externalUserService.delete(id);
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
