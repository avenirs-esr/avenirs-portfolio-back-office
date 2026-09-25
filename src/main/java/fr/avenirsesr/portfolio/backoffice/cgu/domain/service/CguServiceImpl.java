package fr.avenirsesr.portfolio.backoffice.cgu.domain.service;

import fr.avenirsesr.portfolio.backoffice.cgu.domain.exception.CguNotFoundException;
import fr.avenirsesr.portfolio.backoffice.cgu.domain.model.Cgu;
import fr.avenirsesr.portfolio.backoffice.cgu.domain.port.input.CguService;
import fr.avenirsesr.portfolio.backoffice.cgu.domain.port.output.repository.CguRepository;
import fr.avenirsesr.portfolio.common.cgu.application.adapter.dto.CguDTO;
import fr.avenirsesr.portfolio.common.file.application.adapter.client.FileClient;
import fr.avenirsesr.portfolio.common.file.application.adapter.dto.FileDTO;
import fr.avenirsesr.portfolio.common.file.application.adapter.request.FileUploadRequest;
import fr.avenirsesr.portfolio.common.file.domain.exception.FileTypeNotSupportedException;
import fr.avenirsesr.portfolio.common.file.domain.model.enums.EFileType;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class CguServiceImpl implements CguService {
  private final FileClient fileClient;
  private final CguRepository cguRepository;

  @Override
  public FileDTO publish(FileUploadRequest request) {
    if (EFileType.fromMimeType(request.mimeType()) != EFileType.HTML) {
      throw new FileTypeNotSupportedException("The terms of use must be an html file");
    }

    FileDTO published = fileClient.upload(request);
    Cgu cgu = cguRepository.save(Cgu.create(published.id(), nextVersion()));
    log.info("Terms of use version {} published as file {}", cgu.getVersion(), cgu.getFileId());

    return published;
  }

  @Override
  public CguDTO getLatest() {
    Cgu cgu = cguRepository.findLatest().orElseThrow(CguNotFoundException::new);
    byte[] content = fileClient.fetchContent(cgu.getFileId());

    return new CguDTO(
        cgu.getId(),
        cgu.getVersion(),
        cgu.getUploadedAt(),
        new String(content, StandardCharsets.UTF_8));
  }

  private int nextVersion() {
    return cguRepository.findLatest().map(Cgu::getVersion).orElse(0) + 1;
  }
}
