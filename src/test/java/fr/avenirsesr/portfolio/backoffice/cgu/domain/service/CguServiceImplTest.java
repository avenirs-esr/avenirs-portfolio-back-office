package fr.avenirsesr.portfolio.backoffice.cgu.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import fr.avenirsesr.portfolio.backoffice.cgu.domain.exception.CguNotFoundException;
import fr.avenirsesr.portfolio.backoffice.cgu.domain.model.Cgu;
import fr.avenirsesr.portfolio.backoffice.cgu.domain.port.output.repository.CguRepository;
import fr.avenirsesr.portfolio.common.cgu.application.adapter.dto.CguDTO;
import fr.avenirsesr.portfolio.common.file.application.adapter.client.FileClient;
import fr.avenirsesr.portfolio.common.file.application.adapter.dto.FileDTO;
import fr.avenirsesr.portfolio.common.file.application.adapter.request.FileUploadRequest;
import fr.avenirsesr.portfolio.common.file.domain.exception.FileTypeNotSupportedException;
import fr.avenirsesr.portfolio.common.file.domain.model.enums.EFileType;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CguServiceImplTest {

  @Mock private FileClient fileClient;
  @Mock private CguRepository cguRepository;

  @InjectMocks private CguServiceImpl service;

  private static FileUploadRequest requestWithMimeType(String mimeType) {
    return new FileUploadRequest(
        "cgu.html", mimeType, "<html></html>".getBytes(StandardCharsets.UTF_8), false);
  }

  private static FileDTO uploadedFile() {
    return new FileDTO(
        UUID.randomUUID(), "cgu.html", EFileType.HTML, 13L, "/storage/cgu", Instant.now());
  }

  private ArgumentCaptor<Cgu> captureSavedCgu() {
    ArgumentCaptor<Cgu> captor = ArgumentCaptor.forClass(Cgu.class);
    verify(cguRepository).save(captor.capture());
    return captor;
  }

  @Test
  void shouldPublishTheUploadedHtmlAndSaveItsFirstVersion() {
    BddLogger.given("an html terms of use file and no version published yet");
    FileUploadRequest request = requestWithMimeType("text/html");
    FileDTO uploaded = uploadedFile();
    when(fileClient.upload(request)).thenReturn(uploaded);
    when(cguRepository.findLatest()).thenReturn(Optional.empty());
    when(cguRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("publishing it");
    FileDTO result = service.publish(request);

    BddLogger.then("the uploaded file is saved as the first version");
    Cgu saved = captureSavedCgu().getValue();
    assertThat(saved.getFileId()).isEqualTo(uploaded.id());
    assertThat(saved.getVersion()).isEqualTo(1);
    assertThat(saved.getUploadedAt()).isNotNull();
    assertThat(result).isEqualTo(uploaded);
  }

  @Test
  void shouldIncrementTheVersionOfTheLastPublishedTermsOfUse() {
    BddLogger.given("an html terms of use file and a fourth version already published");
    FileUploadRequest request = requestWithMimeType("text/html");
    when(fileClient.upload(request)).thenReturn(uploadedFile());
    when(cguRepository.findLatest()).thenReturn(Optional.of(Cgu.create(UUID.randomUUID(), 4)));
    when(cguRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("publishing it");
    service.publish(request);

    BddLogger.then("it is saved as the fifth version");
    assertThat(captureSavedCgu().getValue().getVersion()).isEqualTo(5);
  }

  @Test
  void shouldRejectAFileThatIsNotHtml() {
    BddLogger.given("a terms of use file that is not html");
    FileUploadRequest request = requestWithMimeType("application/pdf");

    BddLogger.when("publishing it");
    BddLogger.then("it should be rejected without reaching the file client nor the database");
    assertThrows(FileTypeNotSupportedException.class, () -> service.publish(request));

    verifyNoInteractions(fileClient, cguRepository);
  }

  @Test
  void shouldRejectAFileWithAnUnknownMimeType() {
    BddLogger.given("a terms of use file with an unsupported mime type");
    FileUploadRequest request = requestWithMimeType("application/x-unknown");

    BddLogger.when("publishing it");
    BddLogger.then("it should be rejected without reaching the file client nor the database");
    assertThrows(FileTypeNotSupportedException.class, () -> service.publish(request));

    verifyNoInteractions(fileClient, cguRepository);
  }

  @Test
  void shouldReturnTheLastPublishedTermsOfUseWithItsContent() {
    BddLogger.given("a published terms of use version");
    UUID fileId = UUID.randomUUID();
    Cgu published = Cgu.create(fileId, 3);
    when(cguRepository.findLatest()).thenReturn(Optional.of(published));
    when(fileClient.fetchContent(fileId))
        .thenReturn("<html>cgu</html>".getBytes(StandardCharsets.UTF_8));

    BddLogger.when("reading the latest version");
    CguDTO result = service.getLatest();

    BddLogger.then("its identifier, version, upload date and content are returned");
    assertThat(result.id()).isEqualTo(published.getId());
    assertThat(result.version()).isEqualTo(3);
    assertThat(result.uploadedAt()).isEqualTo(published.getUploadedAt());
    assertThat(result.content()).isEqualTo("<html>cgu</html>");
  }

  @Test
  void shouldFailWhenNoTermsOfUseWasPublishedYet() {
    BddLogger.given("no published terms of use version");
    when(cguRepository.findLatest()).thenReturn(Optional.empty());

    BddLogger.when("reading the latest version");
    BddLogger.then("it should fail without reaching the file client");
    assertThrows(CguNotFoundException.class, () -> service.getLatest());

    verifyNoInteractions(fileClient);
  }
}
