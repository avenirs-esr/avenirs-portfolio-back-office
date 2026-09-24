package fr.avenirsesr.portfolio.backoffice.cgu.domain.service;

import fr.avenirsesr.portfolio.backoffice.cgu.domain.port.input.CguService;
import fr.avenirsesr.portfolio.common.file.application.adapter.client.FileClient;
import fr.avenirsesr.portfolio.common.file.application.adapter.dto.FileDTO;
import fr.avenirsesr.portfolio.common.file.application.adapter.request.FileUploadRequest;
import fr.avenirsesr.portfolio.common.file.domain.exception.FileTypeNotSupportedException;
import fr.avenirsesr.portfolio.common.file.domain.model.enums.EFileType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class CguServiceImpl implements CguService {
  private final FileClient fileClient;

  @Override
  public FileDTO publish(FileUploadRequest request) {
    if (EFileType.fromMimeType(request.mimeType()) != EFileType.HTML) {
      throw new FileTypeNotSupportedException("The terms of use must be an html file");
    }

    FileDTO published = fileClient.upload(request);
    log.info("New terms of use version published as file {}", published.id());

    return published;
  }
}
