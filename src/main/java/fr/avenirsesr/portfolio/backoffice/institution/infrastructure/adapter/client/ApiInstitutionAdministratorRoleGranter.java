package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.client;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.client.SecurityClient;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionAdministratorRoleGrantException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.client.InstitutionAdministratorRoleAssigner;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.ERole;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.AvenirsSecurityHeaders;
import fr.avenirsesr.portfolio.common.user.domain.exceptions.ExternalUserNotFoundException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class ApiInstitutionAdministratorRoleGranter
    implements InstitutionAdministratorRoleAssigner {

  private final WebClient webClient;
  private final ExternalUserRepository externalUserRepository;
  private final SecurityClient securityClient;

  @Value("${avenirs.security.grant.endpoint}")
  private String grantEndpoint;

  @Value("${security.authentication.api-key}")
  private String apiKeyValue;

  public ApiInstitutionAdministratorRoleGranter(
      WebClient webClient,
      ExternalUserRepository externalUserRepository,
      SecurityClient securityClient) {
    this.webClient = webClient;
    this.externalUserRepository = externalUserRepository;
    this.securityClient = securityClient;
  }

  @Override
  public void grantAdminRole(UUID userId) {

    String login =
        externalUserRepository
            .findById(userId)
            .map(ExternalUser::getEppn)
            .orElseThrow(ExternalUserNotFoundException::new);

    if (securityClient.findRolesByLogin(login).contains(ERole.ROLE_ADMIN)) {
      log.debug("User {} already has ROLE_ADMIN", login);
      return;
    }

    AccessControlGrantResult result =
        webClient
            .post()
            .uri(grantEndpoint)
            .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
            .bodyValue(new AccessControlGrantRequest(login, ERole.ROLE_ADMIN.name()))
            .retrieve()
            .bodyToMono(AccessControlGrantResult.class)
            .block();

    if (result == null || !result.granted()) {
      String error = result == null ? "no response from security service" : result.error();
      log.error("Security service refused to grant ROLE_ADMIN to {}: {}", login, error);
      throw new InstitutionAdministratorRoleGrantException();
    }

    log.info("Granted ROLE_ADMIN to {} (assignment {})", login, result.assignmentId());
  }

  private record AccessControlGrantRequest(String login, String role) {}

  private record AccessControlGrantResult(
      String login, boolean granted, UUID assignmentId, String error) {}
}
