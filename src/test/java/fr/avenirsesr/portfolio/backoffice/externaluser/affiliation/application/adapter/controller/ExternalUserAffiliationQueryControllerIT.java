package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.CofolioProfile;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.client.CofolioProfileClient;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.client.SecurityClient;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.EPermission;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.ERole;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.HmacAuthenticationToken;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.List;
import java.util.Map;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

class ExternalUserAffiliationQueryControllerIT extends ContainerConfigurationTest {

  private static final String BASE_PATH = "/back-office/external-users/affiliations";
  private static final String EXTERNAL_USERS_PATH = "/back-office/external-users/";
  private static final String STAFF_EPPN = "ethan.perrin@university.com";
  private static final String SEEDED_STUDENT_EPPN = "lucas.tessier@university.com";
  private static final String SEEDED_RENNES_UAI = "0350001A";
  private static final String SEEDED_BORDEAUX_UAI = "0330001C";
  private static final String SEEDED_PROGRAM_ID_SI_SCO = "10000001";
  private static final String SEEDED_BORDEAUX_STUDENT_EPPN = "sophie.charpentier@university.com";
  private static final String SEEDED_OPTION_ID_SI_SCO = "10000002";
  private static final String SEEDED_STUDENT_GROUP_ID_SI_SCO = "10000003";
  private static final String SEEDED_OTHER_STUDENT_GROUP_ID_SI_SCO = "10000005";

  /**
   * Counts of the seeded fixtures in affiliated users, each of them counted once however many
   * affiliations it holds. Without filter, the listing is scoped to the institution of the staff.
   */
  private static final int SEEDED_RENNES_STAFF_USERS_COUNT = 28;

  private static final int SEEDED_RENNES_USERS_COUNT = 108;
  private static final int SEEDED_GROUPE_A_USERS_COUNT = 80;
  private static final int SEEDED_TESSIER_USERS_COUNT = 2;

  @Autowired private MockMvc mockMvc;
  @Autowired private ExternalUserRepository externalUserRepository;
  @Autowired private InstitutionRepository institutionRepository;
  @Autowired private GroupRepository groupRepository;

  @Autowired private ExternalUserAffiliationService externalUserAffiliationService;

  @MockitoBean private CofolioProfileClient cofolioProfileClient;
  @MockitoBean private SecurityClient securityClient;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  /** Nobody logged in to Cofolio unless a test says otherwise. */
  @BeforeEach
  void stubCofolioProfiles() {
    when(cofolioProfileClient.findByEppns(any())).thenReturn(Map.of());
  }

  private UUID externalUserIdOf(String eppn) {
    return externalUserRepository.findByEppn(eppn).orElseThrow().getId();
  }

  private UUID institutionIdOf(String uai) {
    return institutionRepository.findByUai(uai).orElseThrow().getId();
  }

  private UUID groupIdOf(String idSISco) {
    return groupRepository.findByIdSISco(idSISco).orElseThrow().getId();
  }

  /**
   * Scopes the staff to the seeded program, which covers the option and the student group below it.
   * Rolled back with the test, so the other ones keep seeing a staff without any group.
   */
  private void affiliateStaffToSeededProgram() {
    externalUserAffiliationService.addAffiliation(
        externalUserIdOf(STAFF_EPPN),
        institutionIdOf(SEEDED_RENNES_UAI),
        groupIdOf(SEEDED_PROGRAM_ID_SI_SCO),
        EUserCategory.STAFF);
  }

  /** Binds the seeded student to the program too, on top of Groupe A two levels below it. */
  private void affiliateStudentToSeededProgram() {
    externalUserAffiliationService.addAffiliation(
        externalUserIdOf(SEEDED_STUDENT_EPPN),
        institutionIdOf(SEEDED_RENNES_UAI),
        groupIdOf(SEEDED_PROGRAM_ID_SI_SCO),
        EUserCategory.STUDENT);
  }

  private static int totalElementsOf(MvcResult result) throws Exception {
    return JsonPath.read(result.getResponse().getContentAsString(), "$.page.totalElements");
  }

  /** Authenticates the request as a logged-in staff granted the external user read permission. */
  private static MockHttpServletRequestBuilder asStaffUser(
      MockHttpServletRequestBuilder builder, String eppn) {
    return builder
        .header("X-Forwarded-For", "203.0.113.10")
        .with(
            authentication(
                new HmacAuthenticationToken(
                    eppn,
                    List.of(
                        new SimpleGrantedAuthority(
                            EPermission.PERM_EXTERNAL_USER_READ.authority())))));
  }

  @Nested
  class WhenGettingAffiliations {

    @Test
    void thenItShouldReturnTheFirstPageSortedByLastNameThenFirstName() throws Exception {
      BddLogger.given(
          "the seeded affiliations of the staff institution, the first last name alphabetically"
              + " being AMU");

      BddLogger.when("calling GET /back-office/external-users/affiliations");
      mockMvc
          .perform(asStaffUser(get(BASE_PATH), STAFF_EPPN).accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", hasSize(8)))
          .andExpect(jsonPath("$.data[0].lastName", is("AMU")))
          .andExpect(jsonPath("$.data[0].firstName", is("Ens1")))
          .andExpect(jsonPath("$.data[1].lastName", is("AMU")))
          .andExpect(jsonPath("$.data[1].firstName", is("Ens3")))
          .andExpect(jsonPath("$.page.page", is(0)))
          .andExpect(jsonPath("$.page.pageSize", is(8)))
          .andExpect(jsonPath("$.page.totalElements", is(SEEDED_RENNES_USERS_COUNT)));

      BddLogger.then(
          "it should return the default page, ordered by last name then first name, limited to the"
              + " staff scope");
    }

    @Test
    void thenItShouldOnlyReturnTheUsersOfTheStaffScope_whenNoInstitutionNorGroupIsFiltered()
        throws Exception {
      BddLogger.given("a staff affiliated to Rennes and a seeded student affiliated to Bordeaux");

      BddLogger.when("calling GET /back-office/external-users/affiliations without any filter");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("pageSize", "100")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.data[*].institutions")
                  .value(everyItem(hasItem(hasEntry("name", "Université de Rennes")))));
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("keyword", "charpentier")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.page.totalElements", is(0)));

      BddLogger.then("it should not return the users the staff has no right on");
    }

    @Test
    void thenItShouldReturnTheSameUsersAsTheStaffInstitution_whenNoInstitutionNorGroupIsFiltered()
        throws Exception {
      BddLogger.given("a staff affiliated to Rennes only");

      BddLogger.when(
          "calling GET /back-office/external-users/affiliations with and without the institution");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("institutionIds", institutionIdOf(SEEDED_RENNES_UAI).toString())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.page.totalElements", is(SEEDED_RENNES_USERS_COUNT)));
      mockMvc
          .perform(asStaffUser(get(BASE_PATH), STAFF_EPPN).accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.page.totalElements", is(SEEDED_RENNES_USERS_COUNT)));

      BddLogger.then("it should return the same number of users");
    }

    @Test
    void thenItShouldStayWithinTheStaffScope_whenTheStaffIsAlsoAffiliatedToAGroup()
        throws Exception {
      BddLogger.given("a staff affiliated to Rennes and to a seeded program");
      affiliateStaffToSeededProgram();

      BddLogger.when("calling GET /back-office/external-users/affiliations without any filter");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("pageSize", "100")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.data[*].institutions")
                  .value(everyItem(hasItem(hasEntry("name", "Université de Rennes")))));

      BddLogger.then("it should only return users of the institution of the staff");
    }

    @Test
    void thenItShouldNotNarrowTheGroups_whenOnlyTheInstitutionIsFiltered() throws Exception {
      BddLogger.given("a staff affiliated to Rennes and to a seeded program");
      affiliateStaffToSeededProgram();
      String rennesId = institutionIdOf(SEEDED_RENNES_UAI).toString();
      String programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO).toString();

      BddLogger.when(
          "calling GET /back-office/external-users/affiliations with and without the program");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("institutionIds", rennesId)
                  .param("groupIds", programId)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andDo(
              explicit ->
                  mockMvc
                      .perform(
                          asStaffUser(get(BASE_PATH), STAFF_EPPN)
                              .param("institutionIds", rennesId)
                              .accept(MediaType.APPLICATION_JSON))
                      .andExpect(status().isOk())
                      .andExpect(
                          jsonPath(
                              "$.page.totalElements", greaterThan(totalElementsOf(explicit)))));

      BddLogger.then(
          "it should not narrow the search on any group, unlike when filtering on the program");
    }

    @Test
    void thenItShouldDefaultTheInstitutionsToTheStaffOnes_whenOnlyTheGroupIsFiltered()
        throws Exception {
      BddLogger.given("a staff affiliated to Rennes and to a seeded program");
      affiliateStaffToSeededProgram();
      String rennesId = institutionIdOf(SEEDED_RENNES_UAI).toString();
      String groupId = groupIdOf(SEEDED_STUDENT_GROUP_ID_SI_SCO).toString();

      BddLogger.when(
          "calling GET /back-office/external-users/affiliations with and without the institution");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("institutionIds", rennesId)
                  .param("groupIds", groupId)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andDo(
              explicit ->
                  mockMvc
                      .perform(
                          asStaffUser(get(BASE_PATH), STAFF_EPPN)
                              .param("groupIds", groupId)
                              .accept(MediaType.APPLICATION_JSON))
                      .andExpect(status().isOk())
                      .andExpect(jsonPath("$.page.totalElements", is(totalElementsOf(explicit)))));

      BddLogger.then("it should return the same users as when filtering on the staff institutions");
    }

    @Test
    void thenItShouldNotNarrowTheSearch_whenNoFilterIsGivenAndTheUserHasNoStaffAffiliation()
        throws Exception {
      BddLogger.given("a logged-in user holding the permission but no staff affiliation");

      BddLogger.when("calling GET /back-office/external-users/affiliations without any filter");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), SEEDED_STUDENT_EPPN).accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", hasSize(8)));

      BddLogger.then("it should return the default page, as no scope is defined for that user");
    }

    @Test
    void thenItShouldReturnTheIdentityOfTheAffiliatedUserAndTheLabelsOfItsScope() throws Exception {
      BddLogger.given(
          "a student affiliated to two institutions and to a student group of each of them");
      affiliateStaffToSeededProgram();

      BddLogger.when("calling GET /back-office/external-users/affiliations filtered by group");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("keyword", "tessier")
                  .param("groupIds", groupIdOf(SEEDED_STUDENT_GROUP_ID_SI_SCO).toString())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", hasSize(1)))
          .andExpect(jsonPath("$.data[0].id", is(externalUserIdOf(SEEDED_STUDENT_EPPN).toString())))
          .andExpect(jsonPath("$.data[0].firstName", is("Lucas")))
          .andExpect(jsonPath("$.data[0].lastName", is("Tessier")))
          .andExpect(jsonPath("$.data[0].categories", contains("STUDENT")))
          .andExpect(
              jsonPath(
                  "$.data[0].institutions[*].name",
                  contains("Université de Bordeaux", "Université de Rennes")))
          .andExpect(
              jsonPath(
                  "$.data[0].groups[*].name",
                  contains(
                      "Groupe A",
                      "Groupe B",
                      "Licence Informatique",
                      "Licence Informatique - Parcours IA",
                      "Licence Mathematiques")))
          .andExpect(jsonPath("$.data[0].groups[0].type", is("STUDENT_GROUP")))
          .andExpect(
              jsonPath("$.data[0].groups[0].parent.name", is("Licence Informatique - Parcours IA")))
          .andExpect(jsonPath("$.data[0].groups[0].usersCount", is(SEEDED_GROUPE_A_USERS_COUNT)));

      BddLogger.then(
          "it should return the user identity once, with every institution it is affiliated to and"
              + " every group it belongs to, the ancestors of the hierarchy included");
    }

    @Test
    void thenItShouldReturnMatchingAffiliations_whenSearchingByLastName() throws Exception {
      BddLogger.given("two seeded users sharing the Tessier last name");

      BddLogger.when("calling GET /back-office/external-users/affiliations with a keyword");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("keyword", "TESSIER")
                  .param("pageSize", "20")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", hasSize(SEEDED_TESSIER_USERS_COUNT)))
          .andExpect(jsonPath("$.data[*].lastName", everyItem(is("Tessier"))))
          .andExpect(jsonPath("$.page.totalElements", is(SEEDED_TESSIER_USERS_COUNT)));

      BddLogger.then("it should return both of them, the search ignoring the case");
    }

    @Test
    void thenItShouldReturnMatchingAffiliations_whenSearchingByFirstName() throws Exception {
      BddLogger.given("two seeded users sharing the Lucas first name");

      BddLogger.when("calling GET /back-office/external-users/affiliations with a keyword");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("keyword", "lucas")
                  .param("pageSize", "20")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", hasSize(SEEDED_TESSIER_USERS_COUNT)))
          .andExpect(jsonPath("$.data[*].firstName", everyItem(is("Lucas"))));

      BddLogger.then("it should match the first name as well as the last name");
    }

    @Test
    void thenItShouldReturnMatchingAffiliations_whenSearchingByEmail() throws Exception {
      BddLogger.given("the email of the seeded student, shared by both Lucas Tessier accounts");
      String email =
          externalUserRepository.findByEppn(SEEDED_STUDENT_EPPN).orElseThrow().getEmail();

      BddLogger.when("calling GET /back-office/external-users/affiliations with it in upper case");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("keyword", email.toUpperCase())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", hasSize(SEEDED_TESSIER_USERS_COUNT)))
          .andExpect(
              jsonPath("$.data[*].id", hasItem(externalUserIdOf(SEEDED_STUDENT_EPPN).toString())))
          .andExpect(jsonPath("$.data[*].lastName", everyItem(is("Tessier"))));

      BddLogger.then("it should match the email as well as the names, ignoring the case");
    }

    @Test
    void thenItShouldMatchTheSpecialCharactersLiterally_whenSearchingByEmail() throws Exception {
      BddLogger.given("a seeded email whose dots are turned into underscores");
      String keyword =
          externalUserRepository
              .findByEppn(SEEDED_STUDENT_EPPN)
              .orElseThrow()
              .getEmail()
              .replace('.', '_');

      BddLogger.when("calling GET /back-office/external-users/affiliations with that keyword");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("keyword", keyword)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.page.totalElements", is(0)));

      BddLogger.then("it should find nobody, the underscore matching itself only");
    }

    @Test
    void thenItShouldReturnOnlyStaffAffiliations_whenFilteringByCategory() throws Exception {
      BddLogger.given("the seeded affiliations, holding both staff and student ones");

      BddLogger.when("calling GET /back-office/external-users/affiliations filtered by category");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("categories", EUserCategory.STAFF.name())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[*].categories[*]", everyItem(is("STAFF"))))
          .andExpect(jsonPath("$.page.totalElements", is(SEEDED_RENNES_STAFF_USERS_COUNT)));

      BddLogger.then(
          "it should return only the users holding an affiliation of that category, within the"
              + " scope of the staff");
    }

    @Test
    void thenItShouldReturnOnlyTheAffiliationsOfThatInstitution_whenFilteringByInstitution()
        throws Exception {
      BddLogger.given("the seeded affiliations, spread over two institutions");

      BddLogger.when(
          "calling GET /back-office/external-users/affiliations filtered by institution");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("institutionIds", institutionIdOf(SEEDED_RENNES_UAI).toString())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.data[*].institutions")
                  .value(everyItem(hasItem(hasEntry("name", "Université de Rennes")))))
          .andExpect(jsonPath("$.page.totalElements", is(SEEDED_RENNES_USERS_COUNT)));

      BddLogger.then("it should return only the users affiliated to that institution");
    }

    @Test
    void thenItShouldReturnOnlyTheAffiliationsOfThatGroup_whenFilteringByGroup() throws Exception {
      BddLogger.given("the seeded affiliations, some of them bound to no group at all");
      affiliateStaffToSeededProgram();

      BddLogger.when("calling GET /back-office/external-users/affiliations filtered by group");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("groupIds", groupIdOf(SEEDED_STUDENT_GROUP_ID_SI_SCO).toString())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.data[*].groups").value(everyItem(hasItem(hasEntry("name", "Groupe A")))))
          .andExpect(jsonPath("$.page.totalElements", is(SEEDED_GROUPE_A_USERS_COUNT)));

      BddLogger.then(
          "it should return only the users bound to that group, reachable through the program the"
              + " staff is affiliated to");
    }

    @Test
    void thenItShouldReturnTheUsersOfTheDescendantGroups_whenFilteringByAProgram()
        throws Exception {
      BddLogger.given(
          "a student bound to Groupe A, two levels below the program the staff is affiliated to,"
              + " but not to the program itself");
      affiliateStaffToSeededProgram();

      BddLogger.when(
          "calling GET /back-office/external-users/affiliations filtered by the program");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("keyword", "tessier")
                  .param("groupIds", groupIdOf(SEEDED_PROGRAM_ID_SI_SCO).toString())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.data[*].id", hasItem(externalUserIdOf(SEEDED_STUDENT_EPPN).toString())))
          .andExpect(
              jsonPath("$.data[*].groups")
                  .value(everyItem(hasItem(hasEntry("name", "Licence Informatique")))));

      BddLogger.then(
          "it should return that student, the composition of a group including its descendants");
    }

    @Test
    void thenItShouldReturnTheUserOnce_whenItBelongsToSeveralLevelsOfTheFilteredGroup()
        throws Exception {
      BddLogger.given("a student bound both to the program and to Groupe A below it");
      affiliateStaffToSeededProgram();
      affiliateStudentToSeededProgram();
      String studentId = externalUserIdOf(SEEDED_STUDENT_EPPN).toString();

      BddLogger.when(
          "calling GET /back-office/external-users/affiliations filtered by the program");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("keyword", "tessier")
                  .param("groupIds", groupIdOf(SEEDED_PROGRAM_ID_SI_SCO).toString())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[?(@.id == '" + studentId + "')]", hasSize(1)));

      BddLogger.then("it should list the student once, however many of its affiliations match");
    }

    @Test
    void thenItShouldListAsManyUsersAsTheGroupCounts_whenFilteringByAProgram() throws Exception {
      BddLogger.given(
          "a student bound both to the program and to Groupe A below it, and the users counted for"
              + " the program");
      affiliateStaffToSeededProgram();
      affiliateStudentToSeededProgram();
      UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);
      int programUsersCount =
          externalUserAffiliationService
              .countExternalUsersByGroupIdsWithSubGroups(List.of(programId))
              .get(programId);

      BddLogger.when(
          "calling GET /back-office/external-users/affiliations filtered by the program");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("groupIds", programId.toString())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.page.totalElements", is(programUsersCount)));

      BddLogger.then(
          "the composition should hold exactly the users counted for the program, each once");
    }

    @Test
    void thenItShouldCumulateTheFilters_whenSearchingAndFilteringAtOnce() throws Exception {
      BddLogger.given("a Tessier user holding staff affiliations and another holding student ones");

      BddLogger.when(
          "calling GET /back-office/external-users/affiliations with keyword and filter");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("keyword", "tessier")
                  .param("categories", EUserCategory.STAFF.name())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", hasSize(1)))
          .andExpect(jsonPath("$.data[*].categories[*]", everyItem(is("STAFF"))))
          .andExpect(jsonPath("$.page.totalElements", is(1)));

      BddLogger.then("it should return only the user matching both the search and the filter");
    }

    @Test
    void thenItShouldReturnTheRequestedPage_whenPaginating() throws Exception {
      BddLogger.given("two seeded users matching the Tessier keyword");

      BddLogger.when(
          "calling GET /back-office/external-users/affiliations with a page size of one");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("keyword", "tessier")
                  .param("page", "1")
                  .param("pageSize", "1")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", hasSize(1)))
          .andExpect(jsonPath("$.data[*].lastName", everyItem(is("Tessier"))))
          .andExpect(jsonPath("$.page.page", is(1)))
          .andExpect(jsonPath("$.page.pageSize", is(1)))
          .andExpect(jsonPath("$.page.totalElements", is(SEEDED_TESSIER_USERS_COUNT)))
          .andExpect(jsonPath("$.page.totalPages", is(2)));

      BddLogger.then("it should return the second page and its page information");
    }

    @Test
    void thenItShouldReportTheCofolioConnectionAndProfilePicture() throws Exception {
      BddLogger.given("a Tessier user who logged in to Cofolio and uploaded a profile picture");
      affiliateStaffToSeededProgram();
      UUID profilePictureId = UUID.randomUUID();
      when(cofolioProfileClient.findByEppns(any()))
          .thenReturn(
              Map.of(
                  SEEDED_STUDENT_EPPN,
                  new CofolioProfile(SEEDED_STUDENT_EPPN, true, profilePictureId)));

      BddLogger.when("calling GET /back-office/external-users/affiliations");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("keyword", "tessier")
                  .param("groupIds", groupIdOf(SEEDED_STUDENT_GROUP_ID_SI_SCO).toString())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", hasSize(1)))
          .andExpect(jsonPath("$.data[0].hasConnected", is(true)))
          .andExpect(jsonPath("$.data[0].profilePictureId", is(profilePictureId.toString())));

      BddLogger.then("it should report the connection and the id of the picture");
    }

    @Test
    void thenItShouldReportNoPicture_whenTheUserConnectedWithoutUploadingOne() throws Exception {
      BddLogger.given("a Tessier user who logged in to Cofolio without uploading a picture");
      affiliateStaffToSeededProgram();
      when(cofolioProfileClient.findByEppns(any()))
          .thenReturn(
              Map.of(SEEDED_STUDENT_EPPN, new CofolioProfile(SEEDED_STUDENT_EPPN, true, null)));

      BddLogger.when("calling GET /back-office/external-users/affiliations");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("keyword", "tessier")
                  .param("groupIds", groupIdOf(SEEDED_STUDENT_GROUP_ID_SI_SCO).toString())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[0].hasConnected", is(true)))
          .andExpect(jsonPath("$.data[0].profilePictureId", is(nullValue())));

      BddLogger.then("it should report the connection with no picture");
    }

    @Test
    void thenItShouldReportNoConnection_whenTheApiKnowsNothingOfTheUser() throws Exception {
      BddLogger.given("users the portfolio api reports nothing for");

      BddLogger.when("calling GET /back-office/external-users/affiliations");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("keyword", "tessier")
                  .param("pageSize", "20")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", hasSize(SEEDED_TESSIER_USERS_COUNT)))
          .andExpect(jsonPath("$.data[*].hasConnected", everyItem(is(false))))
          .andExpect(jsonPath("$.data[*].profilePictureId", everyItem(is(nullValue()))));

      BddLogger.then("it should report them as never connected rather than failing");
    }

    @Test
    void thenItShouldReturnForbidden_whenFilteringOnAGroupTheStaffIsNotAffiliatedTo()
        throws Exception {
      BddLogger.given("a staff affiliated to no group at all");

      BddLogger.when("calling GET /back-office/external-users/affiliations filtered by group");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("groupIds", groupIdOf(SEEDED_STUDENT_GROUP_ID_SI_SCO).toString())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403 rather than the members of that group");
    }

    @Test
    void thenItShouldReturnForbidden_whenFilteringOnAnotherInstitution() throws Exception {
      BddLogger.given("a staff of the Rennes institution");

      BddLogger.when(
          "calling GET /back-office/external-users/affiliations filtered by another institution");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("institutionIds", institutionIdOf(SEEDED_BORDEAUX_UAI).toString())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, the staff not being affiliated to that institution");
    }

    @Test
    void thenItShouldReturnForbidden_whenOnlyOneOfTheFilteredGroupsIsReachable() throws Exception {
      BddLogger.given("a staff affiliated to the Rennes program only");
      affiliateStaffToSeededProgram();

      BddLogger.when("calling GET /back-office/external-users/affiliations with two groups");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("groupIds", groupIdOf(SEEDED_STUDENT_GROUP_ID_SI_SCO).toString())
                  .param("groupIds", groupIdOf(SEEDED_OTHER_STUDENT_GROUP_ID_SI_SCO).toString())
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, every filtered group having to be reachable");
    }

    @Test
    void thenItShouldReturnForbidden_whenStaffMissesTheReadPermission() throws Exception {
      BddLogger.given("a logged-in staff without the external user read permission");

      BddLogger.when("calling GET /back-office/external-users/affiliations");
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
  class WhenGettingExternalUser {

    @Test
    void thenItShouldReturnTheIdentityTheInstitutionAndTheGroupsWithTheirParents()
        throws Exception {
      BddLogger.given("a student of Rennes and Bordeaux, seen by a staff of Rennes");
      UUID externalUserId = externalUserIdOf(SEEDED_STUDENT_EPPN);
      UUID profilePictureId = UUID.randomUUID();
      when(cofolioProfileClient.findByEppns(any()))
          .thenReturn(
              Map.of(
                  SEEDED_STUDENT_EPPN,
                  new CofolioProfile(SEEDED_STUDENT_EPPN, true, profilePictureId)));

      BddLogger.when("calling GET /back-office/external-users/{externalUserId}/detail");
      mockMvc
          .perform(
              asStaffUser(get(EXTERNAL_USERS_PATH + externalUserId + "/detail"), STAFF_EPPN)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(externalUserId.toString())))
          .andExpect(jsonPath("$.firstName", is("Lucas")))
          .andExpect(jsonPath("$.lastName", is("Tessier")))
          .andExpect(jsonPath("$.profilePictureId", is(profilePictureId.toString())))
          .andExpect(
              jsonPath("$.institution.id", is(institutionIdOf(SEEDED_RENNES_UAI).toString())))
          .andExpect(
              jsonPath(
                  "$.groups[*].id",
                  containsInAnyOrder(
                      groupIdOf(SEEDED_STUDENT_GROUP_ID_SI_SCO).toString(),
                      groupIdOf(SEEDED_OPTION_ID_SI_SCO).toString(),
                      groupIdOf(SEEDED_PROGRAM_ID_SI_SCO).toString())));

      BddLogger.then("it should hide the Bordeaux group and list the Rennes ones with parents");
    }

    @Test
    void thenItShouldReturnForbidden_whenTheUserIsOutsideTheStaffScope() throws Exception {
      BddLogger.given("a student affiliated to Bordeaux only, seen by a staff of Rennes");
      UUID externalUserId = externalUserIdOf(SEEDED_BORDEAUX_STUDENT_EPPN);

      BddLogger.when("calling GET /back-office/external-users/{externalUserId}/detail");
      mockMvc
          .perform(
              asStaffUser(get(EXTERNAL_USERS_PATH + externalUserId + "/detail"), STAFF_EPPN)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403");
    }

    @Test
    void thenItShouldReturnNotFound_whenExternalUserDoesNotExist() throws Exception {
      BddLogger.given("a random external user id that does not exist");

      BddLogger.when("calling GET /back-office/external-users/{externalUserId}/detail");
      mockMvc
          .perform(
              asStaffUser(get(EXTERNAL_USERS_PATH + UUID.randomUUID() + "/detail"), STAFF_EPPN)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());

      BddLogger.then("it should return 404");
    }

    @Test
    void thenItShouldReturnForbidden_whenStaffMissesTheReadPermission() throws Exception {
      BddLogger.given("a logged-in staff without the external user read permission");
      UUID externalUserId = externalUserIdOf(SEEDED_STUDENT_EPPN);

      BddLogger.when("calling GET /back-office/external-users/{externalUserId}/detail");
      mockMvc
          .perform(
              get(EXTERNAL_USERS_PATH + externalUserId + "/detail")
                  .header("X-Forwarded-For", "203.0.113.10")
                  .with(authentication(new HmacAuthenticationToken(STAFF_EPPN)))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403");
    }
  }

  @Nested
  class WhenGettingGlobalAuthorizationOfExternalUser {

    @Test
    void thenItShouldReturnTheRolesAndTheUniversesOfTheUser() throws Exception {
      BddLogger.given("a student also holding the staff role, seen by a staff of Rennes");
      UUID externalUserId = externalUserIdOf(SEEDED_STUDENT_EPPN);
      when(securityClient.findRolesByLogin(SEEDED_STUDENT_EPPN))
          .thenReturn(Set.of(ERole.ROLE_STUDENT, ERole.ROLE_STAFF));

      BddLogger.when(
          "calling GET /back-office/external-users/{externalUserId}/global-authorization");
      mockMvc
          .perform(
              asStaffUser(
                      get(EXTERNAL_USERS_PATH + externalUserId + "/global-authorization"),
                      STAFF_EPPN)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.roles", containsInAnyOrder("ROLE_STUDENT", "ROLE_STAFF")))
          .andExpect(jsonPath("$.universes", containsInAnyOrder("STUDENT", "STAFF")));

      BddLogger.then("it should return the roles and both universes");
    }

    @Test
    void thenItShouldReturnForbidden_whenTheUserIsOutsideTheStaffScope() throws Exception {
      BddLogger.given("a student affiliated to Bordeaux only, seen by a staff of Rennes");
      UUID externalUserId = externalUserIdOf(SEEDED_BORDEAUX_STUDENT_EPPN);

      BddLogger.when(
          "calling GET /back-office/external-users/{externalUserId}/global-authorization");
      mockMvc
          .perform(
              asStaffUser(
                      get(EXTERNAL_USERS_PATH + externalUserId + "/global-authorization"),
                      STAFF_EPPN)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403 without asking the security service");
      verifyNoInteractions(securityClient);
    }

    @Test
    void thenItShouldReturnNotFound_whenExternalUserDoesNotExist() throws Exception {
      BddLogger.given("a random external user id that does not exist");

      BddLogger.when(
          "calling GET /back-office/external-users/{externalUserId}/global-authorization");
      mockMvc
          .perform(
              asStaffUser(
                      get(EXTERNAL_USERS_PATH + UUID.randomUUID() + "/global-authorization"),
                      STAFF_EPPN)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());

      BddLogger.then("it should return 404");
    }

    @Test
    void thenItShouldReturnForbidden_whenStaffMissesTheReadPermission() throws Exception {
      BddLogger.given("a logged-in staff without the external user read permission");
      UUID externalUserId = externalUserIdOf(SEEDED_STUDENT_EPPN);

      BddLogger.when(
          "calling GET /back-office/external-users/{externalUserId}/global-authorization");
      mockMvc
          .perform(
              get(EXTERNAL_USERS_PATH + externalUserId + "/global-authorization")
                  .header("X-Forwarded-For", "203.0.113.10")
                  .with(authentication(new HmacAuthenticationToken(STAFF_EPPN)))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403");
    }
  }

  @Nested
  class WhenGettingAffiliationsOfExternalUser {

    @Test
    void thenItShouldReturnAllItsAffiliations() throws Exception {
      BddLogger.given("a seeded student affiliated to two institutions and two groups");
      UUID externalUserId = externalUserIdOf(SEEDED_STUDENT_EPPN);

      BddLogger.when("calling GET /back-office/external-users/{externalUserId}/affiliations");
      mockMvc
          .perform(
              asStaffUser(
                      get("/back-office/external-users/" + externalUserId + "/affiliations"),
                      STAFF_EPPN)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(4)))
          .andExpect(jsonPath("$[*].externalUserId", everyItem(is(externalUserId.toString()))));

      BddLogger.then("it should return the four affiliations of that user");
    }

    @Test
    void thenItShouldReturnNotFound_whenExternalUserDoesNotExist() throws Exception {
      BddLogger.given("a random external user id that does not exist");
      UUID unknownId = UUID.randomUUID();

      BddLogger.when("calling GET /back-office/external-users/{externalUserId}/affiliations");
      mockMvc
          .perform(
              asStaffUser(
                      get("/back-office/external-users/" + unknownId + "/affiliations"), STAFF_EPPN)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());

      BddLogger.then("it should return 404");
    }
  }
}
