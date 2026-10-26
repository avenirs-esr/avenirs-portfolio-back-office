package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.client;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.client.SecurityClient;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.ERole;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class ApiSecurityClient implements SecurityClient {

  private final WebClient webClient;

  @Value("${avenirs.security.roles.endpoint}")
  private String rolesEndpoint;

  public ApiSecurityClient(WebClient webClient) {
    this.webClient = webClient;
  }

  @Override
  public Set<ERole> findRolesByLogin(String login) {
    log.debug("Fetching the roles of a login from the security service");

    UserRolesResponse response =
        webClient
            .get()
            .uri(rolesEndpoint + "?login={login}", login)
            .retrieve()
            .bodyToMono(UserRolesResponse.class)
            .block();

    Set<ERole> roles = EnumSet.noneOf(ERole.class);
    if (response == null || response.roles() == null) {
      return roles;
    }

    response
        .roles()
        .forEach(
            name -> {
              try {
                roles.add(ERole.valueOf(name));
              } catch (IllegalArgumentException e) {
                log.warn("Ignoring the unknown role {} reported by the security service", name);
              }
            });
    return roles;
  }

  private record UserRolesResponse(List<String> roles) {}
}
