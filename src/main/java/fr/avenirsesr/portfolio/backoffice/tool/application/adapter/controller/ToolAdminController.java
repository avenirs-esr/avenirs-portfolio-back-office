package fr.avenirsesr.portfolio.backoffice.tool.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto.CreateLocalToolRequest;
import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto.CreateNationalToolRequest;
import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto.ToolDTO;
import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto.UpdateToolRequest;
import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.mapper.ToolApplicationMapper;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.EToolType;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.backoffice.tool.domain.port.input.ToolService;
import fr.avenirsesr.portfolio.common.data.application.adapter.response.PagedResponse;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.file.application.adapter.dto.FileDTO;
import fr.avenirsesr.portfolio.common.file.domain.model.File;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("back-office/tools")
public class ToolAdminController {

  private final ToolService toolService;

  @PreAuthorize("hasAuthority('tool:national:management')")
  @GetMapping("/national")
  public ResponseEntity<PagedResponse<ToolDTO>> getNationalTools(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize) {
    PageCriteria pageCriteria = new PageCriteria(page, pageSize);
    log.debug("Received request to get the national tools {}", pageCriteria);

    return ResponseEntity.ok(
        ToolApplicationMapper.toPagedResponse(toolService.findNationalTools(pageCriteria)));
  }

  @PreAuthorize("hasAuthority('tool:local:management')")
  @GetMapping("/local")
  public ResponseEntity<PagedResponse<ToolDTO>> getLocalTools(
      @Parameter(schema = @Schema(ref = "#/components/schemas/EToolType"))
          @RequestParam(required = false)
          EToolType type,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize) {
    PageCriteria pageCriteria = new PageCriteria(page, pageSize);
    log.debug("Received request to get the {} tools of the staff scope {}", type, pageCriteria);

    return ResponseEntity.ok(
        ToolApplicationMapper.toPagedResponse(toolService.findLocalTools(type, pageCriteria)));
  }

  @PreAuthorize("hasAuthority('tool:national:management')")
  @GetMapping("/national/{toolId}")
  public ResponseEntity<ToolDTO> getNationalTool(@PathVariable UUID toolId) {
    log.debug("Received request to get the national tool {}", toolId);

    return ResponseEntity.ok(ToolApplicationMapper.toToolDTO(toolService.getNationalTool(toolId)));
  }

  @PreAuthorize("hasAuthority('tool:local:management')")
  @GetMapping("/local/{toolId}")
  public ResponseEntity<ToolDTO> getLocalTool(@PathVariable UUID toolId) {
    log.debug("Received request to get the tool {}", toolId);

    return ResponseEntity.ok(ToolApplicationMapper.toToolDTO(toolService.getLocalTool(toolId)));
  }

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

  @PreAuthorize("hasAuthority('tool:national:management')")
  @DeleteMapping("/national/{toolId}")
  public ResponseEntity<Void> deleteNationalTool(@PathVariable UUID toolId) {
    log.debug("Received request to delete the national tool {}", toolId);

    toolService.deleteNationalTool(toolId);

    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasAuthority('tool:local:management')")
  @DeleteMapping("/local/{toolId}")
  public ResponseEntity<Void> deleteLocalTool(@PathVariable UUID toolId) {
    log.debug("Received request to delete the tool {}", toolId);

    toolService.deleteLocalTool(toolId);

    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasAuthority('tool:national:management')")
  @PostMapping(value = "/national/{toolId}/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<FileDTO> uploadNationalToolBanner(
      @PathVariable UUID toolId, @RequestParam("file") MultipartFile file) {
    log.debug("Received request to upload the banner of the national tool {}", toolId);

    File banner =
        toolService.uploadNationalToolBanner(
            toolId,
            file.getOriginalFilename(),
            file.getContentType(),
            file.getSize(),
            file::getInputStream);

    return ResponseEntity.status(HttpStatus.CREATED).body(FileDTO.fromDomain(banner));
  }

  @PreAuthorize("hasAuthority('tool:national:management')")
  @DeleteMapping("/national/{toolId}/banner")
  public ResponseEntity<Void> deleteNationalToolBanner(@PathVariable UUID toolId) {
    log.debug("Received request to delete the banner of the national tool {}", toolId);

    toolService.deleteNationalToolBanner(toolId);

    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasAuthority('tool:local:management')")
  @PostMapping(value = "/local/{toolId}/banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<FileDTO> uploadLocalToolBanner(
      @PathVariable UUID toolId, @RequestParam("file") MultipartFile file) {
    log.debug("Received request to upload the banner of the tool {}", toolId);

    File banner =
        toolService.uploadLocalToolBanner(
            toolId,
            file.getOriginalFilename(),
            file.getContentType(),
            file.getSize(),
            file::getInputStream);

    return ResponseEntity.status(HttpStatus.CREATED).body(FileDTO.fromDomain(banner));
  }

  @PreAuthorize("hasAuthority('tool:local:management')")
  @DeleteMapping("/local/{toolId}/banner")
  public ResponseEntity<Void> deleteLocalToolBanner(@PathVariable UUID toolId) {
    log.debug("Received request to delete the banner of the tool {}", toolId);

    toolService.deleteLocalToolBanner(toolId);

    return ResponseEntity.noContent().build();
  }
}
