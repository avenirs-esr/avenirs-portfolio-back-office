package fr.avenirsesr.portfolio.backoffice.group.application.adapter.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationData;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationImportSummary;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
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
  private static final String OTHER_STAFF_EPPN = "camille.fournier@university.com";
  private static final int SEEDED_GROUPE_A_USERS_COUNT = 80;
  private static final String SEEDED_PROGRAM_ID_SI_SCO = "10000001";
  private static final String SEEDED_PROGRAM_OPTION_ID_SI_SCO = "10000002";
  private static final String SEEDED_STUDENT_GROUP_ID_SI_SCO = "10000003";
  private static final String SEEDED_PARENT_NAME = "Licence Informatique";
  private static final String SEEDED_INSTITUTION_ID = "14baa158-22f9-391a-bf7d-676022151b65";
  private static final String SEEDED_INSTITUTION_NAME = "Université de Rennes";
  private static final String RENNES_UAI = "0350001A";
  private static final String GROUPE_A_STUDENT_EPPN = "arthur.lambert@university.com";
  private static final String UNGROUPED_STAFF_EPPN = "zoe.cousin@university.com";

  @Autowired private MockMvc mockMvc;
  @Autowired private GroupRepository groupRepository;
  @Autowired private ExternalUserAffiliationService externalUserAffiliationService;

  @Value("${security.authentication.api-key}")
  private String apiKeyValue;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  private UUID groupIdOf(String idSISco) {
    return groupRepository.findByIdSISco(idSISco).orElseThrow().getId();
  }

  /**
   * Adds affiliations for the duration of the test, which the enclosing transaction rolls back.
   * Imports report their failures instead of throwing, so the summary is asserted: a rejected
   * affiliation would otherwise leave the test asserting the untouched seeded counts.
   */
  private void affiliate(ExternalUserAffiliationData... affiliations) {
    ExternalUserAffiliationImportSummary summary =
        externalUserAffiliationService.createAll(List.of(affiliations));

    assertThat(summary.failed()).isEmpty();
    assertThat(summary.created()).hasSize(affiliations.length);
  }

  /** Authenticates the request as a logged-in staff granted the group read permission. */
  private static MockHttpServletRequestBuilder asStaffUser(
      MockHttpServletRequestBuilder builder, String eppn) {
    return builder
        .header("X-Forwarded-For", "203.0.113.10")
        .with(
            authentication(
                new HmacAuthenticationToken(
                    eppn,
                    List.of(new SimpleGrantedAuthority(EPermission.PERM_GROUP_READ.authority())))));
  }

  @Nested
  class WhenGettingGroups {

    @Test
    void thenItShouldReturnTheGroupsOfTheStaffInstitutionSortedAlphabetically() throws Exception {
      BddLogger.given("a staff attached to the Rennes institution, with groups in two of them");

      BddLogger.when("calling GET /back-office/groups");
      mockMvc
          .perform(asStaffUser(get(BASE_PATH), STAFF_EPPN).accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", hasSize(3)))
          .andExpect(jsonPath("$.data[0].name", is("Groupe A")))
          .andExpect(jsonPath("$.data[1].name", is("Licence Informatique")))
          .andExpect(jsonPath("$.data[2].name", is("Licence Informatique - Parcours IA")))
          .andExpect(jsonPath("$.data[2].type", is("PROGRAM_OPTION")))
          .andExpect(jsonPath("$.data[2].parent.name", is(SEEDED_PARENT_NAME)))
          .andExpect(jsonPath("$.page.page", is(0)))
          .andExpect(jsonPath("$.page.totalElements", is(3)))
          .andExpect(jsonPath("$.page.totalPages", is(1)));

      BddLogger.then(
          "it should return only the groups of the institution it is attached to, ordered by"
              + " name");
    }

    @Test
    void thenItShouldReturnTheNumberOfAffiliatedUsersOfEachGroup() throws Exception {
      BddLogger.given(
          "a student group holding affiliated users, below a program option itself below a"
              + " program, neither holding any affiliated user of its own");

      BddLogger.when("calling GET /back-office/groups");
      mockMvc
          .perform(asStaffUser(get(BASE_PATH), STAFF_EPPN).accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[0].name", is("Groupe A")))
          .andExpect(jsonPath("$.data[0].usersCount", is(SEEDED_GROUPE_A_USERS_COUNT)))
          .andExpect(jsonPath("$.data[1].name", is("Licence Informatique")))
          .andExpect(jsonPath("$.data[1].usersCount", is(SEEDED_GROUPE_A_USERS_COUNT)))
          .andExpect(jsonPath("$.data[2].name", is("Licence Informatique - Parcours IA")))
          .andExpect(jsonPath("$.data[2].usersCount", is(SEEDED_GROUPE_A_USERS_COUNT)));

      BddLogger.then(
          "it should report, for each group, the count of distinct users affiliated to it or to"
              + " any of its descendants, however deep");
    }

    @Test
    void thenItShouldCountSharedUsersOnce_whenAUserIsAffiliatedToAGroupAndItsAncestor()
        throws Exception {
      BddLogger.given(
          "a student of Groupe A also affiliated to the program above it, and a staff affiliated"
              + " to that program only");
      affiliate(
          new ExternalUserAffiliationData(
              GROUPE_A_STUDENT_EPPN, RENNES_UAI, SEEDED_PROGRAM_ID_SI_SCO, EUserCategory.STUDENT),
          new ExternalUserAffiliationData(
              UNGROUPED_STAFF_EPPN, RENNES_UAI, SEEDED_PROGRAM_ID_SI_SCO, EUserCategory.STAFF));

      BddLogger.when("calling GET /back-office/groups");
      mockMvc
          .perform(asStaffUser(get(BASE_PATH), STAFF_EPPN).accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[0].name", is("Groupe A")))
          .andExpect(jsonPath("$.data[0].usersCount", is(SEEDED_GROUPE_A_USERS_COUNT)))
          .andExpect(jsonPath("$.data[1].name", is("Licence Informatique")))
          .andExpect(jsonPath("$.data[1].usersCount", is(SEEDED_GROUPE_A_USERS_COUNT + 1)));

      BddLogger.then(
          "it should count the shared student once, so the program reports its sub-tree plus the"
              + " staff only");
    }

    @Test
    void thenItShouldReturnTheGroupsOfTheOtherInstitution_whenAnotherStaffAsks() throws Exception {
      BddLogger.given("a staff of the Bordeaux institution");

      BddLogger.when("calling GET /back-office/groups");
      mockMvc
          .perform(asStaffUser(get(BASE_PATH), OTHER_STAFF_EPPN).accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", hasSize(2)))
          .andExpect(jsonPath("$.data[0].name", is("Groupe B")))
          .andExpect(jsonPath("$.data[1].name", is("Licence Mathematiques")));

      BddLogger.then("it should return only the groups of the institution it is attached to");
    }

    @Test
    void thenItShouldReturnMatchingGroups_whenFilteringByKeyword() throws Exception {
      BddLogger.given("a staff of the Rennes institution");

      BddLogger.when("calling GET /back-office/groups with a keyword");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("keyword", "licence")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", hasSize(2)))
          .andExpect(
              jsonPath("$.data[*].name")
                  .value(contains("Licence Informatique", "Licence Informatique - Parcours IA")));

      BddLogger.then("it should return only the groups whose name contains the keyword");
    }

    @Test
    void thenItShouldReturnOnlyGroupsOfType_whenFilteringByType() throws Exception {
      BddLogger.given("a staff of the Rennes institution");

      BddLogger.when("calling GET /back-office/groups filtered by type");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("types", EGroupType.STUDENT_GROUP.name())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", hasSize(1)))
          .andExpect(jsonPath("$.data[0].name", is("Groupe A")));

      BddLogger.then("it should return only the groups of that type");
    }

    @Test
    void thenItShouldReturnTheRequestedPage_whenPaginating() throws Exception {
      BddLogger.given("a staff of the Rennes institution owning three groups");

      BddLogger.when("calling GET /back-office/groups with a page size of two");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("page", "1")
                  .param("pageSize", "2")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", hasSize(1)))
          .andExpect(jsonPath("$.data[0].name", is("Licence Informatique - Parcours IA")))
          .andExpect(jsonPath("$.page.page", is(1)))
          .andExpect(jsonPath("$.page.pageSize", is(2)))
          .andExpect(jsonPath("$.page.totalElements", is(3)))
          .andExpect(jsonPath("$.page.totalPages", is(2)));

      BddLogger.then("it should return the second page and its page information");
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

        BddLogger.when("calling GET /back-office/groups/{id} as a staff of the institution");
        response =
            mockMvc.perform(
                asStaffUser(get(BASE_PATH + "/" + programOptionId), STAFF_EPPN)
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
            .andExpect(jsonPath("$.institution.id", is(SEEDED_INSTITUTION_ID)))
            .andExpect(jsonPath("$.institution.name", is(SEEDED_INSTITUTION_NAME)))
            .andExpect(jsonPath("$.parent.id", is(programId.toString())))
            .andExpect(jsonPath("$.parent.name", is(SEEDED_PARENT_NAME)))
            .andExpect(jsonPath("$.parent.type", is("PROGRAM")))
            .andExpect(jsonPath("$.usersCount", is(SEEDED_GROUPE_A_USERS_COUNT)));
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
                asStaffUser(get(BASE_PATH + "/" + programId), STAFF_EPPN)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnGroupWithoutParentId() throws Exception {
        BddLogger.then("it should return the group with a null parent id");

        response
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("PROGRAM")))
            .andExpect(jsonPath("$.institution.id", is(SEEDED_INSTITUTION_ID)))
            .andExpect(jsonPath("$.institution.name", is(SEEDED_INSTITUTION_NAME)))
            .andExpect(jsonPath("$.parent", is(nullValue())))
            .andExpect(jsonPath("$.usersCount", is(SEEDED_GROUPE_A_USERS_COUNT)));
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
                asStaffUser(get(BASE_PATH + "/" + unknownId), STAFF_EPPN)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnNotFound() throws Exception {
        BddLogger.then("it should return 404");

        response.andExpect(status().isNotFound());
      }
    }

    @Nested
    class AndGroupIsOutOfTheStaffInstitution {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded Rennes group and a staff of the Bordeaux institution");
        UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);

        BddLogger.when("calling GET /back-office/groups/{id}");
        response =
            mockMvc.perform(
                asStaffUser(get(BASE_PATH + "/" + programId), OTHER_STAFF_EPPN)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnForbidden() throws Exception {
        BddLogger.then("it should return 403");

        response.andExpect(status().isForbidden());
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
            .andExpect(jsonPath("$.institution.id", is(SEEDED_INSTITUTION_ID)))
            .andExpect(jsonPath("$.institution.name", is(SEEDED_INSTITUTION_NAME)))
            .andExpect(jsonPath("$.parent", is(nullValue())))
            .andExpect(jsonPath("$.usersCount", is(SEEDED_GROUPE_A_USERS_COUNT)));
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
            .andExpect(jsonPath("$.type", is("PROGRAM")))
            .andExpect(jsonPath("$.institution.id", is(SEEDED_INSTITUTION_ID)))
            .andExpect(jsonPath("$.institution.name", is(SEEDED_INSTITUTION_NAME)))
            .andExpect(jsonPath("$.parent", is(nullValue())))
            .andExpect(jsonPath("$.usersCount", is(SEEDED_GROUPE_A_USERS_COUNT)));
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
  class WhenGettingSubGroupsOfGroup {

    @Nested
    class AndGroupHasChildren {

      private UUID programOptionId;
      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded program whose only direct child is a program option");
        UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);
        programOptionId = groupIdOf(SEEDED_PROGRAM_OPTION_ID_SI_SCO);

        BddLogger.when("calling GET /back-office/groups/{id}/sub-groups");
        response =
            mockMvc.perform(
                asStaffUser(get(BASE_PATH + "/" + programId + "/sub-groups"), STAFF_EPPN)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnItsDirectChildrenOnly() throws Exception {
        BddLogger.then(
            "it should list the program option only, not the student group below it, while still"
                + " counting that student group's users in the program option count");

        response
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(programOptionId.toString())))
            .andExpect(jsonPath("$[0].name", is("Licence Informatique - Parcours IA")))
            .andExpect(jsonPath("$[0].type", is("PROGRAM_OPTION")))
            .andExpect(jsonPath("$[0].parent.name", is(SEEDED_PARENT_NAME)))
            .andExpect(jsonPath("$[0].usersCount", is(SEEDED_GROUPE_A_USERS_COUNT)));
      }
    }

    @Nested
    class AndChildrenHoldAffiliatedUsers {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded program option whose child student group holds affiliated users");
        UUID programOptionId = groupIdOf(SEEDED_PROGRAM_OPTION_ID_SI_SCO);

        BddLogger.when("calling GET /back-office/groups/{id}/sub-groups");
        response =
            mockMvc.perform(
                asStaffUser(get(BASE_PATH + "/" + programOptionId + "/sub-groups"), STAFF_EPPN)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnTheNumberOfAffiliatedUsersOfEachSubGroup() throws Exception {
        BddLogger.then("it should report the count of distinct users affiliated to each sub-group");

        response
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is("Groupe A")))
            .andExpect(jsonPath("$[0].type", is("STUDENT_GROUP")))
            .andExpect(jsonPath("$[0].usersCount", is(SEEDED_GROUPE_A_USERS_COUNT)));
      }
    }

    @Nested
    class AndGroupHasNoChild {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a seeded student group, which cannot have children");
        UUID studentGroupId = groupIdOf(SEEDED_STUDENT_GROUP_ID_SI_SCO);

        BddLogger.when("calling GET /back-office/groups/{id}/sub-groups");
        response =
            mockMvc.perform(
                asStaffUser(get(BASE_PATH + "/" + studentGroupId + "/sub-groups"), STAFF_EPPN)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnAnEmptyList() throws Exception {
        BddLogger.then("it should return an empty list");

        response.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
      }
    }

    @Nested
    class AndGroupDoesNotExist {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a random group id that does not exist");
        UUID unknownId = UUID.randomUUID();

        BddLogger.when("calling GET /back-office/groups/{id}/sub-groups");
        response =
            mockMvc.perform(
                asStaffUser(get(BASE_PATH + "/" + unknownId + "/sub-groups"), STAFF_EPPN)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnNotFound() throws Exception {
        BddLogger.then("it should return 404");

        response.andExpect(status().isNotFound());
      }
    }

    @Nested
    class AndGroupBelongsToAnotherInstitution {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a staff of the Bordeaux institution and a group of the Rennes one");
        UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);

        BddLogger.when("calling GET /back-office/groups/{id}/sub-groups");
        response =
            mockMvc.perform(
                asStaffUser(get(BASE_PATH + "/" + programId + "/sub-groups"), OTHER_STAFF_EPPN)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnForbidden() throws Exception {
        BddLogger.then("it should return 403, the staff not being affiliated to that institution");

        response.andExpect(status().isForbidden());
      }
    }

    @Nested
    class AndStaffMissesTheReadPermission {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a logged-in staff without the group read permission");
        UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);

        BddLogger.when("calling GET /back-office/groups/{id}/sub-groups");
        response =
            mockMvc.perform(
                get(BASE_PATH + "/" + programId + "/sub-groups")
                    .header("X-Forwarded-For", "203.0.113.10")
                    .with(authentication(new HmacAuthenticationToken(STAFF_EPPN)))
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnForbidden() throws Exception {
        BddLogger.then("it should return 403");

        response.andExpect(status().isForbidden());
      }
    }
  }
}
