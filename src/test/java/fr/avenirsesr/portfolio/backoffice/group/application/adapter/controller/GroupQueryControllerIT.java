package fr.avenirsesr.portfolio.backoffice.group.application.adapter.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.EPermission;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.AvenirsSecurityHeaders;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.HmacAuthenticationToken;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

class GroupQueryControllerIT extends ContainerConfigurationTest {

  private static final String BASE_PATH = "/back-office/groups";
  private static final String STAFF_EPPN = "ethan.perrin@university.com";
  private static final String SEEDED_PROGRAM_ID_SI_SCO = "10000001";
  private static final String SEEDED_PROGRAM_OPTION_ID_SI_SCO = "10000002";
  private static final String SEEDED_STUDENT_GROUP_ID_SI_SCO = "10000003";
  private static final String SEEDED_OTHER_PROGRAM_ID_SI_SCO = "10000004";
  private static final String SEEDED_OTHER_STUDENT_GROUP_ID_SI_SCO = "10000005";
  private static final String SEEDED_INSTITUTION_UAI = "0350001A";
  private static final String SEEDED_OTHER_INSTITUTION_UAI = "0330001C";

  private UUID institutionId;
  private UUID otherInstitutionId;

  @Autowired private MockMvc mockMvc;
  @Autowired private GroupRepository groupRepository;

  @Value("${security.authentication.api-key}")
  private String apiKeyValue;

  @BeforeAll
  void setup(
      @Autowired SeederRunner seederRunner,
      @Autowired InstitutionRepository institutionRepository) {
    seederRunner.run();
    institutionId = institutionRepository.findByUai(SEEDED_INSTITUTION_UAI).orElseThrow().getId();
    otherInstitutionId =
        institutionRepository.findByUai(SEEDED_OTHER_INSTITUTION_UAI).orElseThrow().getId();
  }

  private UUID groupIdOf(String idSISco) {
    return groupRepository.findByIdSISco(idSISco).orElseThrow().getId();
  }

  /** Authenticates the request as a logged-in staff granted the group read permission. */
  private static MockHttpServletRequestBuilder asStaffUser(MockHttpServletRequestBuilder builder) {
    return builder
        .header("X-Forwarded-For", "203.0.113.10")
        .with(
            authentication(
                new HmacAuthenticationToken(
                    STAFF_EPPN,
                    List.of(new SimpleGrantedAuthority(EPermission.PERM_GROUP_READ.authority())))));
  }

  @Nested
  class WhenGettingGroups {

    @Test
    void thenItShouldReturnOnlyGroupsOfInstitution_whenFilteringByInstitutionId() throws Exception {
      BddLogger.given("groups seeded across two institutions");

      BddLogger.when("calling GET /back-office/groups filtered by institutionId");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH))
                  .param("institutionId", otherInstitutionId.toString())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(2)))
          .andExpect(
              jsonPath("$[*].idSISco")
                  .value(
                      containsInAnyOrder(
                          SEEDED_OTHER_PROGRAM_ID_SI_SCO, SEEDED_OTHER_STUDENT_GROUP_ID_SI_SCO)));

      BddLogger.then("it should return only the groups belonging to that institution");
    }

    @Test
    void thenItShouldReturnOnlyChildGroups_whenFilteringByParentId() throws Exception {
      BddLogger.given("a seeded program used as the parent of a program option");
      UUID parentId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);

      BddLogger.when("calling GET /back-office/groups filtered by parentId");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH))
                  .param("parentId", parentId.toString())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(1)))
          .andExpect(jsonPath("$[0].idSISco", is(SEEDED_PROGRAM_OPTION_ID_SI_SCO)));

      BddLogger.then("it should return only the direct children of that group");
    }

    @Test
    void thenItShouldReturnOnlyGroupsOfType_whenFilteringByType() throws Exception {
      BddLogger.given("student groups seeded across two institutions");

      BddLogger.when("calling GET /back-office/groups filtered by type");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH))
                  .param("type", EGroupType.STUDENT_GROUP.name())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$[*].idSISco")
                  .value(
                      containsInAnyOrder(
                          SEEDED_STUDENT_GROUP_ID_SI_SCO, SEEDED_OTHER_STUDENT_GROUP_ID_SI_SCO)));

      BddLogger.then("it should return only groups of that type");
    }

    @Test
    void thenItShouldReturnOnlyGroupsWithinDateRange_whenFilteringByStartDateAndEndDate()
        throws Exception {
      BddLogger.given("groups seeded with different start and end dates");

      BddLogger.when("calling GET /back-office/groups filtered by startDate and endDate");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH))
                  .param("startDate", "2024-09-01")
                  .param("endDate", "2025-06-30")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$[*].idSISco")
                  .value(
                      containsInAnyOrder(
                          SEEDED_STUDENT_GROUP_ID_SI_SCO, SEEDED_OTHER_STUDENT_GROUP_ID_SI_SCO)));

      BddLogger.then(
          "it should return only groups whose start date and end date fall within the range");
    }

    @Test
    void thenItShouldReturnMatchingGroup_whenCombiningMultipleFilters() throws Exception {
      BddLogger.given("a program option belonging to the seeded institution");

      BddLogger.when("calling GET /back-office/groups filtered by institutionId and type together");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH))
                  .param("institutionId", institutionId.toString())
                  .param("type", EGroupType.PROGRAM_OPTION.name())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(1)))
          .andExpect(jsonPath("$[0].idSISco", is(SEEDED_PROGRAM_OPTION_ID_SI_SCO)));

      BddLogger.then("it should return only the group matching every filter");
    }

    @Test
    void thenItShouldReturnEmptyList_whenNoGroupMatchesTheFilters() throws Exception {
      BddLogger.given("a filter combination matched by no seeded group");

      BddLogger.when("calling GET /back-office/groups filtered by institutionId and type");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH))
                  .param("institutionId", otherInstitutionId.toString())
                  .param("type", EGroupType.PROGRAM_OPTION.name())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(0)));

      BddLogger.then("it should return an empty list");
    }

    @Test
    void thenItShouldReturnForbidden_whenStaffMissesTheReadPermission() throws Exception {
      BddLogger.given("a logged-in staff without the group read permission");

      BddLogger.when("calling GET /back-office/groups");
      mockMvc
          .perform(
              get(BASE_PATH)
                  .header("X-Forwarded-For", "203.0.113.10")
                  .with(authentication(new HmacAuthenticationToken(STAFF_EPPN)))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403");
    }
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
}
