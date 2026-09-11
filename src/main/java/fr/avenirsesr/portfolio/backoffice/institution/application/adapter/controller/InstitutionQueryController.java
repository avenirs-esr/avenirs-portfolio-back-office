package fr.avenirsesr.portfolio.backoffice.institution.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.institution.application.adapter.mapper.InstitutionApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionService;
import fr.avenirsesr.portfolio.common.institution.application.adapter.dto.InstitutionDTO;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only institution endpoints consumed by the other Avenirs services. Unlike {@link
 * InstitutionController}, they are protected by the API key alone, without an admin token.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("back-office/institutions")
public class InstitutionQueryController {

  private final InstitutionService institutionService;

  @GetMapping("/{id}")
  public ResponseEntity<InstitutionDTO> getInstitutionById(@PathVariable UUID id) {
    log.debug("Getting institution for id: {}", id);

    return ResponseEntity.ok(
        InstitutionApplicationMapper.toInstitutionDTO(institutionService.findById(id)));
  }
}
