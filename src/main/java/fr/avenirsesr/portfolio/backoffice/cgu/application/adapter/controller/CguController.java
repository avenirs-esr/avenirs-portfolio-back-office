package fr.avenirsesr.portfolio.backoffice.cgu.application.adapter.controller;

import static fr.avenirsesr.portfolio.common.file.application.adapter.MultipartFileReader.readBytes;

import fr.avenirsesr.portfolio.backoffice.cgu.domain.port.input.CguService;
import fr.avenirsesr.portfolio.common.file.application.adapter.dto.FileDTO;
import fr.avenirsesr.portfolio.common.file.application.adapter.request.FileUploadRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("back-office/cgu")
public class CguController {
  private final CguService cguService;

  @PreAuthorize("hasAuthority('cgu:update')")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<FileDTO> uploadCgu(@RequestParam("file") MultipartFile file) {
    log.debug("Received request to upload a new terms of use version");

    FileDTO published =
        cguService.publish(
            new FileUploadRequest(
                file.getOriginalFilename(), file.getContentType(), readBytes(file), false));

    return ResponseEntity.status(HttpStatus.CREATED).body(published);
  }
}
