package fr.avenirsesr.portfolio.user.domain.service;

import fr.avenirsesr.portfolio.common.data.domain.model.User;
import fr.avenirsesr.portfolio.user.domain.port.input.UserService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {
  @Override
  public User getUser(UUID id) {
    // temporary fix waiting for security service to be available
    throw new UnsupportedOperationException(
        "Not supported action. Back-Office routes should not be authenticated for now");
  }
}
