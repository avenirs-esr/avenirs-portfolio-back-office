package fr.avenirsesr.portfolio.backoffice.tool.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto.CreateLocalToolRequest;
import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto.CreateNationalToolRequest;
import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto.ToolDTO;
import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto.UpdateToolRequest;
import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.mapper.ToolApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.backoffice.tool.domain.port.input.ToolService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("back-office/tools")
public class ToolAdminController {

  private final ToolService toolService;

  @PreAuthorize("hasAuthority('tool:national:management')")
  @PostMapping("/national")
  public ResponseEntity<ToolDTO> createNationalTool(
      @RequestBody CreateNationalToolRequest request) {
    log.debug("Received request to create the national tool {}", request.name());

    Tool tool =
        toolService.createNationalTool(request.name(), request.description(), request.url());

    return ResponseEntity.status(HttpStatus.CREATED).body(ToolApplicationMapper.toToolDTO(tool));
  }

  @PreAuthorize("hasAuthority('tool:local:management')")
  @PostMapping("/local")
  public ResponseEntity<ToolDTO> createLocalTool(@RequestBody CreateLocalToolRequest request) {
    log.debug(
        "Received request to create the tool {} for institution {}",
        request.name(),
        request.institutionId());

    Tool tool =
        toolService.createLocalTool(
            request.name(), request.description(), request.url(), request.institutionId());

    return ResponseEntity.status(HttpStatus.CREATED).body(ToolApplicationMapper.toToolDTO(tool));
  }

  @PreAuthorize("hasAuthority('tool:national:management')")
  @PutMapping("/national/{toolId}")
  public ResponseEntity<ToolDTO> updateNationalTool(
      @PathVariable UUID toolId, @RequestBody UpdateToolRequest request) {
    log.debug("Received request to update the national tool {}", toolId);

    Tool tool =
        toolService.updateNationalTool(
            toolId, request.name(), request.description(), request.url());

    return ResponseEntity.ok(ToolApplicationMapper.toToolDTO(tool));
  }

  @PreAuthorize("hasAuthority('tool:local:management')")
  @PutMapping("/local/{toolId}")
  public ResponseEntity<ToolDTO> updateLocalTool(
      @PathVariable UUID toolId, @RequestBody UpdateToolRequest request) {
    log.debug("Received request to update the tool {}", toolId);

    Tool tool =
        toolService.updateLocalTool(toolId, request.name(), request.description(), request.url());

    return ResponseEntity.ok(ToolApplicationMapper.toToolDTO(tool));
  }
}
