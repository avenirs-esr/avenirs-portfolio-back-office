package fr.avenirsesr.portfolio.backoffice.file.application.adapter.controller;

import fr.avenirsesr.portfolio.common.file.domain.model.FileResource;
import fr.avenirsesr.portfolio.common.file.domain.port.input.FileResourceService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves the content of the files owned by this service.
 *
 * <p>The files used to be stored by the portfolio api, which served them from its own {@code
 * /storage}; they are now registered here, so this is the endpoint {@code FileDTO.url} points at.
 * Unlike its counterpart in the api, it is not listed in {@code security.permit-all-paths}: back
 * office files are reached by authenticated administrators only.
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("back-office/storage")
public class StorageController {
  private final FileResourceService fileResourceService;

  @GetMapping("/{fileId}")
  public ResponseEntity<ByteArrayResource> getResourceByFileId(@Valid @PathVariable UUID fileId) {
    log.debug("Received request to get the content of file [{}]", fileId);
    FileResource file = fileResourceService.fetchContent(fileId);

    return ResponseEntity.ok()
        .contentType(MediaType.asMediaType(MimeType.valueOf(file.fileType().getMimeType())))
        .body(new ByteArrayResource(file.content()));
  }
}
