package fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.AffiliationScopeNode;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.EAffiliationScopeNodeType;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserDetail;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.StaffAffiliationScope;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.model.ContextualAuthorization;
import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.port.output.provider.ContextualAuthorizationProvider;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.common.authorization.domain.model.enums.EAuthorization;
import fr.avenirsesr.portfolio.common.authorization.domain.model.enums.EAuthorizationEntityType;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PageInfo;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.error.domain.exception.FieldValidationException;
import fr.avenirsesr.portfolio.common.security.domain.exception.UserNotAuthorizedException;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExternalUserContextualAuthorizationServiceImplTest {

  private static final String EPPN = "lucas.tessier@university.com";

  @Mock private ContextualAuthorizationProvider provider;
  @Mock private ExternalUserAffiliationService externalUserAffiliationService;

  private final UUID parentInstitutionId = UUID.randomUUID();
  private final UUID childInstitutionId = UUID.randomUUID();
  private final PageCriteria pageCriteria = new PageCriteria(0, 8);

  private ExternalUser externalUser;
  private ExternalUserContextualAuthorizationServiceImpl service;

  @BeforeEach
  void setUp() {
    externalUser =
        ExternalUser.create(
            EPPN,
            "PEG-0001",
            EExternalSource.BACK_OFFICE,
            "0350001A",
            Set.of(EUserCategory.STAFF),
            EPPN,
            "Lucas",
            "Tessier",
            EUserStatus.ACTIVE);
    lenient().when(provider.supportedTypes()).thenReturn(Set.of(EAuthorizationEntityType.ACTIVITY));
    service =
        new ExternalUserContextualAuthorizationServiceImpl(
            List.of(provider), externalUserAffiliationService);
  }

  private void givenUserSeenByAnAdministrator(AffiliationScopeNode... institutionRoots) {
    when(externalUserAffiliationService.getExternalUserDetail(externalUser.getId()))
        .thenReturn(new ExternalUserDetail(externalUser, null, List.of()));
    when(externalUserAffiliationService.staffScope())
        .thenReturn(new StaffAffiliationScope(List.of(institutionRoots), List.of()));
  }

  private AffiliationScopeNode parentWithChild() {
    return new AffiliationScopeNode(
        parentInstitutionId,
        "Université",
        EAffiliationScopeNodeType.PRIMARY,
        List.of(
            new AffiliationScopeNode(
                childInstitutionId, "IUT", EAffiliationScopeNodeType.SECONDARY, List.of())));
  }

  @Test
  void shouldNarrowTheTypesToEveryInstitutionOfTheAdministratorChildrenIncluded() {
    BddLogger.given("an administrator of an institution having a child institution");
    givenUserSeenByAnAdministrator(parentWithChild());
    when(provider.availableTypes(EPPN, Set.of(parentInstitutionId, childInstitutionId)))
        .thenReturn(Set.of(EAuthorizationEntityType.ACTIVITY));

    BddLogger.when("getting the available types of the user");
    var types = service.getAvailableTypes(externalUser.getId());

    BddLogger.then("both institutions should have been used to narrow the provider");
    assertThat(types).containsExactly(EAuthorizationEntityType.ACTIVITY);
  }

  @Test
  void shouldDelegateThePageToTheProviderOfTheType() {
    BddLogger.given("a provider returning one activity");
    givenUserSeenByAnAdministrator(parentWithChild());
    var expected =
        new PagedResult<>(
            List.of(
                new ContextualAuthorization(
                    EAuthorizationEntityType.ACTIVITY,
                    UUID.randomUUID(),
                    "My activity",
                    Set.of(EAuthorization.EDIT))),
            new PageInfo(0, 8, 1));
    when(provider.findPage(
            EAuthorizationEntityType.ACTIVITY,
            EPPN,
            Set.of(parentInstitutionId, childInstitutionId),
            pageCriteria))
        .thenReturn(expected);

    BddLogger.when("getting the authorizations on activities");
    var page =
        service.getAuthorizations(
            externalUser.getId(), EAuthorizationEntityType.ACTIVITY, pageCriteria);

    BddLogger.then("the page of the provider should be returned");
    assertThat(page).isEqualTo(expected);
  }

  @Test
  void shouldRejectAUserOutsideTheScopeOfTheAdministratorWithoutReachingTheProviders() {
    BddLogger.given("a user the administrator may not see");
    when(externalUserAffiliationService.getExternalUserDetail(externalUser.getId()))
        .thenThrow(new UserNotAuthorizedException("outside of the scope"));

    BddLogger.then("both operations should be refused, and no provider queried");
    assertThatThrownBy(() -> service.getAvailableTypes(externalUser.getId()))
        .isInstanceOf(UserNotAuthorizedException.class);
    assertThatThrownBy(
            () ->
                service.getAuthorizations(
                    externalUser.getId(), EAuthorizationEntityType.ACTIVITY, pageCriteria))
        .isInstanceOf(UserNotAuthorizedException.class);
    verifyNoInteractions(provider);
  }

  @Test
  void shouldRejectATypeNoProviderSupports() {
    BddLogger.given("a provider supporting no type");
    givenUserSeenByAnAdministrator(parentWithChild());
    when(provider.supportedTypes()).thenReturn(Set.of());

    BddLogger.then("getting the authorizations of a type should be rejected");
    assertThatThrownBy(
            () ->
                service.getAuthorizations(
                    externalUser.getId(), EAuthorizationEntityType.ACTIVITY, pageCriteria))
        .isInstanceOf(FieldValidationException.class);
    verify(provider, never()).findPage(any(), any(), any(), any());
  }

  @Test
  void shouldReturnNothingWithoutQueryingTheProvidersWhenTheAdministratorHasNoInstitution() {
    BddLogger.given("an administrator whose scope holds no institution");
    givenUserSeenByAnAdministrator();

    BddLogger.when("getting the types and the authorizations");
    var types = service.getAvailableTypes(externalUser.getId());
    var page =
        service.getAuthorizations(
            externalUser.getId(), EAuthorizationEntityType.ACTIVITY, pageCriteria);

    BddLogger.then("both should be empty, and the provider left alone");
    assertThat(types).isEmpty();
    assertThat(page.content()).isEmpty();
    assertThat(page.pageInfo().totalElements()).isZero();
    verify(provider, never()).availableTypes(any(), any());
    verify(provider, never()).findPage(any(), any(), any(), any());
  }
}
