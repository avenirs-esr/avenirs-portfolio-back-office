package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.controller;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.CofolioProfile;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.client.CofolioProfileClient;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.EPermission;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.HmacAuthenticationToken;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.List;
import java.util.Map;
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

class ExternalUserAffiliationQueryControllerIT extends ContainerConfigurationTest {

  private static final String BASE_PATH = "/back-office/external-users/affiliations";
  private static final String STAFF_EPPN = "ethan.perrin@university.com";
  private static final String SEEDED_STUDENT_EPPN = "lucas.tessier@university.com";
  private static final String SEEDED_RENNES_UAI = "0350001A";
  private static final String SEEDED_BORDEAUX_UAI = "0330001C";
  private static final String SEEDED_PROGRAM_ID_SI_SCO = "10000001";
  private static final String SEEDED_STUDENT_GROUP_ID_SI_SCO = "10000003";
  private static final String SEEDED_OTHER_STUDENT_GROUP_ID_SI_SCO = "10000005";

  /** Totals of the seeded fixtures, all categories and institutions taken together. */
  private static final int SEEDED_AFFILIATIONS_COUNT = 370;

  private static final int SEEDED_STAFF_AFFILIATIONS_COUNT = 60;
  private static final int SEEDED_RENNES_AFFILIATIONS_COUNT = 188;
  private static final int SEEDED_GROUPE_A_AFFILIATIONS_COUNT = 80;
  private static final int SEEDED_TESSIER_AFFILIATIONS_COUNT = 6;

  @Autowired private MockMvc mockMvc;
  @Autowired private ExternalUserRepository externalUserRepository;
  @Autowired private InstitutionRepository institutionRepository;
  @Autowired private GroupRepository groupRepository;

  @Autowired private ExternalUserAffiliationService externalUserAffiliationService;

  @MockitoBean private CofolioProfileClient cofolioProfileClient;

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
      BddLogger.given("the seeded affiliations, the first last name alphabetically being AMU");

      BddLogger.when("calling GET /back-office/external-users/affiliations");
      mockMvc
          .perform(asStaffUser(get(BASE_PATH), STAFF_EPPN).accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", hasSize(8)))
          .andExpect(jsonPath("$.data[0].lastName", is("AMU")))
          .andExpect(jsonPath("$.data[0].firstName", is("Ens1")))
          .andExpect(jsonPath("$.data[1].lastName", is("AMU")))
          .andExpect(jsonPath("$.data[1].firstName", is("Ens2")))
          .andExpect(jsonPath("$.data[2].lastName", is("AMU")))
          .andExpect(jsonPath("$.data[2].firstName", is("Ens3")))
          .andExpect(jsonPath("$.data[3].lastName", is("Arnaud")))
          .andExpect(jsonPath("$.page.page", is(0)))
          .andExpect(jsonPath("$.page.pageSize", is(8)))
          .andExpect(jsonPath("$.page.totalElements", is(SEEDED_AFFILIATIONS_COUNT)));

      BddLogger.then("it should return the default page, ordered by last name then first name");
    }

    @Test
    void thenItShouldReturnTheIdentityOfTheAffiliatedUserAndTheLabelsOfItsScope() throws Exception {
      BddLogger.given("a student affiliated to a student group");
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
          .andExpect(jsonPath("$.data[0].firstName", is("Lucas")))
          .andExpect(jsonPath("$.data[0].lastName", is("Tessier")))
          .andExpect(jsonPath("$.data[0].category", is("STUDENT")))
          .andExpect(jsonPath("$.data[0].institutionName", is("Université de Rennes")))
          .andExpect(jsonPath("$.data[0].groupName", is("Groupe A")))
          .andExpect(jsonPath("$.data[0].groupType", is("STUDENT_GROUP")));

      BddLogger.then("it should return the user identity along with the institution and group");
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
          .andExpect(jsonPath("$.data", hasSize(SEEDED_TESSIER_AFFILIATIONS_COUNT)))
          .andExpect(jsonPath("$.data[*].lastName", everyItem(is("Tessier"))))
          .andExpect(jsonPath("$.page.totalElements", is(SEEDED_TESSIER_AFFILIATIONS_COUNT)));

      BddLogger.then("it should return their affiliations, the search ignoring the case");
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
          .andExpect(jsonPath("$.data", hasSize(SEEDED_TESSIER_AFFILIATIONS_COUNT)))
          .andExpect(jsonPath("$.data[*].firstName", everyItem(is("Lucas"))));

      BddLogger.then("it should match the first name as well as the last name");
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
          .andExpect(jsonPath("$.data[*].category", everyItem(is("STAFF"))))
          .andExpect(jsonPath("$.page.totalElements", is(SEEDED_STAFF_AFFILIATIONS_COUNT)));

      BddLogger.then("it should return only the affiliations held with that category");
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
          .andExpect(jsonPath("$.data[*].institutionName", everyItem(is("Université de Rennes"))))
          .andExpect(jsonPath("$.page.totalElements", is(SEEDED_RENNES_AFFILIATIONS_COUNT)));

      BddLogger.then("it should return only the affiliations held with that institution");
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
          .andExpect(jsonPath("$.data[*].groupName", everyItem(is("Groupe A"))))
          .andExpect(jsonPath("$.page.totalElements", is(SEEDED_GROUPE_A_AFFILIATIONS_COUNT)));

      BddLogger.then(
          "it should return only the affiliations bound to that group, reachable through the"
              + " program the staff is affiliated to");
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
          .andExpect(jsonPath("$.data", hasSize(2)))
          .andExpect(jsonPath("$.data[*].category", everyItem(is("STAFF"))))
          .andExpect(jsonPath("$.page.totalElements", is(2)));

      BddLogger.then("it should return only the affiliations matching both the search and filter");
    }

    @Test
    void thenItShouldReturnTheRequestedPage_whenPaginating() throws Exception {
      BddLogger.given("six seeded affiliations matching the Tessier keyword");

      BddLogger.when(
          "calling GET /back-office/external-users/affiliations with a page size of four");
      mockMvc
          .perform(
              asStaffUser(get(BASE_PATH), STAFF_EPPN)
                  .param("keyword", "tessier")
                  .param("page", "1")
                  .param("pageSize", "4")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data", hasSize(2)))
          .andExpect(jsonPath("$.page.page", is(1)))
          .andExpect(jsonPath("$.page.pageSize", is(4)))
          .andExpect(jsonPath("$.page.totalElements", is(SEEDED_TESSIER_AFFILIATIONS_COUNT)))
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
          .andExpect(jsonPath("$.data", hasSize(SEEDED_TESSIER_AFFILIATIONS_COUNT)))
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
