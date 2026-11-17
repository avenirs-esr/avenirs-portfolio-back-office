package fr.avenirsesr.portfolio.backoffice.file.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.common.error.domain.exception.UserNotFoundException;
import fr.avenirsesr.portfolio.common.file.domain.port.output.service.FileUploaderProvider;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExternalUserFileUploaderProvider implements FileUploaderProvider {
  private final ExternalUserRepository externalUserRepository;

  @Override
  public UUID currentUploaderId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new UserNotFoundException();
    }
    String principal = authentication.getName();
    return externalUserRepository
        .findByEppn(principal)
        .map(ExternalUser::getId)
        .orElseGet(() -> UUID.nameUUIDFromBytes(principal.getBytes(StandardCharsets.UTF_8)));
  }
}
