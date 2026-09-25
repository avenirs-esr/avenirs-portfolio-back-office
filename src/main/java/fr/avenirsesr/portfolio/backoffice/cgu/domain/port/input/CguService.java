package fr.avenirsesr.portfolio.backoffice.cgu.domain.port.input;

import fr.avenirsesr.portfolio.common.cgu.application.adapter.dto.CguDTO;
import fr.avenirsesr.portfolio.common.file.application.adapter.dto.FileDTO;
import fr.avenirsesr.portfolio.common.file.application.adapter.request.FileUploadRequest;

public interface CguService {
  FileDTO publish(FileUploadRequest request);

  CguDTO getLatest();
}
