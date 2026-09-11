package fr.avenirsesr.portfolio.backoffice.group.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.group.application.adapter.dto.GroupImportSummaryResponse;
import fr.avenirsesr.portfolio.backoffice.group.application.adapter.dto.GroupResponse;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupData;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupImportSummary;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.input.GroupService;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("back-office/admin/groups")
public class GroupController {

  private final GroupService groupService;

  @Value("${admin.token:}")
  private String adminToken;

  @PreAuthorize("hasAuthority('group:read')")
  @GetMapping
  public ResponseEntity<List<GroupResponse>> findAll(
      @RequestParam(required = false) UUID institutionId,
      @RequestParam(required = false) UUID parentId,
      @RequestParam(required = false) EGroupType type,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate,
      @RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    List<Group> groups = groupService.findAll(institutionId, parentId, type, startDate, endDate);
    return ResponseEntity.ok(groups.stream().map(GroupResponse::from).toList());
  }

  @PreAuthorize("hasAuthority('group:read')")
  @GetMapping("/{id}")
  public ResponseEntity<GroupResponse> findById(
      @PathVariable UUID id, @RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    Group group = groupService.findById(id);
    return ResponseEntity.ok(GroupResponse.from(group));
  }

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
