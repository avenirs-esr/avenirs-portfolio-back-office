package fr.avenirsesr.portfolio.backoffice.tool.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto.ToolDTO;
import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.mapper.ToolApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.EToolType;
import fr.avenirsesr.portfolio.backoffice.tool.domain.port.input.ToolService;
import fr.avenirsesr.portfolio.common.data.application.adapter.response.PagedResponse;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("back-office/tools/available")
public class ToolController {

  private final ToolService toolService;

  @PreAuthorize("hasAuthority('tool:read')")
  @GetMapping
  public ResponseEntity<PagedResponse<ToolDTO>> getAvailableTools(
      @RequestParam(required = false) EToolType type,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize) {
    PageCriteria pageCriteria = new PageCriteria(page, pageSize);
    log.debug(
        "Received request to get the {} tools available to the learner {}", type, pageCriteria);

    return ResponseEntity.ok(
        ToolApplicationMapper.toPagedResponse(toolService.findAvailableTools(type, pageCriteria)));
  }

  @PreAuthorize("hasAuthority('tool:read')")
  @GetMapping("/{toolId}")
  public ResponseEntity<ToolDTO> getAvailableTool(@PathVariable UUID toolId) {
    log.debug("Received request to get the tool {} available to the learner", toolId);

    return ResponseEntity.ok(ToolApplicationMapper.toToolDTO(toolService.getAvailableTool(toolId)));
  }
}
