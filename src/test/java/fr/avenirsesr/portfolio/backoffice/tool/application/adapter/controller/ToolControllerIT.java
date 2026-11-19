package fr.avenirsesr.portfolio.backoffice.tool.application.adapter.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.EToolType;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.backoffice.tool.domain.port.output.repository.ToolRepository;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.EPermission;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.HmacAuthenticationToken;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

class ToolControllerIT extends ContainerConfigurationTest {

  private static final String AVAILABLE_PATH = "/back-office/tools/available";
  private static final String RENNES_LEARNER_EPPN = "arthur.lambert@university.com";
  private static final String BORDEAUX_LEARNER_EPPN = "sophie.charpentier@university.com";
  private static final String RENNES_UAI = "0350001A";
  private static final String RENNES_IUT_UAI = "0350002B";
  private static final String BORDEAUX_UAI = "0330001C";
  private static final String BORDEAUX_IUT_UAI = "0330002D";
  private static final String DESCRIPTION = "Plateforme de cours en ligne";
  private static final String URL = "https://moodle.example.fr";

  @Autowired private MockMvc mockMvc;
  @Autowired private ToolRepository toolRepository;
  @Autowired private InstitutionRepository institutionRepository;
  @Autowired private ExternalUserRepository externalUserRepository;
  @Autowired private ExternalUserAffiliationService externalUserAffiliationService;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  private Institution institution(String uai) {
    return institutionRepository.findByUai(uai).orElseThrow();
  }

  private Tool savedTool(String name, String institutionUai) {
    Institution institution = institutionUai == null ? null : institution(institutionUai);
    return toolRepository.save(Tool.create(name, DESCRIPTION, URL, null, institution));
  }

  private void affiliateAsStudent(String eppn, String institutionUai) {
    ExternalUser learner = externalUserRepository.findByEppn(eppn).orElseThrow();
    externalUserAffiliationService.addAffiliation(
        learner.getId(), institution(institutionUai).getId(), null, EUserCategory.STUDENT);
  }

  private ResultActions getAs(String eppn, String path) throws Exception {
    return getAs(eppn, path, EPermission.PERM_TOOL_READ);
  }

  private ResultActions getAs(String eppn, String path, EPermission... permissions)
      throws Exception {
    return mockMvc.perform(
        get(path)
            .header("X-Forwarded-For", "203.0.113.10")
            .with(
                authentication(
                    new HmacAuthenticationToken(
                        eppn,
                        Arrays.stream(permissions)
                            .map(permission -> new SimpleGrantedAuthority(permission.authority()))
                            .toList()))));
  }

  private static String[] idsOf(Tool... tools) {
    return Arrays.stream(tools).map(tool -> tool.getId().toString()).toArray(String[]::new);
  }

  @Nested
  class WhenListingTheAvailableTools {

    @Test
    void thenItShouldReturnTheNationalToolsAndThoseOfTheLearnerInstitution() throws Exception {
      BddLogger.given(
          "a learner of Rennes, national tools and tools of Rennes, of its IUT and of Bordeaux");
      Tool moodle = savedTool("Moodle", null);
      Tool agenda = savedTool("Agenda", null);
      Tool rennes = savedTool("ENT Rennes", RENNES_UAI);
      savedTool("Emploi du temps IUT", RENNES_IUT_UAI);
      savedTool("ENT Bordeaux", BORDEAUX_UAI);

      BddLogger.when("calling GET " + AVAILABLE_PATH + " as that learner");

      BddLogger.then(
          "it should return the national tools and the regional tool of Rennes, sorted by name");
      getAs(RENNES_LEARNER_EPPN, AVAILABLE_PATH)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[*].id", contains(idsOf(agenda, rennes, moodle))))
          .andExpect(jsonPath("$.data[0].type", is(EToolType.NATIONAL.name())))
          .andExpect(jsonPath("$.data[0].institution", nullValue()))
          .andExpect(jsonPath("$.data[1].type", is(EToolType.REGIONAL.name())))
          .andExpect(jsonPath("$.data[1].institution.name", is("Université de Rennes")))
          .andExpect(jsonPath("$.page.totalElements", is(3)));
    }

    @Test
    void thenItShouldIncludeTheToolsOfTheParentOfASecondaryInstitution() throws Exception {
      BddLogger.given(
          "a learner of Bordeaux also affiliated to the IUT of Rennes, and tools of every scope");
      affiliateAsStudent(BORDEAUX_LEARNER_EPPN, RENNES_IUT_UAI);
      Tool national = savedTool("National", null);
      Tool rennes = savedTool("Rennes", RENNES_UAI);
      Tool rennesIut = savedTool("IUT Rennes", RENNES_IUT_UAI);
      Tool bordeaux = savedTool("Bordeaux", BORDEAUX_UAI);
      savedTool("IUT Bordeaux", BORDEAUX_IUT_UAI);

      BddLogger.when("calling GET " + AVAILABLE_PATH + " as that learner");

      BddLogger.then(
          "it should return the national tool and those of Bordeaux, of the IUT of Rennes and of"
              + " Rennes, but not those of the IUT of Bordeaux");
      getAs(BORDEAUX_LEARNER_EPPN, AVAILABLE_PATH)
          .andExpect(status().isOk())
          .andExpect(
              jsonPath("$.data[*].id", contains(idsOf(bordeaux, rennesIut, national, rennes))))
          .andExpect(jsonPath("$.page.totalElements", is(4)));
    }

    @Test
    void thenItShouldOnlyReturnTheToolsOfTheRequestedType() throws Exception {
      BddLogger.given("a learner of Rennes, a national and a regional tool of Rennes");
      Tool national = savedTool("National", null);
      Tool rennes = savedTool("Rennes", RENNES_UAI);

      BddLogger.when("calling GET " + AVAILABLE_PATH + " for each type");

      BddLogger.then("each type should only return its tools");
      getAs(RENNES_LEARNER_EPPN, AVAILABLE_PATH + "?type=NATIONAL")
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[*].id", contains(idsOf(national))));
      getAs(RENNES_LEARNER_EPPN, AVAILABLE_PATH + "?type=REGIONAL")
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[*].id", contains(idsOf(rennes))));
      getAs(RENNES_LEARNER_EPPN, AVAILABLE_PATH + "?type=LOCAL")
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.page.totalElements", is(0)));
    }

    @Test
    void thenItShouldPageOverTheAvailableTools() throws Exception {
      BddLogger.given("a learner of Rennes and three national tools");
      savedTool("Agenda", null);
      savedTool("Moodle", null);
      Tool zotero = savedTool("Zotero", null);

      BddLogger.when("calling GET " + AVAILABLE_PATH + " for the second page of two tools");

      BddLogger.then("it should return the last tool with the page information");
      getAs(RENNES_LEARNER_EPPN, AVAILABLE_PATH + "?page=1&pageSize=2")
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[*].id", contains(idsOf(zotero))))
          .andExpect(jsonPath("$.page.page", is(1)))
          .andExpect(jsonPath("$.page.pageSize", is(2)))
          .andExpect(jsonPath("$.page.totalElements", is(3)))
          .andExpect(jsonPath("$.page.totalPages", is(2)));
    }

    @Test
    void thenItShouldReturnBadRequest_whenTheTypeIsUnknown() throws Exception {
      BddLogger.given("a learner of Rennes");

      BddLogger.when("calling GET " + AVAILABLE_PATH + "?type=UNKNOWN");

      BddLogger.then("it should return 400");
      getAs(RENNES_LEARNER_EPPN, AVAILABLE_PATH + "?type=UNKNOWN")
          .andExpect(status().isBadRequest());
    }

    @Test
    void thenItShouldReturnForbidden_whenTheUserCannotReadTheTools() throws Exception {
      BddLogger.given("a user of Rennes without the tool read permission");

      BddLogger.when("calling GET " + AVAILABLE_PATH);

      BddLogger.then("it should return 403");
      getAs(RENNES_LEARNER_EPPN, AVAILABLE_PATH, EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isForbidden());
    }

    @Test
    void thenItShouldReturnUnauthorized_whenNoUserIsAuthenticated() throws Exception {
      BddLogger.given("a request with neither an api key nor a signed user context");

      BddLogger.when("calling GET " + AVAILABLE_PATH);

      BddLogger.then("it should return 401");
      mockMvc.perform(get(AVAILABLE_PATH)).andExpect(status().isUnauthorized());
    }
  }

  @Nested
  class WhenGettingAnAvailableTool {

    @Test
    void thenItShouldReturnANationalTool() throws Exception {
      BddLogger.given("a learner of Rennes and a national tool");
      Tool national = savedTool("Moodle", null);

      BddLogger.when("calling GET " + AVAILABLE_PATH + "/{toolId}");

      BddLogger.then("it should return the tool");
      getAs(RENNES_LEARNER_EPPN, AVAILABLE_PATH + "/" + national.getId())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(national.getId().toString())))
          .andExpect(jsonPath("$.name", is("Moodle")))
          .andExpect(jsonPath("$.description", is(DESCRIPTION)))
          .andExpect(jsonPath("$.url", is(URL)))
          .andExpect(jsonPath("$.type", is(EToolType.NATIONAL.name())))
          .andExpect(jsonPath("$.institution", nullValue()))
          .andExpect(jsonPath("$.banner", nullValue()));
    }

    @Test
    void thenItShouldReturnAToolOfTheLearnerInstitution() throws Exception {
      BddLogger.given("a learner of Rennes and a regional tool of Rennes");
      Tool rennes = savedTool("ENT Rennes", RENNES_UAI);

      BddLogger.when("calling GET " + AVAILABLE_PATH + "/{toolId}");

      BddLogger.then("it should return the tool with its institution");
      getAs(RENNES_LEARNER_EPPN, AVAILABLE_PATH + "/" + rennes.getId())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.type", is(EToolType.REGIONAL.name())))
          .andExpect(jsonPath("$.institution.id", is(institution(RENNES_UAI).getId().toString())))
          .andExpect(jsonPath("$.institution.name", is("Université de Rennes")));
    }

    @Test
    void thenItShouldReturnForbidden_whenTheToolBelongsToAnotherInstitution() throws Exception {
      BddLogger.given("a learner of Rennes and a local tool of the IUT of Rennes");
      Tool rennesIut = savedTool("Emploi du temps IUT", RENNES_IUT_UAI);

      BddLogger.when("calling GET " + AVAILABLE_PATH + "/{toolId}");

      BddLogger.then("it should return 403");
      getAs(RENNES_LEARNER_EPPN, AVAILABLE_PATH + "/" + rennesIut.getId())
          .andExpect(status().isForbidden());
    }

    @Test
    void thenItShouldReturnForbidden_whenTheUserCannotReadTheTools() throws Exception {
      BddLogger.given("a national tool and a user without the tool read permission");
      Tool national = savedTool("Moodle", null);

      BddLogger.when("calling GET " + AVAILABLE_PATH + "/{toolId}");

      BddLogger.then("it should return 403");
      getAs(
              RENNES_LEARNER_EPPN,
              AVAILABLE_PATH + "/" + national.getId(),
              EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isForbidden());
    }

    @Test
    void thenItShouldReturnNotFound_whenTheToolDoesNotExist() throws Exception {
      BddLogger.given("an unknown tool id");

      BddLogger.when("calling GET " + AVAILABLE_PATH + "/{toolId}");

      BddLogger.then("it should return 404");
      getAs(RENNES_LEARNER_EPPN, AVAILABLE_PATH + "/" + UUID.randomUUID())
          .andExpect(status().isNotFound());
    }
  }
}
