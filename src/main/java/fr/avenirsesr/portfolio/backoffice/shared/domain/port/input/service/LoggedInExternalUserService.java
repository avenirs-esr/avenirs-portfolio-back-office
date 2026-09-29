package fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;

public interface LoggedInExternalUserService {
  ExternalUser getLoggedInExternalUser();
}
