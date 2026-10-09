package fr.avenirsesr.portfolio.backoffice.file.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.LoggedInExternalUserService;
import fr.avenirsesr.portfolio.common.file.domain.port.output.service.FileUploaderProvider;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Binds the shared file registry to the notion of a current user held by the back office.
 *
 * <p>Users are external here, there is no user table to point at: this is precisely why the
 * registry stores the uploader as a plain identifier.
 */
@Component
@RequiredArgsConstructor
public class ExternalUserFileUploaderProvider implements FileUploaderProvider {
  private final LoggedInExternalUserService loggedInExternalUserService;

  @Override
  public UUID currentUploaderId() {
    return loggedInExternalUserService.getLoggedInExternalUser().getId();
  }
}
