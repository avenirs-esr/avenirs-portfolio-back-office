package fr.avenirsesr.portfolio.backoffice.file.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.LoggedInExternalUserService;
import fr.avenirsesr.portfolio.common.file.domain.port.output.service.FileUploaderProvider;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExternalUserFileUploaderProvider implements FileUploaderProvider {
  private final LoggedInExternalUserService loggedInExternalUserService;

  @Override
  public UUID currentUploaderId() {
    return loggedInExternalUserService.getLoggedInExternalUser().getId();
  }
}
