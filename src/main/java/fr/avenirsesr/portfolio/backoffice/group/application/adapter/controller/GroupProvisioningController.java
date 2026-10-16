package fr.avenirsesr.portfolio.backoffice.group.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.group.application.adapter.dto.GroupImportSummaryResponse;
import fr.avenirsesr.portfolio.backoffice.group.application.adapter.dto.GroupResponse;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupData;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupImportSummary;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.input.GroupService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Write-only endpoints letting an institution provision its own group referential, typically from
 * an automated feed on its side, hence the API key authentication. Reads are exposed to the
 * logged-in staff by {@link GroupQueryController}.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("back-office/admin/groups")
public class GroupProvisioningController {

  private final GroupService groupService;

  @Value("${admin.token:}")
  private String adminToken;

  @PreAuthorize("hasAuthority('group:import')")
  @PostMapping
  public ResponseEntity<GroupImportSummaryResponse> createAll(
      @RequestBody List<GroupData> groups, @RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    log.info("Importing {} group(s) via admin endpoint", groups.size());
    GroupImportSummary summary = groupService.createAll(groups);
    log.info(
        "Group import summary: {} created, {} updated, {} failed",
        summary.created().size(),
        summary.updated().size(),
        summary.failed().size());
    return ResponseEntity.ok(GroupImportSummaryResponse.from(summary));
  }

  @PreAuthorize("hasAuthority('group:update')")
  @PutMapping
  public ResponseEntity<List<GroupResponse>> updateAll(
      @RequestBody List<GroupData> groups, @RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    log.info("Updating {} group(s) via admin endpoint", groups.size());
    List<Group> updated = groupService.updateAll(groups);
    return ResponseEntity.ok(updated.stream().map(GroupResponse::from).toList());
  }

  @PreAuthorize("hasAuthority('group:delete')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID id, @RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    groupService.delete(id);
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
