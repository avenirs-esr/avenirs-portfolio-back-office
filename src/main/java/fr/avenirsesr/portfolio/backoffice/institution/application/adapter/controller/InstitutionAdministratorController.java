package fr.avenirsesr.portfolio.backoffice.institution.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.institution.application.adapter.dto.DesignateInstitutionAdministratorRequest;
import fr.avenirsesr.portfolio.backoffice.institution.application.adapter.dto.InstitutionAdministratorDTO;
import fr.avenirsesr.portfolio.backoffice.institution.application.adapter.mapper.InstitutionAdministratorApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionAdministratorService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("back-office/institutions/administrators")
public class InstitutionAdministratorController {

  private final InstitutionAdministratorService institutionAdministratorService;

  @PreAuthorize("hasAuthority('rbac:assign')")
  @PostMapping("/{userId}")
  public ResponseEntity<List<InstitutionAdministratorDTO>> designateInstitutionAdministrator(
      @PathVariable UUID userId, @RequestBody DesignateInstitutionAdministratorRequest request) {
    log.debug(
        "Received request to designate user {} as administrator of institutions {}",
        userId,
        request.institutionIds());

    List<Institution> administered =
        institutionAdministratorService.designate(userId, request.institutionIds());

    return ResponseEntity.ok(InstitutionAdministratorApplicationMapper.toDTOs(administered));
  }
}
