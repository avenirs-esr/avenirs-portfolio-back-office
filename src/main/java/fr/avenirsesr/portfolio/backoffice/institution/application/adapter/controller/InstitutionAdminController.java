package fr.avenirsesr.portfolio.backoffice.institution.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.institution.application.adapter.dto.InstitutionImportSummaryResponse;
import fr.avenirsesr.portfolio.backoffice.institution.application.adapter.dto.InstitutionResponse;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionData;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionImportSummary;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionService;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
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
@RequestMapping("back-office/admin/institutions")
public class InstitutionAdminController {

  private final InstitutionService institutionService;

  @Value("${admin.token:}")
  private String adminToken;

  @PreAuthorize("hasAnyAuthority('primary-establishment:read','secondary-establishment:read')")
  @GetMapping
  public ResponseEntity<List<InstitutionResponse>> findAll(
      @RequestParam(required = false) UUID parentId,
      @RequestParam(required = false) EInstitutionType type,
      @RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    List<Institution> institutions = institutionService.findAll(parentId, type);
    return ResponseEntity.ok(institutions.stream().map(InstitutionResponse::from).toList());
  }

  @PreAuthorize("hasAnyAuthority('primary-establishment:read','secondary-establishment:read')")
  @GetMapping("/{id}")
  public ResponseEntity<InstitutionResponse> findById(
      @PathVariable UUID id, @RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    Institution institution = institutionService.findById(id);
    return ResponseEntity.ok(InstitutionResponse.from(institution));
  }

  @PreAuthorize("hasAnyAuthority('primary-establishment:create','secondary-establishment:create')")
  @PostMapping
  public ResponseEntity<InstitutionImportSummaryResponse> createAll(
      @RequestBody List<InstitutionData> institutions,
      @RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    log.info("Importing {} institution(s) via admin endpoint", institutions.size());
    InstitutionImportSummary summary = institutionService.createAll(institutions);
    log.info(
        "Institution import summary: {} created, {} updated, {} failed",
        summary.created().size(),
        summary.updated().size(),
        summary.failed().size());
    return ResponseEntity.ok(InstitutionImportSummaryResponse.from(summary));
  }

  @PreAuthorize("hasAnyAuthority('primary-establishment:update','secondary-establishment:update')")
  @PutMapping
  public ResponseEntity<List<InstitutionResponse>> updateAll(
      @RequestBody List<InstitutionData> institutions,
      @RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    log.info("Updating {} institution(s) via admin endpoint", institutions.size());
    List<Institution> updated = institutionService.updateAll(institutions);
    return ResponseEntity.ok(updated.stream().map(InstitutionResponse::from).toList());
  }

  @PreAuthorize("hasAnyAuthority('primary-establishment:delete','secondary-establishment:delete')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID id, @RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    institutionService.delete(id);
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
