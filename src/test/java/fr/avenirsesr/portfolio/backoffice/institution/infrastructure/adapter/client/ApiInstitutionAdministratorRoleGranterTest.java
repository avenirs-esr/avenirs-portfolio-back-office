package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.client;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.client.SecurityClient;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionAdministratorRoleGrantException;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.ERole;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import fr.avenirsesr.portfolio.common.user.domain.exceptions.ExternalUserNotFoundException;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
public class ApiInstitutionAdministratorRoleGranterTest {

  private static final String GRANT_PATH = "/internal/access-control/grant";
  private static final String EPPN = "ethan.perrin@university.com";

  @Mock private ExternalUserRepository externalUserRepository;
  @Mock private SecurityClient securityClient;

  private MockWebServer mockWebServer;
  private ApiInstitutionAdministratorRoleGranter granter;

  @BeforeEach
  void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    WebClient webClient = WebClient.builder().build();
    granter =
        new ApiInstitutionAdministratorRoleGranter(
            webClient, externalUserRepository, securityClient);
    ReflectionTestUtils.setField(
        granter, "grantEndpoint", mockWebServer.url(GRANT_PATH).toString());
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Nested
  class WhenTheUserCannotBeResolved {

    @Test
    void shouldThrowWhenTheUserDoesNotExist() {
      BddLogger.given("an unknown user id");
      UUID userId = UUID.randomUUID();
      when(externalUserRepository.findById(userId)).thenReturn(Optional.empty());

      BddLogger.when("granting the admin role");
      BddLogger.then("it fails and the security service is never called");
      assertThatThrownBy(() -> granter.grantAdminRole(userId))
          .isInstanceOf(ExternalUserNotFoundException.class);
      verifyNoInteractions(securityClient);
      assertThat(mockWebServer.getRequestCount()).isZero();
    }
  }

  @Nested
  class WhenTheUserAlreadyHasTheRole {

    @Test
    void shouldSkipTheGrant() {
      BddLogger.given("a user who already has ROLE_ADMIN");
      UUID userId = UUID.randomUUID();
      when(externalUserRepository.findById(userId)).thenReturn(Optional.of(externalUser(EPPN)));
      when(securityClient.findRolesByLogin(EPPN)).thenReturn(Set.of(ERole.ROLE_ADMIN));

      BddLogger.when("granting the admin role");
      BddLogger.then("the grant endpoint is not called and no error is raised");
      assertThatCode(() -> granter.grantAdminRole(userId)).doesNotThrowAnyException();
      assertThat(mockWebServer.getRequestCount()).isZero();
    }
  }

  @Nested
  class WhenGrantingTheRole {

    @Test
    void shouldPostLoginAndRoleNameWhenTheUserHasNotTheRoleYet() throws InterruptedException {
      BddLogger.given("a user without ROLE_ADMIN and a security service that grants it");
      UUID userId = UUID.randomUUID();
      when(externalUserRepository.findById(userId)).thenReturn(Optional.of(externalUser(EPPN)));
      when(securityClient.findRolesByLogin(EPPN)).thenReturn(Set.of());
      mockWebServer.enqueue(
          jsonResponse(
              "{\"login\":\""
                  + EPPN
                  + "\",\"granted\":true,\"assignmentId\":\""
                  + UUID.randomUUID()
                  + "\",\"error\":null}"));

      BddLogger.when("granting the admin role");
      granter.grantAdminRole(userId);

      BddLogger.then("a POST carrying the login and the role name is sent to the grant endpoint");
      RecordedRequest request = mockWebServer.takeRequest();
      assertThat(request.getMethod()).isEqualTo("POST");
      assertThat(request.getPath()).isEqualTo(GRANT_PATH);
      String body = request.getBody().readUtf8();
      assertThat(body).contains(EPPN).contains(ERole.ROLE_ADMIN.name());
    }

    @Test
    void shouldThrowWhenTheGrantIsRefused() {
      BddLogger.given("a security service that refuses the grant");
      UUID userId = UUID.randomUUID();
      when(externalUserRepository.findById(userId)).thenReturn(Optional.of(externalUser(EPPN)));
      when(securityClient.findRolesByLogin(EPPN)).thenReturn(Set.of());
      mockWebServer.enqueue(
          jsonResponse(
              "{\"login\":\""
                  + EPPN
                  + "\",\"granted\":false,\"assignmentId\":null,\"error\":\"role not found\"}"));

      BddLogger.when("granting the admin role");
      BddLogger.then("a grant exception is raised");
      assertThatThrownBy(() -> granter.grantAdminRole(userId))
          .isInstanceOf(InstitutionAdministratorRoleGrantException.class);
    }

    @Test
    void shouldThrowWhenTheSecurityServiceReturnsNoBody() {
      BddLogger.given("a security service that returns an empty body");
      UUID userId = UUID.randomUUID();
      when(externalUserRepository.findById(userId)).thenReturn(Optional.of(externalUser(EPPN)));
      when(securityClient.findRolesByLogin(EPPN)).thenReturn(Set.of());
      mockWebServer.enqueue(new MockResponse().setResponseCode(200));

      BddLogger.when("granting the admin role");
      BddLogger.then("a grant exception is raised");
      assertThatThrownBy(() -> granter.grantAdminRole(userId))
          .isInstanceOf(InstitutionAdministratorRoleGrantException.class);
    }
  }

  private static MockResponse jsonResponse(String body) {
    return new MockResponse()
        .setResponseCode(200)
        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .setBody(body);
  }

  private static ExternalUser externalUser(String eppn) {
    return ExternalUser.create(
        eppn,
        "TEACH-EP-09",
        EExternalSource.BACK_OFFICE,
        "0350001A",
        Set.of(EUserCategory.STAFF),
        eppn,
        "Ethan",
        "Perrin",
        EUserStatus.ACTIVE);
  }
}
