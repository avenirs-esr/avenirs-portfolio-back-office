package fr.avenirsesr.portfolio.backoffice.tool.application.adapter.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import fr.avenirsesr.portfolio.common.file.domain.model.File;
import fr.avenirsesr.portfolio.common.file.domain.model.enums.EFileType;
import fr.avenirsesr.portfolio.common.file.domain.port.output.repository.FileRepository;
import fr.avenirsesr.portfolio.common.file.domain.port.output.service.FileStorageService;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.EPermission;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.HmacAuthenticationToken;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
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
  @Autowired private FileRepository fileRepository;

  @MockitoBean private FileStorageService fileStorageService;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  private UUID institutionIdOf(String uai) {
    return institutionRepository.findByUai(uai).map(Institution::getId).orElseThrow();
  }

  private Tool existingTool(String institutionUai) {
    return existingTool(institutionUai, null);
  }

  private Tool existingTool(String institutionUai, File banner) {
    Institution institution =
        institutionUai == null
            ? null
            : institutionRepository.findByUai(institutionUai).orElseThrow();
    return toolRepository.save(Tool.create(NAME, DESCRIPTION, URL, banner, institution));
  }

  private File storedBanner() {
    return fileRepository.save(
        File.create(
            UUID.randomUUID(),
            EFileType.PNG,
            "previous.png",
            1024,
            "previous-locator",
            UUID.randomUUID(),
            false));
  }

  private static MockMultipartFile pngBanner() {
    return new MockMultipartFile("file", "banner.png", "image/png", new byte[] {1, 2, 3});
  }

  private Tool reloaded(UUID toolId) {
    return stored(toolId).orElseThrow();
  }

  private Optional<Tool> stored(UUID toolId) {
    entityManager.flush();
    entityManager.clear();
    return toolRepository.findById(toolId);
  }

  private ResultActions postAsStaff(String path, Object payload, EPermission... permissions)
      throws Exception {
    return mockMvc.perform(withJson(asStaff(post(path), permissions), payload));
  }

  private ResultActions putAsStaff(String path, Object payload, EPermission... permissions)
      throws Exception {
    return mockMvc.perform(withJson(asStaff(put(path), permissions), payload));
  }

  private ResultActions uploadAsStaff(
      String path, MockMultipartFile file, EPermission... permissions) throws Exception {
    return mockMvc.perform(asStaff(multipart(path).file(file), permissions));
  }

  private ResultActions deleteAsStaff(String path, EPermission... permissions) throws Exception {
    return mockMvc.perform(asStaff(delete(path), permissions));
  }

  private MockHttpServletRequestBuilder withJson(
      MockHttpServletRequestBuilder request, Object payload) throws Exception {
    return request
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(payload));
  }

  private static <B extends AbstractMockHttpServletRequestBuilder<B>> B asStaff(
      B request, EPermission... permissions) {
    return request
        .header("X-Forwarded-For", "203.0.113.10")
        .with(
            authentication(
                new HmacAuthenticationToken(
                    STAFF_EPPN,
                    Arrays.stream(permissions)
                        .map(permission -> new SimpleGrantedAuthority(permission.authority()))
                        .toList())));
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

  @Nested
  class WhenManagingTheBannerOfANationalTool {

    @Test
    void thenItShouldUploadTheBannerAndAttachItToTheTool() throws Exception {
      BddLogger.given("an existing national tool without banner");
      Tool existing = existingTool(null);
      when(fileStorageService.upload(any())).thenReturn("new-locator");

      BddLogger.when("calling POST " + NATIONAL_PATH + "/{toolId}/banner with a png");
      String body =
          uploadAsStaff(
                  NATIONAL_PATH + "/" + existing.getId() + "/banner",
                  pngBanner(),
                  EPermission.PERM_TOOL_NATIONAL_MANAGEMENT)
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.fileName", is("banner.png")))
              .andExpect(jsonPath("$.fileType", is(EFileType.PNG.name())))
              .andReturn()
              .getResponse()
              .getContentAsString();

      BddLogger.then("it should attach the stored file to the tool");
      UUID bannerId = UUID.fromString(objectMapper.readTree(body).get("id").asText());
      assertThat(reloaded(existing.getId()).getBanner())
          .get()
          .extracting(File::getId)
          .isEqualTo(bannerId);
    }

    @Test
    void thenItShouldReplaceAndDeleteThePreviousBanner() throws Exception {
      BddLogger.given("an existing national tool with a banner");
      File previous = storedBanner();
      Tool existing = existingTool(null, previous);
      when(fileStorageService.upload(any())).thenReturn("new-locator");

      BddLogger.when("calling POST " + NATIONAL_PATH + "/{toolId}/banner with another png");
      uploadAsStaff(
              NATIONAL_PATH + "/" + existing.getId() + "/banner",
              pngBanner(),
              EPermission.PERM_TOOL_NATIONAL_MANAGEMENT)
          .andExpect(status().isCreated());

      BddLogger.then("it should attach the new banner and delete the previous file");
      assertThat(reloaded(existing.getId()).getBanner())
          .get()
          .extracting(File::getUri)
          .isEqualTo("new-locator");
      assertThat(fileRepository.findById(previous.getId())).isEmpty();
      verify(fileStorageService).delete("previous-locator");
    }

    @Test
    void thenItShouldReturnBadRequest_whenTheFileIsNotAnImage() throws Exception {
      BddLogger.given("an existing national tool");
      Tool existing = existingTool(null);

      BddLogger.when("calling POST " + NATIONAL_PATH + "/{toolId}/banner with a pdf");
      uploadAsStaff(
              NATIONAL_PATH + "/" + existing.getId() + "/banner",
              new MockMultipartFile("file", "banner.pdf", "application/pdf", new byte[] {1}),
              EPermission.PERM_TOOL_NATIONAL_MANAGEMENT)
          .andExpect(status().isBadRequest());

      BddLogger.then("it should return 400, without storing anything");
      verify(fileStorageService, never()).upload(any());
    }

    @Test
    void thenItShouldReturnForbidden_whenTheStaffOnlyManagesLocalTools() throws Exception {
      BddLogger.given("an existing national tool and a staff managing local tools only");
      Tool existing = existingTool(null);

      BddLogger.when("calling POST " + NATIONAL_PATH + "/{toolId}/banner");
      uploadAsStaff(
              NATIONAL_PATH + "/" + existing.getId() + "/banner",
              pngBanner(),
              EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, without storing anything");
      verify(fileStorageService, never()).upload(any());
    }

    @Test
    void thenItShouldDeleteTheBanner() throws Exception {
      BddLogger.given("an existing national tool with a banner");
      File banner = storedBanner();
      Tool existing = existingTool(null, banner);

      BddLogger.when("calling DELETE " + NATIONAL_PATH + "/{toolId}/banner");
      deleteAsStaff(
              NATIONAL_PATH + "/" + existing.getId() + "/banner",
              EPermission.PERM_TOOL_NATIONAL_MANAGEMENT)
          .andExpect(status().isNoContent());

      BddLogger.then("it should detach the banner and delete its file");
      assertThat(reloaded(existing.getId()).getBanner()).isEmpty();
      assertThat(fileRepository.findById(banner.getId())).isEmpty();
      verify(fileStorageService).delete("previous-locator");
    }

    @Test
    void thenItShouldReturnNotFound_whenTheToolDoesNotExist() throws Exception {
      BddLogger.when("calling DELETE " + NATIONAL_PATH + "/{toolId}/banner for an unknown tool");
      deleteAsStaff(
              NATIONAL_PATH + "/" + UUID.randomUUID() + "/banner",
              EPermission.PERM_TOOL_NATIONAL_MANAGEMENT)
          .andExpect(status().isNotFound());

      BddLogger.then("it should return 404");
    }
  }

  @Nested
  class WhenManagingTheBannerOfALocalTool {

    @Test
    void thenItShouldUploadTheBannerOfAToolOfTheStaffScope() throws Exception {
      BddLogger.given("an existing regional tool of Rennes and a staff of Rennes");
      Tool existing = existingTool(RENNES_UAI);
      when(fileStorageService.upload(any())).thenReturn("new-locator");

      BddLogger.when("calling POST " + LOCAL_PATH + "/{toolId}/banner with a png");
      uploadAsStaff(
              LOCAL_PATH + "/" + existing.getId() + "/banner",
              pngBanner(),
              EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isCreated());

      BddLogger.then("it should attach the stored file to the tool");
      assertThat(reloaded(existing.getId()).getBanner())
          .get()
          .extracting(File::getUri)
          .isEqualTo("new-locator");
    }

    @Test
    void thenItShouldReturnForbidden_whenTheToolIsOutsideTheStaffScope() throws Exception {
      BddLogger.given("an existing tool of Bordeaux and a staff of Rennes");
      Tool existing = existingTool(BORDEAUX_UAI);

      BddLogger.when("calling POST " + LOCAL_PATH + "/{toolId}/banner");
      uploadAsStaff(
              LOCAL_PATH + "/" + existing.getId() + "/banner",
              pngBanner(),
              EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, without storing anything");
      verify(fileStorageService, never()).upload(any());
      assertThat(reloaded(existing.getId()).getBanner()).isEmpty();
    }

    @Test
    void thenItShouldDeleteTheBannerOfAToolOfTheStaffScope() throws Exception {
      BddLogger.given("an existing local tool of the IUT of Rennes with a banner");
      File banner = storedBanner();
      Tool existing = existingTool(RENNES_IUT_UAI, banner);

      BddLogger.when("calling DELETE " + LOCAL_PATH + "/{toolId}/banner");
      deleteAsStaff(
              LOCAL_PATH + "/" + existing.getId() + "/banner",
              EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isNoContent());

      BddLogger.then("it should detach the banner and delete its file");
      assertThat(reloaded(existing.getId()).getBanner()).isEmpty();
      assertThat(fileRepository.findById(banner.getId())).isEmpty();
    }

    @Test
    void thenItShouldReturnForbidden_whenDeletingTheBannerOfANationalTool() throws Exception {
      BddLogger.given("an existing national tool with a banner");
      File banner = storedBanner();
      Tool existing = existingTool(null, banner);

      BddLogger.when("calling DELETE " + LOCAL_PATH + "/{toolId}/banner");
      deleteAsStaff(
              LOCAL_PATH + "/" + existing.getId() + "/banner",
              EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, keeping the banner");
      assertThat(reloaded(existing.getId()).getBanner())
          .get()
          .extracting(File::getId)
          .isEqualTo(banner.getId());
      verify(fileStorageService, never()).delete(any());
    }
  }

  @Nested
  class WhenDeletingANationalTool {

    @Test
    void thenItShouldDeleteTheToolAndItsBanner() throws Exception {
      BddLogger.given("an existing national tool with a banner");
      File banner = storedBanner();
      Tool existing = existingTool(null, banner);

      BddLogger.when("calling DELETE " + NATIONAL_PATH + "/{toolId}");
      deleteAsStaff(
              NATIONAL_PATH + "/" + existing.getId(), EPermission.PERM_TOOL_NATIONAL_MANAGEMENT)
          .andExpect(status().isNoContent());

      BddLogger.then("it should delete the tool and the file of its banner");
      assertThat(stored(existing.getId())).isEmpty();
      assertThat(fileRepository.findById(banner.getId())).isEmpty();
      verify(fileStorageService).delete("previous-locator");
    }

    @Test
    void thenItShouldReturnForbidden_whenTheToolIsAttachedToAnInstitution() throws Exception {
      BddLogger.given("an existing regional tool of Rennes");
      Tool existing = existingTool(RENNES_UAI);

      BddLogger.when("calling DELETE " + NATIONAL_PATH + "/{toolId}");
      deleteAsStaff(
              NATIONAL_PATH + "/" + existing.getId(), EPermission.PERM_TOOL_NATIONAL_MANAGEMENT)
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, keeping the tool");
      assertThat(stored(existing.getId())).isPresent();
    }

    @Test
    void thenItShouldReturnForbidden_whenTheStaffOnlyManagesLocalTools() throws Exception {
      BddLogger.given("an existing national tool and a staff managing local tools only");
      Tool existing = existingTool(null);

      BddLogger.when("calling DELETE " + NATIONAL_PATH + "/{toolId}");
      deleteAsStaff(NATIONAL_PATH + "/" + existing.getId(), EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, keeping the tool");
      assertThat(stored(existing.getId())).isPresent();
    }

    @Test
    void thenItShouldReturnNotFound_whenTheToolDoesNotExist() throws Exception {
      BddLogger.when("calling DELETE " + NATIONAL_PATH + "/{toolId} for an unknown tool");
      deleteAsStaff(
              NATIONAL_PATH + "/" + UUID.randomUUID(), EPermission.PERM_TOOL_NATIONAL_MANAGEMENT)
          .andExpect(status().isNotFound());

      BddLogger.then("it should return 404");
    }
  }

  @Nested
  class WhenDeletingALocalTool {

    @Test
    void thenItShouldDeleteALocalToolOfTheStaffScopeAndItsBanner() throws Exception {
      BddLogger.given("an existing local tool of the IUT of Rennes with a banner");
      File banner = storedBanner();
      Tool existing = existingTool(RENNES_IUT_UAI, banner);

      BddLogger.when("calling DELETE " + LOCAL_PATH + "/{toolId}");
      deleteAsStaff(LOCAL_PATH + "/" + existing.getId(), EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isNoContent());

      BddLogger.then("it should delete the tool and the file of its banner");
      assertThat(stored(existing.getId())).isEmpty();
      assertThat(fileRepository.findById(banner.getId())).isEmpty();
    }

    @Test
    void thenItShouldDeleteARegionalToolOfTheStaffScope() throws Exception {
      BddLogger.given("an existing regional tool of Rennes without banner");
      Tool existing = existingTool(RENNES_UAI);

      BddLogger.when("calling DELETE " + LOCAL_PATH + "/{toolId}");
      deleteAsStaff(LOCAL_PATH + "/" + existing.getId(), EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isNoContent());

      BddLogger.then("it should delete the tool, without touching the storage");
      assertThat(stored(existing.getId())).isEmpty();
      verify(fileStorageService, never()).delete(any());
    }

    @Test
    void thenItShouldReturnForbidden_whenTheToolIsOutsideTheStaffScope() throws Exception {
      BddLogger.given("an existing tool of Bordeaux and a staff of Rennes");
      Tool existing = existingTool(BORDEAUX_UAI);

      BddLogger.when("calling DELETE " + LOCAL_PATH + "/{toolId}");
      deleteAsStaff(LOCAL_PATH + "/" + existing.getId(), EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, keeping the tool");
      assertThat(stored(existing.getId())).isPresent();
    }

    @Test
    void thenItShouldReturnForbidden_whenTheToolIsNational() throws Exception {
      BddLogger.given("an existing national tool");
      Tool existing = existingTool(null);

      BddLogger.when("calling DELETE " + LOCAL_PATH + "/{toolId}");
      deleteAsStaff(LOCAL_PATH + "/" + existing.getId(), EPermission.PERM_TOOL_LOCAL_MANAGEMENT)
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, keeping the tool");
      assertThat(stored(existing.getId())).isPresent();
    }

    @Test
    void thenItShouldReturnForbidden_whenTheStaffOnlyManagesNationalTools() throws Exception {
      BddLogger.given("an existing regional tool of Rennes and a staff managing national tools");
      Tool existing = existingTool(RENNES_UAI);

      BddLogger.when("calling DELETE " + LOCAL_PATH + "/{toolId}");
      deleteAsStaff(LOCAL_PATH + "/" + existing.getId(), EPermission.PERM_TOOL_NATIONAL_MANAGEMENT)
          .andExpect(status().isForbidden());

      BddLogger.then("it should return 403, keeping the tool");
      assertThat(stored(existing.getId())).isPresent();
    }
  }
}
