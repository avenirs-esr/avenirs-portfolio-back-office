package fr.avenirsesr.portfolio.backoffice.group.application.adapter.controller;

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
import fr.avenirsesr.portfolio.backoffice.group.application.adapter.dto.GroupAccessCheckRequest;
import fr.avenirsesr.portfolio.backoffice.group.application.adapter.dto.GroupAccessibleIdsRequest;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
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

class GroupControllerIT extends ContainerConfigurationTest {

  private static final String BASE_PATH = "/back-office/groups";
  private static final String SEEDED_PROGRAM_ID_SI_SCO = "10000001";
  private static final String SEEDED_PROGRAM_OPTION_ID_SI_SCO = "10000002";
  private static final String SEEDED_STUDENT_GROUP_ID_SI_SCO = "10000003";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private GroupRepository groupRepository;

  @Value("${security.authentication.api-key}")
  private String apiKeyValue;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  private UUID groupIdOf(String idSiSco) {
    return groupRepository.findByIdSiSco(idSiSco).orElseThrow().getId();
  }

  @Nested
  class WhenGettingGroupById {

    @Nested
    class AndGroupHasAParent {

      private UUID programOptionId;
      private UUID programId;
      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded program option attached to a program");
        programOptionId = groupIdOf(SEEDED_PROGRAM_OPTION_ID_SI_SCO);
        programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);

        BddLogger.when("calling GET /back-office/groups/{id} with the api key only");
        response =
            mockMvc.perform(
                get(BASE_PATH + "/" + programOptionId)
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnGroupNameTypeAndParentId() throws Exception {
        BddLogger.then("it should return the group name, type and parent id");

        response
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(programOptionId.toString())))
            .andExpect(jsonPath("$.name", is("Licence Informatique - Parcours IA")))
            .andExpect(jsonPath("$.type", is("PROGRAM_OPTION")))
            .andExpect(jsonPath("$.parentId", is(programId.toString())));
      }
    }

    @Nested
    class AndGroupIsARootProgram {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded program without parent");
        UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);

        BddLogger.when("calling GET /back-office/groups/{id}");
        response =
            mockMvc.perform(
                get(BASE_PATH + "/" + programId)
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnGroupWithoutParentId() throws Exception {
        BddLogger.then("it should return the group with a null parent id");

        response
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("PROGRAM")))
            .andExpect(jsonPath("$.parentId", is(nullValue())));
      }
    }

    @Nested
    class AndGroupDoesNotExist {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a random group id that does not exist");
        UUID unknownId = UUID.randomUUID();

        BddLogger.when("calling GET /back-office/groups/{id}");
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
        UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);

        BddLogger.when("calling GET /back-office/groups/{id}");
        response =
            mockMvc.perform(get(BASE_PATH + "/" + programId).accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnUnauthorized() throws Exception {
        BddLogger.then("it should return 401");

        response.andExpect(status().isUnauthorized());
      }
    }
  }

  @Nested
  class WhenGettingProgramOfGroup {

    @Nested
    class AndWalkingUpFromAStudentGroup {

      private UUID programId;
      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded student group whose grand-parent is a program");
        UUID studentGroupId = groupIdOf(SEEDED_STUDENT_GROUP_ID_SI_SCO);
        programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);

        BddLogger.when("calling GET /back-office/groups/{id}/program");
        response =
            mockMvc.perform(
                get(BASE_PATH + "/" + studentGroupId + "/program")
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnTheTopmostAncestor() throws Exception {
        BddLogger.then("it should return the topmost ancestor, which is the program");

        response
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(programId.toString())))
            .andExpect(jsonPath("$.name", is("Licence Informatique")))
            .andExpect(jsonPath("$.type", is("PROGRAM")))
            .andExpect(jsonPath("$.parentId", is(nullValue())));
      }
    }

    @Nested
    class AndWalkingUpFromAProgramOption {

      private UUID programId;
      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded program option whose parent is a program");
        UUID programOptionId = groupIdOf(SEEDED_PROGRAM_OPTION_ID_SI_SCO);
        programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);

        BddLogger.when("calling GET /back-office/groups/{id}/program");
        response =
            mockMvc.perform(
                get(BASE_PATH + "/" + programOptionId + "/program")
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnTheParentProgram() throws Exception {
        BddLogger.then("it should return the parent program");

        response
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(programId.toString())))
            .andExpect(jsonPath("$.type", is("PROGRAM")));
      }
    }

    @Nested
    class AndGroupIsAlreadyAProgram {

      private UUID programId;
      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded program");
        programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);

        BddLogger.when("calling GET /back-office/groups/{id}/program");
        response =
            mockMvc.perform(
                get(BASE_PATH + "/" + programId + "/program")
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnTheProgramItself() throws Exception {
        BddLogger.then("it should return the program itself");

        response.andExpect(status().isOk()).andExpect(jsonPath("$.id", is(programId.toString())));
      }
    }

    @Nested
    class AndGroupDoesNotExist {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a random group id that does not exist");
        UUID unknownId = UUID.randomUUID();

        BddLogger.when("calling GET /back-office/groups/{id}/program");
        response =
            mockMvc.perform(
                get(BASE_PATH + "/" + unknownId + "/program")
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
  }

  @Nested
  class WhenCheckingStaffAccess {

    @Nested
    class AndTargetIdIsDirectlyAffiliated {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded program matching an affiliated id");
        UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);
        GroupAccessCheckRequest request =
            new GroupAccessCheckRequest(List.of(programId), List.of(programId));

        BddLogger.when("calling POST /back-office/groups/staff/access-check");
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
    class AndTargetIsGrandchildOfAnAffiliatedGroup {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded student group whose grand-parent is an affiliated program");
        UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);
        UUID studentGroupId = groupIdOf(SEEDED_STUDENT_GROUP_ID_SI_SCO);
        GroupAccessCheckRequest request =
            new GroupAccessCheckRequest(List.of(programId), List.of(studentGroupId));

        BddLogger.when("calling POST /back-office/groups/staff/access-check");
        response =
            mockMvc.perform(
                post(BASE_PATH + "/staff/access-check")
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
      }

      @Test
      void thenItShouldGrantAccessThroughTheAncestorChain() throws Exception {
        BddLogger.then("it should grant access through the ancestor chain");

        response.andExpect(status().isOk()).andExpect(jsonPath("$", is(true)));
      }
    }

    @Nested
    class AndTargetIsUnrelatedToAffiliations {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded student group unrelated to the affiliated id");
        UUID studentGroupId = groupIdOf(SEEDED_STUDENT_GROUP_ID_SI_SCO);
        UUID unrelatedAffiliatedId = UUID.randomUUID();
        GroupAccessCheckRequest request =
            new GroupAccessCheckRequest(List.of(unrelatedAffiliatedId), List.of(studentGroupId));

        BddLogger.when("calling POST /back-office/groups/staff/access-check");
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
        BddLogger.given("a random target group id that does not exist");
        UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);
        UUID unknownId = UUID.randomUUID();
        GroupAccessCheckRequest request =
            new GroupAccessCheckRequest(List.of(programId), List.of(unknownId));

        BddLogger.when("calling POST /back-office/groups/staff/access-check");
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
        UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);
        GroupAccessCheckRequest request =
            new GroupAccessCheckRequest(List.of(programId), List.of(programId));

        BddLogger.when("calling POST /back-office/groups/staff/access-check");
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
    class AndAffiliatedGroupHasNoParent {

      private UUID programId;
      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded program without parent");
        programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);
        GroupAccessibleIdsRequest request = new GroupAccessibleIdsRequest(List.of(programId));

        BddLogger.when("calling POST /back-office/groups/student/accessible-ids");
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
            .andExpect(jsonPath("$", contains(programId.toString())));
      }
    }

    @Nested
    class AndAffiliatedGroupHasParents {

      private UUID studentGroupId;
      private UUID programOptionId;
      private UUID programId;
      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given(
            "a seeded student group whose ancestors are a program option and a program");
        studentGroupId = groupIdOf(SEEDED_STUDENT_GROUP_ID_SI_SCO);
        programOptionId = groupIdOf(SEEDED_PROGRAM_OPTION_ID_SI_SCO);
        programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);
        GroupAccessibleIdsRequest request = new GroupAccessibleIdsRequest(List.of(studentGroupId));

        BddLogger.when("calling POST /back-office/groups/student/accessible-ids");
        response =
            mockMvc.perform(
                post(BASE_PATH + "/student/accessible-ids")
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
      }

      @Test
      void thenItShouldReturnTheGroupIdAndEveryAncestor() throws Exception {
        BddLogger.then("it should return the group id and every ancestor up to the program");

        response
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(
                jsonPath(
                    "$",
                    contains(
                        studentGroupId.toString(),
                        programOptionId.toString(),
                        programId.toString())));
      }
    }

    @Nested
    class AndAffiliatedGroupDoesNotExist {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a random affiliated group id that does not exist");
        UUID unknownId = UUID.randomUUID();
        GroupAccessibleIdsRequest request = new GroupAccessibleIdsRequest(List.of(unknownId));

        BddLogger.when("calling POST /back-office/groups/student/accessible-ids");
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
        UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);
        GroupAccessibleIdsRequest request = new GroupAccessibleIdsRequest(List.of(programId));

        BddLogger.when("calling POST /back-office/groups/student/accessible-ids");
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
