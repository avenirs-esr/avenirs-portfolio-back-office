package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.client;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.CofolioProfile;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.client.CofolioProfileClient;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.client.LoggedInUserHeadersForwarder;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class ApiCofolioProfileClient implements CofolioProfileClient {

  private static final ParameterizedTypeReference<List<UserProfileResponse>> PROFILE_LIST =
      new ParameterizedTypeReference<>() {};

  private final WebClient webClient;
  private final LoggedInUserHeadersForwarder headersForwarder;

  @Value("${avenirs.api.user-profiles.endpoint}")
  private String userProfilesEndpoint;

  public ApiCofolioProfileClient(
      WebClient webClient, LoggedInUserHeadersForwarder headersForwarder) {
    this.webClient = webClient;
    this.headersForwarder = headersForwarder;
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
            .headers(headersForwarder::forward)
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
                    response.eppn(),
                    response.hasConnected(),
                    response.userId(),
                    response.profilePictureId(),
                    response.staff(),
                    response.student())));

    return profilesByEppn;
  }

  private record UserProfileLookupBody(List<String> eppns) {}

  private record UserProfileResponse(
      String eppn,
      boolean hasConnected,
      UUID userId,
      UUID profilePictureId,
      boolean staff,
      boolean student) {}
}
