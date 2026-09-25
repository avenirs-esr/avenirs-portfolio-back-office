package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.StaffAccessCheckRequest;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.StudentScopeResponse;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("back-office/access")
public class AccessController {

  private final ExternalUserAffiliationService externalUserAffiliationService;

  @PostMapping("/staff/check")
  public ResponseEntity<Boolean> staffAccessCheck(@RequestBody StaffAccessCheckRequest request) {
    log.debug(
        "Checking staff access for eppn {} to institutions {} / groups {}",
        request.eppn(),
        request.targetInstitutionIds(),
        request.targetGroupIds());

    boolean hasAccess =
        externalUserAffiliationService.staffHasAccess(
            request.eppn(), request.targetInstitutionIds(), request.targetGroupIds());

    return ResponseEntity.ok(hasAccess);
  }

  @GetMapping("/student/{eppn}/scope")
  public ResponseEntity<StudentScopeResponse> studentScope(@PathVariable String eppn) {
    log.debug("Resolving student scope for eppn {}", eppn);

    var scope = externalUserAffiliationService.studentScope(eppn);

    return ResponseEntity.ok(new StudentScopeResponse(scope.institutionIds(), scope.groupIds()));
  }
}
