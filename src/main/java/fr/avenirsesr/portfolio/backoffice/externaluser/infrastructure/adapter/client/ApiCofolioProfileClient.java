package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.client;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.CofolioProfile;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.client.CofolioProfileClient;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.AvenirsSecurityHeaders;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class ApiCofolioProfileClient implements CofolioProfileClient {

  private static final String DEV_EPPN_HEADER = "eppn";

  private static final ParameterizedTypeReference<List<UserProfileResponse>> PROFILE_LIST =
      new ParameterizedTypeReference<>() {};

  private final WebClient webClient;
  private final HttpServletRequest currentRequest;

  @Value("${avenirs.api.user-profiles.endpoint}")
  private String userProfilesEndpoint;

  public ApiCofolioProfileClient(WebClient webClient, HttpServletRequest currentRequest) {
    this.webClient = webClient;
    this.currentRequest = currentRequest;
  }

  @Override
  public Map<String, CofolioProfile> findByEppns(Collection<String> eppns) {
    if (eppns == null || eppns.isEmpty()) {
      return Map.of();
    }

    List<String> requestedEppns = eppns.stream().filter(Objects::nonNull).distinct().toList();
    log.debug(
        "Fetching the cofolio profiles of {} eppn(s) from the portfolio api",
        requestedEppns.size());

    List<UserProfileResponse> responses =
        webClient
            .post()
            .uri(userProfilesEndpoint)
            .headers(this::forwardLoggedInUser)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new UserProfileLookupBody(requestedEppns))
            .retrieve()
            .bodyToMono(PROFILE_LIST)
            .block();

    if (responses == null) {
      return Map.of();
    }

    Map<String, CofolioProfile> profilesByEppn = new LinkedHashMap<>();
    responses.forEach(
        response ->
            profilesByEppn.put(
                response.eppn(),
                new CofolioProfile(
                    response.eppn(), response.hasConnected(), response.profilePictureId())));

    return profilesByEppn;
  }

  private void forwardLoggedInUser(HttpHeaders headers) {
    copyHeader(headers, AvenirsSecurityHeaders.SIGNED_CONTEXT);
    copyHeader(headers, AvenirsSecurityHeaders.CONTEXT_SIGNATURE);
    copyHeader(headers, DEV_EPPN_HEADER);
  }

  private void copyHeader(HttpHeaders headers, String name) {
    String value = currentRequest.getHeader(name);
    if (value != null) {
      headers.set(name, value);
    }
  }

  private record UserProfileLookupBody(List<String> eppns) {}

  private record UserProfileResponse(
      String eppn, boolean hasConnected, UUID userId, UUID profilePictureId) {}
}
