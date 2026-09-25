package fr.avenirsesr.portfolio.backoffice.cgu.application.adapter.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.cgu.domain.model.Cgu;
import fr.avenirsesr.portfolio.backoffice.cgu.domain.port.output.repository.CguRepository;
import fr.avenirsesr.portfolio.common.file.application.adapter.client.FileClient;
import fr.avenirsesr.portfolio.common.file.application.adapter.dto.FileDTO;
import fr.avenirsesr.portfolio.common.file.domain.model.enums.EFileType;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.AvenirsSecurityHeaders;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

class CguControllerIT extends ContainerConfigurationTest {

  private static final String BASE_PATH = "/back-office/cgu";
  private static final String FILE_NAME = "cgu.html";
  private static final byte[] CONTENT =
      "<html><body>Conditions générales</body></html>".getBytes(StandardCharsets.UTF_8);

  @Autowired private MockMvc mockMvc;
  @Autowired private CguRepository cguRepository;

  @MockitoBean private FileClient fileClient;

  @Value("${security.authentication.api-key}")
  private String apiKeyValue;

  private static MockMultipartFile fileOfType(String mimeType) {
    return new MockMultipartFile("file", FILE_NAME, mimeType, CONTENT);
  }

  private FileDTO storedFile(UUID fileId) {
    return new FileDTO(
        fileId, FILE_NAME, EFileType.HTML, CONTENT.length, "/storage/" + fileId, Instant.now());
  }

  private int lastPublishedVersion() {
    return cguRepository.findLatest().map(Cgu::getVersion).orElse(0);
  }

  @Test
  void shouldPublishTheUploadedHtmlAndSaveItsVersion() throws Exception {
    BddLogger.given("the " + BASE_PATH + " endpoint and an html terms of use file");
    UUID fileId = UUID.randomUUID();
    when(fileClient.upload(any())).thenReturn(storedFile(fileId));
    int previousVersion = lastPublishedVersion();

    BddLogger.when("posting the file with the cgu:update authority");
    BddLogger.then("201 CREATED is returned with the stored file");
    mockMvc
        .perform(
            multipart(BASE_PATH)
                .file(fileOfType(MediaType.TEXT_HTML_VALUE))
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(fileId.toString())))
        .andExpect(jsonPath("$.fileName", is(FILE_NAME)))
        .andExpect(jsonPath("$.fileType", is(EFileType.HTML.name())));

    BddLogger.and("the publication is recorded with the next version");
    assertThat(cguRepository.findLatest())
        .isPresent()
        .get()
        .satisfies(
            cgu -> {
              assertThat(cgu.getFileId()).isEqualTo(fileId);
              assertThat(cgu.getVersion()).isEqualTo(previousVersion + 1);
              assertThat(cgu.getUploadedAt()).isNotNull();
            });
  }

  @Test
  void shouldRejectAFileThatIsNotHtml() throws Exception {
    BddLogger.given("the " + BASE_PATH + " endpoint and a pdf file");
    int previousVersion = lastPublishedVersion();

    BddLogger.when("posting the file");
    BddLogger.then("400 BAD REQUEST is returned");
    mockMvc
        .perform(
            multipart(BASE_PATH)
                .file(fileOfType(MediaType.APPLICATION_PDF_VALUE))
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue))
        .andExpect(status().isBadRequest());

    BddLogger.and("nothing is uploaded nor recorded");
    verifyNoInteractions(fileClient);
    assertThat(lastPublishedVersion()).isEqualTo(previousVersion);
  }

  @Test
  void shouldRejectAnUploadWithoutTheApiKey() throws Exception {
    BddLogger.given("the " + BASE_PATH + " endpoint and an html terms of use file");

    BddLogger.when("posting the file without the api key");
    BddLogger.then("401 UNAUTHORIZED is returned");
    mockMvc
        .perform(
            multipart(BASE_PATH)
                .file(fileOfType(MediaType.TEXT_HTML_VALUE))
                .principal(uuidPrincipal()))
        .andExpect(status().isUnauthorized());

    BddLogger.and("nothing is uploaded");
    verifyNoInteractions(fileClient);
  }

  @Test
  void shouldReturnTheLastPublishedTermsOfUseWithoutAuthentication() throws Exception {
    BddLogger.given("two published terms of use versions");
    UUID outdatedFileId = UUID.randomUUID();
    UUID latestFileId = UUID.randomUUID();
    cguRepository.save(Cgu.create(outdatedFileId, lastPublishedVersion() + 1));
    Cgu latest = cguRepository.save(Cgu.create(latestFileId, lastPublishedVersion() + 1));
    when(fileClient.fetchContent(latestFileId)).thenReturn(CONTENT);

    BddLogger.when("reading " + BASE_PATH + "/latest without any credentials");
    BddLogger.then("200 OK is returned with the highest version and its content");
    mockMvc
        .perform(get(BASE_PATH + "/latest"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(latest.getId().toString())))
        .andExpect(jsonPath("$.version", is(latest.getVersion())))
        .andExpect(jsonPath("$.uploadedAt").exists())
        .andExpect(jsonPath("$.content", is(new String(CONTENT, StandardCharsets.UTF_8))));
  }

  @Test
  void shouldReturnNotFoundWhenNoTermsOfUseWasPublishedYet() throws Exception {
    BddLogger.given("no published terms of use version");

    BddLogger.when("reading " + BASE_PATH + "/latest");
    BddLogger.then("404 NOT FOUND is returned");
    mockMvc.perform(get(BASE_PATH + "/latest")).andExpect(status().isNotFound());

    BddLogger.and("no content is fetched");
    verifyNoInteractions(fileClient);
  }
}
