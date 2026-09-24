package fr.avenirsesr.portfolio.backoffice.institution.application.adapter.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.institution.application.adapter.dto.InstitutionAccessCheckRequest;
import fr.avenirsesr.portfolio.backoffice.institution.application.adapter.dto.InstitutionAccessibleIdsRequest;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.AvenirsSecurityHeaders;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

class InstitutionControllerIT extends ContainerConfigurationTest {

  private static final String BASE_PATH = "/back-office/institutions";
  private static final String SEEDED_PRIMARY_HAI = "0350001A";
  private static final String SEEDED_SECONDARY_HAI = "0350002B";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private InstitutionRepository institutionRepository;

  @Value("${security.authentication.api-key}")
  private String apiKeyValue;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  private UUID institutionIdOf(String hai) {
    return institutionRepository.findByHai(hai).orElseThrow().getId();
  }

  @Nested
  class WhenGettingInstitutionById {

    @Nested
    class AndInstitutionIsSecondary {

      private UUID secondaryId;
      private UUID primaryId;
      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded secondary institution attached to a primary one");
        secondaryId = institutionIdOf(SEEDED_SECONDARY_HAI);
        primaryId = institutionIdOf(SEEDED_PRIMARY_HAI);

        BddLogger.when("calling GET /back-office/institutions/{id} with the api key only");
        response =
            mockMvc.perform(
                get(BASE_PATH + "/" + secondaryId)
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnInstitutionNameTypeAndParentId() throws Exception {
        BddLogger.then("it should return the institution name, type and parent id");

        response
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(secondaryId.toString())))
            .andExpect(jsonPath("$.name", is("Université de Rennes - IUT")))
            .andExpect(jsonPath("$.type", is("SECONDARY")))
            .andExpect(jsonPath("$.parentId", is(primaryId.toString())));
      }
    }

    @Nested
    class AndInstitutionIsPrimary {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded primary institution without parent");
        UUID primaryId = institutionIdOf(SEEDED_PRIMARY_HAI);

        BddLogger.when("calling GET /back-office/institutions/{id}");
        response =
            mockMvc.perform(
                get(BASE_PATH + "/" + primaryId)
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnInstitutionWithoutParentId() throws Exception {
        BddLogger.then("it should return the institution with a null parent id");

        response
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("PRIMARY")))
            .andExpect(jsonPath("$.parentId", is(nullValue())));
      }
    }

    @Nested
    class AndInstitutionDoesNotExist {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a random institution id that does not exist");
        UUID unknownId = UUID.randomUUID();

        BddLogger.when("calling GET /back-office/institutions/{id}");
        response =
            mockMvc.perform(
                get(BASE_PATH + "/" + unknownId)
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnNotFound() throws Exception {
        BddLogger.then("it should return 404");

        response.andExpect(status().isNotFound());
      }
    }

    @Nested
    class AndApiKeyIsMissing {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a request without api key");
        UUID primaryId = institutionIdOf(SEEDED_PRIMARY_HAI);

        BddLogger.when("calling GET /back-office/institutions/{id}");
        response =
            mockMvc.perform(get(BASE_PATH + "/" + primaryId).accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnUnauthorized() throws Exception {
        BddLogger.then("it should return 401");

        response.andExpect(status().isUnauthorized());
      }
    }
  }

  @Nested
  class WhenCheckingStaffAccess {

    @Nested
    class AndTargetIdIsDirectlyAffiliated {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded primary institution matching an affiliated id");
        UUID primaryId = institutionIdOf(SEEDED_PRIMARY_HAI);
        InstitutionAccessCheckRequest request =
            new InstitutionAccessCheckRequest(List.of(primaryId), List.of(primaryId));

        BddLogger.when("calling POST /back-office/institutions/staff/access-check");
        response =
            mockMvc.perform(
                post(BASE_PATH + "/staff/access-check")
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
      }

      @Test
      void thenItShouldGrantAccess() throws Exception {
        BddLogger.then("it should grant access");

        response.andExpect(status().isOk()).andExpect(jsonPath("$", is(true)));
      }
    }

    @Nested
    class AndTargetIsChildOfAnAffiliatedInstitution {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded secondary institution attached to an affiliated primary one");
        UUID primaryId = institutionIdOf(SEEDED_PRIMARY_HAI);
        UUID secondaryId = institutionIdOf(SEEDED_SECONDARY_HAI);
        InstitutionAccessCheckRequest request =
            new InstitutionAccessCheckRequest(List.of(primaryId), List.of(secondaryId));

        BddLogger.when("calling POST /back-office/institutions/staff/access-check");
        response =
            mockMvc.perform(
                post(BASE_PATH + "/staff/access-check")
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
      }

      @Test
      void thenItShouldGrantAccessThroughTheParentAffiliation() throws Exception {
        BddLogger.then("it should grant access through the parent affiliation");

        response.andExpect(status().isOk()).andExpect(jsonPath("$", is(true)));
      }
    }

    @Nested
    class AndTargetIsUnrelatedToAffiliations {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded secondary institution unrelated to the affiliated id");
        UUID secondaryId = institutionIdOf(SEEDED_SECONDARY_HAI);
        UUID unrelatedAffiliatedId = UUID.randomUUID();
        InstitutionAccessCheckRequest request =
            new InstitutionAccessCheckRequest(List.of(unrelatedAffiliatedId), List.of(secondaryId));

        BddLogger.when("calling POST /back-office/institutions/staff/access-check");
        response =
            mockMvc.perform(
                post(BASE_PATH + "/staff/access-check")
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
      }

      @Test
      void thenItShouldDenyAccess() throws Exception {
        BddLogger.then("it should deny access");

        response.andExpect(status().isOk()).andExpect(jsonPath("$", is(false)));
      }
    }

    @Nested
    class AndTargetDoesNotExist {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a random target institution id that does not exist");
        UUID primaryId = institutionIdOf(SEEDED_PRIMARY_HAI);
        UUID unknownId = UUID.randomUUID();
        InstitutionAccessCheckRequest request =
            new InstitutionAccessCheckRequest(List.of(primaryId), List.of(unknownId));

        BddLogger.when("calling POST /back-office/institutions/staff/access-check");
        response =
            mockMvc.perform(
                post(BASE_PATH + "/staff/access-check")
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
      }

      @Test
      void thenItShouldReturnNotFound() throws Exception {
        BddLogger.then("it should return 404");

        response.andExpect(status().isNotFound());
      }
    }

    @Nested
    class AndApiKeyIsMissing {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a request without api key");
        UUID primaryId = institutionIdOf(SEEDED_PRIMARY_HAI);
        InstitutionAccessCheckRequest request =
            new InstitutionAccessCheckRequest(List.of(primaryId), List.of(primaryId));

        BddLogger.when("calling POST /back-office/institutions/staff/access-check");
        response =
            mockMvc.perform(
                post(BASE_PATH + "/staff/access-check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
      }

      @Test
      void thenItShouldReturnUnauthorized() throws Exception {
        BddLogger.then("it should return 401");

        response.andExpect(status().isUnauthorized());
      }
    }
  }

  @Nested
  class WhenResolvingStudentAccessibleIds {

    @Nested
    class AndAffiliatedInstitutionHasNoParent {

      private UUID primaryId;
      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded primary institution without parent");
        primaryId = institutionIdOf(SEEDED_PRIMARY_HAI);
        InstitutionAccessibleIdsRequest request =
            new InstitutionAccessibleIdsRequest(List.of(primaryId));

        BddLogger.when("calling POST /back-office/institutions/student/accessible-ids");
        response =
            mockMvc.perform(
                post(BASE_PATH + "/student/accessible-ids")
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
      }

      @Test
      void thenItShouldReturnOnlyItsOwnId() throws Exception {
        BddLogger.then("it should return only its own id");

        response
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$", contains(primaryId.toString())));
      }
    }

    @Nested
    class AndAffiliatedInstitutionHasAParent {

      private UUID secondaryId;
      private UUID primaryId;
      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded secondary institution attached to a primary one");
        secondaryId = institutionIdOf(SEEDED_SECONDARY_HAI);
        primaryId = institutionIdOf(SEEDED_PRIMARY_HAI);
        InstitutionAccessibleIdsRequest request =
            new InstitutionAccessibleIdsRequest(List.of(secondaryId));

        BddLogger.when("calling POST /back-office/institutions/student/accessible-ids");
        response =
            mockMvc.perform(
                post(BASE_PATH + "/student/accessible-ids")
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
      }

      @Test
      void thenItShouldReturnBothTheSecondaryAndItsPrimaryInstitution() throws Exception {
        BddLogger.then("it should return both the secondary and its primary institution");

        response
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$", contains(secondaryId.toString(), primaryId.toString())));
      }
    }

    @Nested
    class AndAffiliatedInstitutionDoesNotExist {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a random affiliated institution id that does not exist");
        UUID unknownId = UUID.randomUUID();
        InstitutionAccessibleIdsRequest request =
            new InstitutionAccessibleIdsRequest(List.of(unknownId));

        BddLogger.when("calling POST /back-office/institutions/student/accessible-ids");
        response =
            mockMvc.perform(
                post(BASE_PATH + "/student/accessible-ids")
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
      }

      @Test
      void thenItShouldReturnNotFound() throws Exception {
        BddLogger.then("it should return 404");

        response.andExpect(status().isNotFound());
      }
    }

    @Nested
    class AndApiKeyIsMissing {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a request without api key");
        UUID primaryId = institutionIdOf(SEEDED_PRIMARY_HAI);
        InstitutionAccessibleIdsRequest request =
            new InstitutionAccessibleIdsRequest(List.of(primaryId));

        BddLogger.when("calling POST /back-office/institutions/student/accessible-ids");
        response =
            mockMvc.perform(
                post(BASE_PATH + "/student/accessible-ids")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
      }

      @Test
      void thenItShouldReturnUnauthorized() throws Exception {
        BddLogger.then("it should return 401");

        response.andExpect(status().isUnauthorized());
      }
    }
  }
}
