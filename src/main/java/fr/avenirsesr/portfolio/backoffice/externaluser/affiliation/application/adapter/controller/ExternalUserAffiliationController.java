package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.AddExternalUserAffiliationRequest;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.ExternalUserAffiliationDTO;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.mapper.ExternalUserAffiliationApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping({"back-office/external-users/{externalUserId}/affiliations"})
public class ExternalUserAffiliationController {

  private final ExternalUserAffiliationService externalUserAffiliationService;

  @PreAuthorize("hasAuthority('external-user:read')")
  @GetMapping
  public ResponseEntity<List<ExternalUserAffiliationDTO>> getAffiliations(
      @PathVariable UUID externalUserId) {
    log.debug("Getting affiliations for external user: {}", externalUserId);

    List<ExternalUserAffiliationDTO> affiliations =
        externalUserAffiliationService.getAffiliations(externalUserId).stream()
            .map(ExternalUserAffiliationApplicationMapper::toExternalUserAffiliationDTO)
            .toList();

    return ResponseEntity.ok(affiliations);
  }

  @PreAuthorize("hasAuthority('external-user:update')")
  @PostMapping
  public ResponseEntity<ExternalUserAffiliationDTO> addAffiliation(
      @PathVariable UUID externalUserId, @RequestBody AddExternalUserAffiliationRequest request) {
    log.debug(
        "Adding affiliation for external user: {} to institution: {}, group: {}",
        externalUserId,
        request.institutionId(),
        request.groupId());

    var affiliation =
        externalUserAffiliationService.addAffiliation(
            externalUserId, request.institutionId(), request.groupId(), request.category());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ExternalUserAffiliationApplicationMapper.toExternalUserAffiliationDTO(affiliation));
  }

  @PreAuthorize("hasAuthority('external-user:update')")
  @DeleteMapping("/{affiliationId}")
  public ResponseEntity<Void> removeAffiliation(
      @PathVariable UUID externalUserId, @PathVariable UUID affiliationId) {
    log.debug("Removing affiliation: {} for external user: {}", affiliationId, externalUserId);

    externalUserAffiliationService.removeAffiliation(externalUserId, affiliationId);

    return ResponseEntity.noContent().build();
  }
}
