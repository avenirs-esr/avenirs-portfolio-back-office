package fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.AffiliationScopeNode;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.model.ContextualAuthorization;
import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.port.input.ExternalUserContextualAuthorizationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.port.output.provider.ContextualAuthorizationProvider;
import fr.avenirsesr.portfolio.common.authorization.domain.model.enums.EAuthorizationEntityType;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PageInfo;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.error.domain.exception.FieldValidationException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ExternalUserContextualAuthorizationServiceImpl
    implements ExternalUserContextualAuthorizationService {

  private final List<ContextualAuthorizationProvider> providers;
  private final ExternalUserAffiliationService externalUserAffiliationService;

  @Override
  public List<EAuthorizationEntityType> getAvailableTypes(UUID externalUserId) {
    Scope scope = resolveScope(externalUserId);
    if (scope.institutionIds().isEmpty()) {
      return List.of();
    }

    Set<EAuthorizationEntityType> available = EnumSet.noneOf(EAuthorizationEntityType.class);
    providers.forEach(
        provider ->
            available.addAll(provider.availableTypes(scope.eppn(), scope.institutionIds())));
    return List.copyOf(available);
  }

  @Override
  public PagedResult<ContextualAuthorization> getAuthorizations(
      UUID externalUserId, EAuthorizationEntityType entityType, PageCriteria pageCriteria) {
    Scope scope = resolveScope(externalUserId);
    ContextualAuthorizationProvider provider =
        providers.stream()
            .filter(candidate -> candidate.supportedTypes().contains(entityType))
            .findFirst()
            .orElseThrow(
                () -> new FieldValidationException(EErrorCode.INVALID_ARGUMENT_TYPE, "entityType"));
    if (scope.institutionIds().isEmpty()) {
      return new PagedResult<>(
          List.of(), new PageInfo(pageCriteria.page(), pageCriteria.pageSize(), 0));
    }

    return provider.findPage(entityType, scope.eppn(), scope.institutionIds(), pageCriteria);
  }

  private Scope resolveScope(UUID externalUserId) {
    String eppn =
        externalUserAffiliationService
            .getExternalUserDetail(externalUserId)
            .externalUser()
            .getEppn();
    Set<UUID> institutionIds = new LinkedHashSet<>();
    externalUserAffiliationService
        .staffScope()
        .institutions()
        .forEach(node -> collectIds(node, institutionIds));
    return new Scope(eppn, institutionIds);
  }

  private static void collectIds(AffiliationScopeNode node, Set<UUID> ids) {
    ids.add(node.id());
    node.children().forEach(child -> collectIds(child, ids));
  }

  private record Scope(String eppn, Set<UUID> institutionIds) {}
}
