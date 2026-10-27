package fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.infrastructure.adapter.client;

import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.model.ContextualAuthorization;
import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.port.output.provider.ContextualAuthorizationProvider;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.client.LoggedInUserHeadersForwarder;
import fr.avenirsesr.portfolio.common.authorization.domain.model.enums.EAuthorization;
import fr.avenirsesr.portfolio.common.authorization.domain.model.enums.EAuthorizationEntityType;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PageInfo;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class ApiContextualAuthorizationProvider implements ContextualAuthorizationProvider {

  private static final Set<EAuthorizationEntityType> SUPPORTED_TYPES =
      Set.of(EAuthorizationEntityType.ACTIVITY);

  private static final ParameterizedTypeReference<List<EAuthorizationEntityType>> TYPE_LIST =
      new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<ApiPage> PAGE =
      new ParameterizedTypeReference<>() {};

  private final WebClient webClient;
  private final LoggedInUserHeadersForwarder headersForwarder;

  @Value("${avenirs.api.contextual-authorizations.endpoint}")
  private String contextualAuthorizationsEndpoint;

  public ApiContextualAuthorizationProvider(
      WebClient webClient, LoggedInUserHeadersForwarder headersForwarder) {
    this.webClient = webClient;
    this.headersForwarder = headersForwarder;
  }

  @Override
  public Set<EAuthorizationEntityType> supportedTypes() {
    return SUPPORTED_TYPES;
  }

  @Override
  public Set<EAuthorizationEntityType> availableTypes(String eppn, Set<UUID> institutionIds) {
    log.debug("Fetching the authorization types of {} from the portfolio api", eppn);

    List<EAuthorizationEntityType> types =
        webClient
            .get()
            .uri(uri(eppn, institutionIds, null, "types"))
            .headers(headersForwarder::forward)
            .retrieve()
            .bodyToMono(TYPE_LIST)
            .block();

    return types == null || types.isEmpty()
        ? EnumSet.noneOf(EAuthorizationEntityType.class)
        : EnumSet.copyOf(types);
  }

  @Override
  public PagedResult<ContextualAuthorization> findPage(
      EAuthorizationEntityType entityType,
      String eppn,
      Set<UUID> institutionIds,
      PageCriteria pageCriteria) {
    log.debug("Fetching the {} authorizations of {} from the portfolio api", entityType, eppn);

    ApiPage response =
        webClient
            .get()
            .uri(
                uri(
                    eppn,
                    institutionIds,
                    new PagedType(entityType, pageCriteria.page(), pageCriteria.pageSize()),
                    null))
            .headers(headersForwarder::forward)
            .retrieve()
            .bodyToMono(PAGE)
            .block();

    if (response == null) {
      return new PagedResult<>(
          List.of(), new PageInfo(pageCriteria.page(), pageCriteria.pageSize(), 0));
    }
    return new PagedResult<>(
        response.data().stream()
            .map(
                authorization ->
                    new ContextualAuthorization(
                        entityType,
                        authorization.entityId(),
                        authorization.entityName(),
                        Set.copyOf(authorization.authorizations())))
            .toList(),
        new PageInfo(
            response.page().page(), response.page().pageSize(), response.page().totalElements()));
  }

  private URI uri(String eppn, Set<UUID> institutionIds, PagedType paged, String suffix) {
    UriComponentsBuilder builder =
        UriComponentsBuilder.fromUriString(contextualAuthorizationsEndpoint)
            .pathSegment(eppn, "authorizations");
    if (suffix != null) {
      builder.pathSegment(suffix);
    }
    if (paged != null) {
      builder
          .queryParam("entityType", paged.entityType())
          .queryParam("page", paged.page())
          .queryParam("pageSize", paged.pageSize());
    }
    builder.queryParam("institutionIds", institutionIds.stream().map(UUID::toString).toList());
    return builder.encode().build().toUri();
  }

  private record PagedType(EAuthorizationEntityType entityType, int page, int pageSize) {}

  private record ApiAuthorization(
      UUID entityId, String entityName, Set<EAuthorization> authorizations) {}

  private record ApiPageInfo(int page, int pageSize, long totalElements, int totalPages) {}

  private record ApiPage(List<ApiAuthorization> data, ApiPageInfo page) {}
}
