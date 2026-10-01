package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.HmacAuthenticationToken;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

class AccessControllerIT extends ContainerConfigurationTest {

  private static final String BASE_PATH = "/back-office/access";
  private static final String STAFF_EPPN = "ethan.perrin@university.com";
  private static final String STUDENT_ONLY_EPPN = "lucas.tessier@university.com";

  @Autowired private MockMvc mockMvc;
  @Autowired private ExternalUserRepository externalUserRepository;
  @Autowired private InstitutionRepository institutionRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private ExternalUserAffiliationService externalUserAffiliationService;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  private static MockHttpServletRequestBuilder asStaffUser(
      MockHttpServletRequestBuilder builder, String eppn) {
    return builder
        .header("X-Forwarded-For", "203.0.113.10")
        .with(authentication(new HmacAuthenticationToken(eppn)));
  }

  @Nested
  class WhenGettingStaffScope {

    @Nested
    class AndStaffIsAffiliatedToAnInstitutionWithAChild {

      private UUID institutionId;
      private UUID childInstitutionId;
      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given(
            "a STAFF external user affiliated to a primary institution having a secondary"
                + " child");
        institutionId = institutionRepository.findByUai("0350001A").orElseThrow().getId();
        childInstitutionId = institutionRepository.findByUai("0350002B").orElseThrow().getId();

        BddLogger.when("calling GET /back-office/access/staff/scope as that user");
        response =
            mockMvc.perform(
                asStaffUser(get(BASE_PATH + "/staff/scope"), STAFF_EPPN)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnTheInstitutionWithItsChildAndNoGroup() throws Exception {
        BddLogger.then(
            "it should return the affiliated institution nested with its child, and no group");

        response
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.institutions", hasSize(1)))
            .andExpect(jsonPath("$.institutions[0].id", is(institutionId.toString())))
            .andExpect(jsonPath("$.institutions[0].title", is("Université de Rennes")))
            .andExpect(jsonPath("$.institutions[0].children", hasSize(1)))
            .andExpect(
                jsonPath("$.institutions[0].children[0].id", is(childInstitutionId.toString())))
            .andExpect(
                jsonPath("$.institutions[0].children[0].title", is("Université de Rennes - IUT")))
            .andExpect(jsonPath("$.institutions[0].children[0].children", hasSize(0)))
            .andExpect(jsonPath("$.groups", hasSize(0)));
      }
    }

    @Nested
    class AndStaffIsAffiliatedToAProgramWithNestedDescendants {

      private UUID programId;
      private UUID programOptionId;
      private UUID studentGroupId;
      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given(
            "a STAFF external user affiliated to a program whose hierarchy has a program option"
                + " and a student group");
        UUID externalUserId = externalUserRepository.findByEppn(STAFF_EPPN).orElseThrow().getId();
        UUID institutionId = institutionRepository.findByUai("0350001A").orElseThrow().getId();
        programId = groupRepository.findByIdSiSco("10000001").orElseThrow().getId();
        programOptionId = groupRepository.findByIdSiSco("10000002").orElseThrow().getId();
        studentGroupId = groupRepository.findByIdSiSco("10000003").orElseThrow().getId();
        externalUserAffiliationService.addAffiliation(
            externalUserId, institutionId, programId, EUserCategory.STAFF);

        BddLogger.when("calling GET /back-office/access/staff/scope as that user");
        response =
            mockMvc.perform(
                asStaffUser(get(BASE_PATH + "/staff/scope"), STAFF_EPPN)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnTheProgramWithItsDescendantsNested() throws Exception {
        BddLogger.then(
            "it should return the program with the option and the student group nested two"
                + " levels deep");

        response
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.groups", hasSize(1)))
            .andExpect(jsonPath("$.groups[0].id", is(programId.toString())))
            .andExpect(jsonPath("$.groups[0].title", is("Licence Informatique")))
            .andExpect(jsonPath("$.groups[0].children", hasSize(1)))
            .andExpect(jsonPath("$.groups[0].children[0].id", is(programOptionId.toString())))
            .andExpect(
                jsonPath("$.groups[0].children[0].title", is("Licence Informatique - Parcours IA")))
            .andExpect(jsonPath("$.groups[0].children[0].children", hasSize(1)))
            .andExpect(
                jsonPath("$.groups[0].children[0].children[0].id", is(studentGroupId.toString())))
            .andExpect(jsonPath("$.groups[0].children[0].children[0].title", is("Groupe A")));
      }
    }

    @Nested
    class AndTheExternalUserHasNoStaffAffiliation {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("an external user affiliated only as a STUDENT");

        BddLogger.when("calling GET /back-office/access/staff/scope as that user");
        response =
            mockMvc.perform(
                asStaffUser(get(BASE_PATH + "/staff/scope"), STUDENT_ONLY_EPPN)
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnAnEmptyScope() throws Exception {
        BddLogger.then("it should return an empty institution and group scope");

        response
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.institutions", hasSize(0)))
            .andExpect(jsonPath("$.groups", hasSize(0)));
      }
    }

    @Nested
    class AndExternalUserDoesNotExist {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a principal eppn that does not match any external user");

        BddLogger.when("calling GET /back-office/access/staff/scope as that user");
        response =
            mockMvc.perform(
                asStaffUser(get(BASE_PATH + "/staff/scope"), "unknown.eppn@university.com")
                    .accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnNotFound() throws Exception {
        BddLogger.then("it should return 404");

        response.andExpect(status().isNotFound());
      }
    }

    @Nested
    class AndNoAuthenticationIsProvided {

      private ResultActions response;

      @BeforeEach
      void setupWhen() throws Exception {
        BddLogger.given("a request with neither an api key nor a signed user context");

        BddLogger.when("calling GET /back-office/access/staff/scope");
        response =
            mockMvc.perform(get(BASE_PATH + "/staff/scope").accept(MediaType.APPLICATION_JSON));
      }

      @Test
      void thenItShouldReturnUnauthorized() throws Exception {
        BddLogger.then("it should return 401");

        response.andExpect(status().isUnauthorized());
      }
    }
  }
}
