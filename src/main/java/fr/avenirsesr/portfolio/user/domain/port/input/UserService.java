package fr.avenirsesr.portfolio.user.domain.port.input;

import fr.avenirsesr.portfolio.common.data.domain.model.User;
import java.util.UUID;

public interface UserService {
  User getUser(UUID id);
}
