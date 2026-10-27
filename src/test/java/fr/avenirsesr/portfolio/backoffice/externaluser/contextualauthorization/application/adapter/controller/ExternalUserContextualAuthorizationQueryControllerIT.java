package fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.application.adapter.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.domain.model.ContextualAuthorization;
import fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.infrastructure.adapter.client.ApiContextualAuthorizationProvider;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.common.authorization.domain.model.enums.EAuthorization;
import fr.avenirsesr.portfolio.common.authorization.domain.model.enums.EAuthorizationEntityType;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PageInfo;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.EPermission;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.HmacAuthenticationToken;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

class ExternalUserContextualAuthorizationQueryControllerIT extends ContainerConfigurationTest {

  private static final String EXTERNAL_USERS_PATH = "/back-office/external-users/";
  private static final String CONTEXTUAL_AUTHORIZATIONS = "/contextual-authorizations";
  private static final String STAFF_EPPN = "ethan.perrin@university.com";
  private static final String SEEDED_STUDENT_EPPN = "lucas.tessier@university.com";
  private static final String SEEDED_BORDEAUX_STUDENT_EPPN = "sophie.charpentier@university.com";
  private static final String SEEDED_RENNES_UAI = "0350001A";

  @Autowired private MockMvc mockMvc;
  @Autowired private ExternalUserRepository externalUserRepository;
  @Autowired private InstitutionRepository institutionRepository;

  @MockitoBean private ApiContextualAuthorizationProvider provider;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  @BeforeEach
  void stubProvider() {
    when(provider.supportedTypes()).thenReturn(Set.of(EAuthorizationEntityType.ACTIVITY));
  }

  private UUID externalUserIdOf(String eppn) {
    return externalUserRepository.findByEppn(eppn).orElseThrow().getId();
  }

  private UUID rennesId() {
    return institutionRepository.findByUai(SEEDED_RENNES_UAI).orElseThrow().getId();
  }

  private static MockHttpServletRequestBuilder asStaffUser(MockHttpServletRequestBuilder builder) {
    return builder
        .header("X-Forwarded-For", "203.0.113.10")
        .with(
            authentication(
                new HmacAuthenticationToken(
                    STAFF_EPPN,
                    List.of(
                        new SimpleGrantedAuthority(
                            EPermission.PERM_EXTERNAL_USER_READ.authority())))));
  }

  @Nested
  class WhenGettingTheAvailableTypes {

    @Test
    void thenItShouldReturnTheTypesOfTheProviderNarrowedToTheInstitutionsOfTheStaff()
        throws Exception {
      BddLogger.given("a student of Rennes holding authorizations on activities");
      UUID externalUserId = externalUserIdOf(SEEDED_STUDENT_EPPN);
      when(provider.availableTypes(eq(SEEDED_STUDENT_EPPN), any()))
          .thenReturn(Set.of(EAuthorizationEntityType.ACTIVITY));

      BddLogger.when("calling GET /back-office/external-users/{externalUserId}/types");
      mockMvc
          .perform(
              asStaffUser(
                      get(
                          EXTERNAL_USERS_PATH
                              + externalUserId
                              + CONTEXTUAL_AUTHORIZATIONS
                              + "/types"))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(header().string("Cache-Control", "no-store"))
          .andExpect(jsonPath("$.length()", is(1)))
          .andExpect(jsonPath("$[0]", is("ACTIVITY")));

      BddLogger.then("the provider should only be asked about the institutions of the staff");
      verify(provider)
          .availableTypes(
              eq(SEEDED_STUDENT_EPPN), argThat(ids -> ids != null && ids.contains(rennesId())));
    }

    @Test
    void thenItShouldReturnForbidden_whenTheUserIsOutsideTheStaffScope() throws Exception {
      BddLogger.given("a student affiliated to Bordeaux only, seen by a staff of Rennes");
      UUID externalUserId = externalUserIdOf(SEEDED_BORDEAUX_STUDENT_EPPN);

      BddLogger.when("calling GET /back-office/external-users/{externalUserId}/types");
      mockMvc
          .perform(
              asStaffUser(
                      get(
                          EXTERNAL_USERS_PATH
                              + externalUserId
                              + CONTEXTUAL_AUTHORIZATIONS
                              + "/types"))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, without querying the provider");
      verify(provider, never()).availableTypes(any(), any());
    }

    @Test
    void thenItShouldReturnNotFound_whenTheExternalUserDoesNotExist() throws Exception {
      BddLogger.when("calling GET /back-office/external-users/{externalUserId}/types");
      mockMvc
          .perform(
              asStaffUser(
                      get(
                          EXTERNAL_USERS_PATH
                              + UUID.randomUUID()
                              + CONTEXTUAL_AUTHORIZATIONS
                              + "/types"))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());

      BddLogger.then("it should return 404");
    }

    @Test
    void thenItShouldReturnForbidden_whenStaffMissesTheReadPermission() throws Exception {
      BddLogger.given("a logged-in staff without the external user read permission");
      UUID externalUserId = externalUserIdOf(SEEDED_STUDENT_EPPN);

      BddLogger.when("calling GET /back-office/external-users/{externalUserId}/types");
      mockMvc
          .perform(
              get(EXTERNAL_USERS_PATH + externalUserId + CONTEXTUAL_AUTHORIZATIONS + "/types")
                  .header("X-Forwarded-For", "203.0.113.10")
                  .with(authentication(new HmacAuthenticationToken(STAFF_EPPN)))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403");
    }
  }

  @Nested
  class WhenGettingTheAuthorizations {

    @Test
    void thenItShouldReturnThePageOfTheProviderWithSortedAuthorizations() throws Exception {
      BddLogger.given("a student of Rennes holding authorizations on an activity");
      UUID externalUserId = externalUserIdOf(SEEDED_STUDENT_EPPN);
      UUID activityId = UUID.randomUUID();
      when(provider.findPage(
              eq(EAuthorizationEntityType.ACTIVITY),
              eq(SEEDED_STUDENT_EPPN),
              any(),
              eq(new PageCriteria(1, 2))))
          .thenReturn(
              new PagedResult<>(
                  List.of(
                      new ContextualAuthorization(
                          EAuthorizationEntityType.ACTIVITY,
                          activityId,
                          "Mon activité",
                          Set.of(EAuthorization.PUBLISH, EAuthorization.EDIT))),
                  new PageInfo(1, 2, 3)));

      BddLogger.when(
          "calling GET /back-office/external-users/{externalUserId}/contextual-authorizations");
      mockMvc
          .perform(
              asStaffUser(
                      get(EXTERNAL_USERS_PATH + externalUserId + CONTEXTUAL_AUTHORIZATIONS)
                          .param("entityType", "ACTIVITY")
                          .param("page", "1")
                          .param("pageSize", "2"))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(header().string("Cache-Control", "no-store"))
          .andExpect(jsonPath("$.data.length()", is(1)))
          .andExpect(jsonPath("$.data[0].entityType", is("ACTIVITY")))
          .andExpect(jsonPath("$.data[0].entityId", is(activityId.toString())))
          .andExpect(jsonPath("$.data[0].entityName", is("Mon activité")))
          .andExpect(jsonPath("$.data[0].authorizations[0]", is("EDIT")))
          .andExpect(jsonPath("$.data[0].authorizations[1]", is("PUBLISH")))
          .andExpect(jsonPath("$.page.page", is(1)))
          .andExpect(jsonPath("$.page.totalElements", is(3)))
          .andExpect(jsonPath("$.page.totalPages", is(2)));

      BddLogger.then("the page should be exposed with the authorizations in a stable order");
    }

    @Test
    void thenItShouldReturnBadRequest_whenTheEntityTypeIsUnknown() throws Exception {
      BddLogger.given("a type that cannot be consulted");
      UUID externalUserId = externalUserIdOf(SEEDED_STUDENT_EPPN);

      BddLogger.when(
          "calling GET /back-office/external-users/{externalUserId}/contextual-authorizations");
      mockMvc
          .perform(
              asStaffUser(
                      get(EXTERNAL_USERS_PATH + externalUserId + CONTEXTUAL_AUTHORIZATIONS)
                          .param("entityType", "ACTIVITY_DRAFT"))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());

      BddLogger.then("it should return 400");
    }

    @Test
    void thenItShouldReturnForbidden_whenTheUserIsOutsideTheStaffScope() throws Exception {
      BddLogger.given("a student affiliated to Bordeaux only, seen by a staff of Rennes");
      UUID externalUserId = externalUserIdOf(SEEDED_BORDEAUX_STUDENT_EPPN);

      BddLogger.when(
          "calling GET /back-office/external-users/{externalUserId}/contextual-authorizations");
      mockMvc
          .perform(
              asStaffUser(
                      get(EXTERNAL_USERS_PATH + externalUserId + CONTEXTUAL_AUTHORIZATIONS)
                          .param("entityType", "ACTIVITY"))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, without querying the provider");
      verify(provider, never()).findPage(any(), any(), any(), any());
    }
  }
}
