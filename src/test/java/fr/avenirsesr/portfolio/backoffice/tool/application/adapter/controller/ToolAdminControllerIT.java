package fr.avenirsesr.portfolio.backoffice.tool.application.adapter.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto.CreateLocalToolRequest;
import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto.CreateNationalToolRequest;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.EToolType;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.backoffice.tool.domain.port.output.repository.ToolRepository;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.EPermission;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.HmacAuthenticationToken;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

class ToolAdminControllerIT extends ContainerConfigurationTest {

  private static final String BASE_PATH = "/back-office/tools";
  private static final String NATIONAL_PATH = BASE_PATH + "/national";
  private static final String LOCAL_PATH = BASE_PATH + "/local";
  private static final String STAFF_EPPN = "ethan.perrin@university.com";
  private static final String RENNES_UAI = "0350001A";
  private static final String RENNES_IUT_UAI = "0350002B";
  private static final String BORDEAUX_UAI = "0330001C";
  private static final String NAME = "Moodle";
  private static final String DESCRIPTION = "Plateforme de cours en ligne";
  private static final String URL = "https://moodle.example.fr";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private ToolRepository toolRepository;
  @Autowired private InstitutionRepository institutionRepository;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  private UUID institutionIdOf(String uai) {
    return institutionRepository.findByUai(uai).map(Institution::getId).orElseThrow();
  }

  private ResultActions postAsStaff(String path, Object payload, EPermission... permissions)
      throws Exception {
    return mockMvc.perform(
        post(path)
            .header("X-Forwarded-For", "203.0.113.10")
            .with(
                authentication(
                    new HmacAuthenticationToken(
                        STAFF_EPPN,
                        Arrays.stream(permissions)
                            .map(permission -> new SimpleGrantedAuthority(permission.authority()))
                            .toList())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(payload)));
  }

  private Tool storedToolOf(ResultActions result) throws Exception {
    String body = result.andReturn().getResponse().getContentAsString();
    UUID id = UUID.fromString(objectMapper.readTree(body).get("id").asText());
    return toolRepository.findById(id).orElseThrow();
  }

  @Nested
  class WhenCreatingANationalTool {

    @Test
    void thenItShouldCreateANationalTool() throws Exception {
      BddLogger.given("a staff granted the national tool management permission");

      BddLogger.when("calling POST " + NATIONAL_PATH);
      ResultActions result =
          postAsStaff(
                  NATIONAL_PATH,
                  new CreateNationalToolRequest(NAME, DESCRIPTION, URL),
                  EPermission.PERM_TOOL_NATIONAL_MANAGEMENT)
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.id").exists())
              .andExpect(jsonPath("$.name", is(NAME)))
              .andExpect(jsonPath("$.description", is(DESCRIPTION)))
              .andExpect(jsonPath("$.url", is(URL)))
              .andExpect(jsonPath("$.type", is(EToolType.NATIONAL.name())))
              .andExpect(jsonPath("$.institutionId", nullValue()));

      BddLogger.then("it should store a tool attached to no institution");
      Tool stored = storedToolOf(result);
      assertThat(stored.getName()).isEqualTo(NAME);
      assertThat(stored.getInstitution()).isEmpty();
    }

    @Test
    void thenItShouldReturnForbidden_whenTheStaffOnlyManagesLocalTools() throws Exception {
      BddLogger.given("a staff granted the local tool management permission only");
      int toolCount = toolRepository.findAll().size();

      BddLogger.when("calling POST " + NATIONAL_PATH);
      postAsStaff(
              NATIONAL_PATH,
              new CreateNationalToolRequest(NAME, DESCRIPTION, URL),
              EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, without storing any tool");
      assertThat(toolRepository.findAll()).hasSize(toolCount);
    }

    @Test
    void thenItShouldReturnBadRequest_whenTheDescriptionExceeds200Characters() throws Exception {
      BddLogger.given("a description of 201 characters");
      int toolCount = toolRepository.findAll().size();

      BddLogger.when("calling POST " + NATIONAL_PATH);
      postAsStaff(
              NATIONAL_PATH,
              new CreateNationalToolRequest(NAME, "a".repeat(201), URL),
              EPermission.PERM_TOOL_NATIONAL_MANAGEMENT)
          .andExpect(status().isBadRequest());

      BddLogger.then("it should return 400, without storing any tool");
      assertThat(toolRepository.findAll()).hasSize(toolCount);
    }
  }

  @Nested
  class WhenCreatingALocalTool {

    @Test
    void thenItShouldCreateARegionalTool_forTheStaffPrimaryInstitution() throws Exception {
      BddLogger.given("a staff of Rennes granted the local tool management permission");
      UUID rennesId = institutionIdOf(RENNES_UAI);

      BddLogger.when("calling POST " + LOCAL_PATH + " for Rennes");
      ResultActions result =
          postAsStaff(
                  LOCAL_PATH,
                  new CreateLocalToolRequest(NAME, DESCRIPTION, URL, rennesId),
                  EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.type", is(EToolType.REGIONAL.name())))
              .andExpect(jsonPath("$.institutionId", is(rennesId.toString())));

      BddLogger.then("it should store a tool attached to Rennes");
      assertThat(storedToolOf(result).getInstitution())
          .get()
          .extracting(Institution::getId)
          .isEqualTo(rennesId);
    }

    @Test
    void thenItShouldCreateALocalTool_forASecondaryInstitutionOfTheStaffScope() throws Exception {
      BddLogger.given(
          "a staff of Rennes granted the local tool management permission, and the IUT of"
              + " Rennes");
      UUID rennesIutId = institutionIdOf(RENNES_IUT_UAI);

      BddLogger.when("calling POST " + LOCAL_PATH + " for the IUT of Rennes");
      ResultActions result =
          postAsStaff(
                  LOCAL_PATH,
                  new CreateLocalToolRequest(NAME, DESCRIPTION, URL, rennesIutId),
                  EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.type", is(EToolType.LOCAL.name())))
              .andExpect(jsonPath("$.institutionId", is(rennesIutId.toString())));

      BddLogger.then("it should store a tool attached to the IUT of Rennes");
      assertThat(storedToolOf(result).getInstitution())
          .get()
          .extracting(Institution::getId)
          .isEqualTo(rennesIutId);
    }

    @Test
    void thenItShouldReturnForbidden_whenTheInstitutionIsOutsideTheStaffScope() throws Exception {
      BddLogger.given("a staff of Rennes and the institution of Bordeaux");
      int toolCount = toolRepository.findAll().size();

      BddLogger.when("calling POST " + LOCAL_PATH + " for Bordeaux");
      postAsStaff(
              LOCAL_PATH,
              new CreateLocalToolRequest(NAME, DESCRIPTION, URL, institutionIdOf(BORDEAUX_UAI)),
              EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, without storing any tool");
      assertThat(toolRepository.findAll()).hasSize(toolCount);
    }

    @Test
    void thenItShouldReturnForbidden_whenTheStaffOnlyManagesNationalTools() throws Exception {
      BddLogger.given("a staff of Rennes granted the national tool management permission only");
      int toolCount = toolRepository.findAll().size();

      BddLogger.when("calling POST " + LOCAL_PATH + " for Rennes");
      postAsStaff(
              LOCAL_PATH,
              new CreateLocalToolRequest(NAME, DESCRIPTION, URL, institutionIdOf(RENNES_UAI)),
              EPermission.PERM_TOOL_NATIONAL_MANAGEMENT)
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, without storing any tool");
      assertThat(toolRepository.findAll()).hasSize(toolCount);
    }

    @Test
    void thenItShouldReturnNotFound_whenTheInstitutionDoesNotExist() throws Exception {
      BddLogger.when("calling POST " + LOCAL_PATH + " for an unknown institution");
      postAsStaff(
              LOCAL_PATH,
              new CreateLocalToolRequest(NAME, DESCRIPTION, URL, UUID.randomUUID()),
              EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isNotFound());

      BddLogger.then("it should return 404");
    }

    @Test
    void thenItShouldReturnBadRequest_whenTheInstitutionIsMissing() throws Exception {
      BddLogger.when("calling POST " + LOCAL_PATH + " without institution");
      postAsStaff(
              LOCAL_PATH,
              new CreateLocalToolRequest(NAME, DESCRIPTION, URL, null),
              EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isBadRequest());

      BddLogger.then("it should return 400");
    }
  }
}
