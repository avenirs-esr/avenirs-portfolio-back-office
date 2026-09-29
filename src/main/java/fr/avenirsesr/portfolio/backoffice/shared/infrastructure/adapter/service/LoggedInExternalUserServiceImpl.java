package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.LoggedInExternalUserService;
import fr.avenirsesr.portfolio.common.error.domain.exception.UserNotFoundException;
import fr.avenirsesr.portfolio.common.user.domain.exceptions.ExternalUserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoggedInExternalUserServiceImpl implements LoggedInExternalUserService {
  private final ExternalUserRepository externalUserRepository;

  @Override
  public ExternalUser getLoggedInExternalUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw new UserNotFoundException();
    }
    return externalUserRepository
        .findByEppn(authentication.getName())
        .orElseThrow(ExternalUserNotFoundException::new);
  }
}
