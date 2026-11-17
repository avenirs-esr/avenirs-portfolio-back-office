package fr.avenirsesr.portfolio.backoffice.tool.application.adapter.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto.CreateLocalToolRequest;
import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto.CreateNationalToolRequest;
import fr.avenirsesr.portfolio.backoffice.tool.application.adapter.dto.UpdateToolRequest;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.EToolType;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.backoffice.tool.domain.port.output.repository.ToolRepository;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.EPermission;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.HmacAuthenticationToken;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import jakarta.persistence.EntityManager;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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
  @Autowired private EntityManager entityManager;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  private UUID institutionIdOf(String uai) {
    return institutionRepository.findByUai(uai).map(Institution::getId).orElseThrow();
  }

  private Tool existingTool(String institutionUai) {
    Institution institution =
        institutionUai == null
            ? null
            : institutionRepository.findByUai(institutionUai).orElseThrow();
    return toolRepository.save(Tool.create(NAME, DESCRIPTION, URL, null, institution));
  }

  private Tool reloaded(UUID toolId) {
    entityManager.flush();
    entityManager.clear();
    return toolRepository.findById(toolId).orElseThrow();
  }

  private ResultActions postAsStaff(String path, Object payload, EPermission... permissions)
      throws Exception {
    return asStaff(post(path), payload, permissions);
  }

  private ResultActions putAsStaff(String path, Object payload, EPermission... permissions)
      throws Exception {
    return asStaff(put(path), payload, permissions);
  }

  private ResultActions asStaff(
      MockHttpServletRequestBuilder request, Object payload, EPermission... permissions)
      throws Exception {
    return mockMvc.perform(
        request
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

  @Nested
  class WhenUpdatingANationalTool {

    private static final UpdateToolRequest UPDATE =
        new UpdateToolRequest("Moodle 2", "Nouvelle description", "https://new.example.fr");

    @Test
    void thenItShouldUpdateTheNationalTool() throws Exception {
      BddLogger.given("an existing national tool");
      Tool existing = existingTool(null);

      BddLogger.when("calling PUT " + NATIONAL_PATH + "/{toolId}");
      putAsStaff(
              NATIONAL_PATH + "/" + existing.getId(),
              UPDATE,
              EPermission.PERM_TOOL_NATIONAL_MANAGEMENT)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id", is(existing.getId().toString())))
          .andExpect(jsonPath("$.name", is(UPDATE.name())))
          .andExpect(jsonPath("$.description", is(UPDATE.description())))
          .andExpect(jsonPath("$.url", is(UPDATE.url())))
          .andExpect(jsonPath("$.type", is(EToolType.NATIONAL.name())))
          .andExpect(jsonPath("$.institutionId", nullValue()));

      BddLogger.then("it should store the new information");
      Tool stored = reloaded(existing.getId());
      assertThat(stored.getName()).isEqualTo(UPDATE.name());
      assertThat(stored.getDescription()).isEqualTo(UPDATE.description());
      assertThat(stored.getUrl()).isEqualTo(UPDATE.url());
      assertThat(stored.getInstitution()).isEmpty();
    }

    @Test
    void thenItShouldReturnForbidden_whenTheToolIsAttachedToAnInstitution() throws Exception {
      BddLogger.given("an existing regional tool of Rennes");
      Tool existing = existingTool(RENNES_UAI);

      BddLogger.when("calling PUT " + NATIONAL_PATH + "/{toolId}");
      putAsStaff(
              NATIONAL_PATH + "/" + existing.getId(),
              UPDATE,
              EPermission.PERM_TOOL_NATIONAL_MANAGEMENT)
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, leaving the tool untouched");
      assertThat(reloaded(existing.getId()).getName()).isEqualTo(NAME);
    }

    @Test
    void thenItShouldReturnForbidden_whenTheStaffOnlyManagesLocalTools() throws Exception {
      BddLogger.given("an existing national tool and a staff managing local tools only");
      Tool existing = existingTool(null);

      BddLogger.when("calling PUT " + NATIONAL_PATH + "/{toolId}");
      putAsStaff(
              NATIONAL_PATH + "/" + existing.getId(),
              UPDATE,
              EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, leaving the tool untouched");
      assertThat(reloaded(existing.getId()).getName()).isEqualTo(NAME);
    }

    @Test
    void thenItShouldReturnNotFound_whenTheToolDoesNotExist() throws Exception {
      BddLogger.when("calling PUT " + NATIONAL_PATH + "/{toolId} for an unknown tool");
      putAsStaff(
              NATIONAL_PATH + "/" + UUID.randomUUID(),
              UPDATE,
              EPermission.PERM_TOOL_NATIONAL_MANAGEMENT)
          .andExpect(status().isNotFound());

      BddLogger.then("it should return 404");
    }

    @Test
    void thenItShouldReturnBadRequest_whenTheUrlIsInvalid() throws Exception {
      BddLogger.given("an existing national tool");
      Tool existing = existingTool(null);

      BddLogger.when("calling PUT " + NATIONAL_PATH + "/{toolId} with an url lacking its scheme");
      putAsStaff(
              NATIONAL_PATH + "/" + existing.getId(),
              new UpdateToolRequest(NAME, DESCRIPTION, "moodle.example.fr"),
              EPermission.PERM_TOOL_NATIONAL_MANAGEMENT)
          .andExpect(status().isBadRequest());

      BddLogger.then("it should return 400, leaving the tool untouched");
      assertThat(reloaded(existing.getId()).getUrl()).isEqualTo(URL);
    }
  }

  @Nested
  class WhenUpdatingALocalTool {

    private static final UpdateToolRequest UPDATE =
        new UpdateToolRequest("Moodle 2", "Nouvelle description", "https://new.example.fr");

    @Test
    void thenItShouldUpdateARegionalTool_ofTheStaffPrimaryInstitution() throws Exception {
      BddLogger.given("an existing regional tool of Rennes and a staff of Rennes");
      Tool existing = existingTool(RENNES_UAI);
      UUID rennesId = institutionIdOf(RENNES_UAI);

      BddLogger.when("calling PUT " + LOCAL_PATH + "/{toolId}");
      putAsStaff(
              LOCAL_PATH + "/" + existing.getId(), UPDATE, EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name", is(UPDATE.name())))
          .andExpect(jsonPath("$.type", is(EToolType.REGIONAL.name())))
          .andExpect(jsonPath("$.institutionId", is(rennesId.toString())));

      BddLogger.then("it should store the new information and keep the tool attached to Rennes");
      Tool stored = reloaded(existing.getId());
      assertThat(stored.getName()).isEqualTo(UPDATE.name());
      assertThat(stored.getDescription()).isEqualTo(UPDATE.description());
      assertThat(stored.getUrl()).isEqualTo(UPDATE.url());
      assertThat(stored.getInstitution()).get().extracting(Institution::getId).isEqualTo(rennesId);
    }

    @Test
    void thenItShouldUpdateALocalTool_ofASecondaryInstitutionOfTheStaffScope() throws Exception {
      BddLogger.given("an existing local tool of the IUT of Rennes and a staff of Rennes");
      Tool existing = existingTool(RENNES_IUT_UAI);

      BddLogger.when("calling PUT " + LOCAL_PATH + "/{toolId}");
      putAsStaff(
              LOCAL_PATH + "/" + existing.getId(), UPDATE, EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.type", is(EToolType.LOCAL.name())))
          .andExpect(jsonPath("$.institutionId", is(institutionIdOf(RENNES_IUT_UAI).toString())));

      BddLogger.then("it should store the new information");
      assertThat(reloaded(existing.getId()).getName()).isEqualTo(UPDATE.name());
    }

    @Test
    void thenItShouldReturnForbidden_whenTheToolIsOutsideTheStaffScope() throws Exception {
      BddLogger.given("an existing tool of Bordeaux and a staff of Rennes");
      Tool existing = existingTool(BORDEAUX_UAI);

      BddLogger.when("calling PUT " + LOCAL_PATH + "/{toolId}");
      putAsStaff(
              LOCAL_PATH + "/" + existing.getId(), UPDATE, EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, leaving the tool untouched");
      assertThat(reloaded(existing.getId()).getName()).isEqualTo(NAME);
    }

    @Test
    void thenItShouldReturnForbidden_whenTheToolIsNational() throws Exception {
      BddLogger.given("an existing national tool");
      Tool existing = existingTool(null);

      BddLogger.when("calling PUT " + LOCAL_PATH + "/{toolId}");
      putAsStaff(
              LOCAL_PATH + "/" + existing.getId(), UPDATE, EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, leaving the tool untouched");
      assertThat(reloaded(existing.getId()).getName()).isEqualTo(NAME);
    }

    @Test
    void thenItShouldReturnForbidden_whenTheStaffOnlyManagesNationalTools() throws Exception {
      BddLogger.given("an existing regional tool of Rennes and a staff managing national tools");
      Tool existing = existingTool(RENNES_UAI);

      BddLogger.when("calling PUT " + LOCAL_PATH + "/{toolId}");
      putAsStaff(
              LOCAL_PATH + "/" + existing.getId(),
              UPDATE,
              EPermission.PERM_TOOL_NATIONAL_MANAGEMENT)
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, leaving the tool untouched");
      assertThat(reloaded(existing.getId()).getName()).isEqualTo(NAME);
    }

    @Test
    void thenItShouldReturnNotFound_whenTheToolDoesNotExist() throws Exception {
      BddLogger.when("calling PUT " + LOCAL_PATH + "/{toolId} for an unknown tool");
      putAsStaff(
              LOCAL_PATH + "/" + UUID.randomUUID(), UPDATE, EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isNotFound());

      BddLogger.then("it should return 404");
    }

    @Test
    void thenItShouldReturnBadRequest_whenTheNameIsBlank() throws Exception {
      BddLogger.given("an existing regional tool of Rennes");
      Tool existing = existingTool(RENNES_UAI);

      BddLogger.when("calling PUT " + LOCAL_PATH + "/{toolId} with a blank name");
      putAsStaff(
              LOCAL_PATH + "/" + existing.getId(),
              new UpdateToolRequest(" ", DESCRIPTION, URL),
              EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isBadRequest());

      BddLogger.then("it should return 400, leaving the tool untouched");
      assertThat(reloaded(existing.getId()).getName()).isEqualTo(NAME);
    }
  }
}
