package fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.mapper.ExternalUserApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.input.ExternalUserService;
import fr.avenirsesr.portfolio.common.user.application.adapter.dto.ExternalUserDTO;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping({"back-office/external-users"})
public class ExternalUserController {

  private final ExternalUserService externalUserService;

  @GetMapping
  public ResponseEntity<List<ExternalUserDTO>> getExternalUsers() {
    log.debug("Getting all external users");

    List<ExternalUser> externalUsers = externalUserService.getAllExternalUsers();

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
}
