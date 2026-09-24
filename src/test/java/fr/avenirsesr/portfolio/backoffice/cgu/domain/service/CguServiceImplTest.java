package fr.avenirsesr.portfolio.backoffice.cgu.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import fr.avenirsesr.portfolio.common.file.application.adapter.client.FileClient;
import fr.avenirsesr.portfolio.common.file.application.adapter.dto.FileDTO;
import fr.avenirsesr.portfolio.common.file.application.adapter.request.FileUploadRequest;
import fr.avenirsesr.portfolio.common.file.domain.exception.FileTypeNotSupportedException;
import fr.avenirsesr.portfolio.common.file.domain.model.enums.EFileType;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CguServiceImplTest {

  @Mock private FileClient fileClient;

  @InjectMocks private CguServiceImpl service;

  private static FileUploadRequest requestWithMimeType(String mimeType) {
    return new FileUploadRequest(
        "cgu.html", mimeType, "<html></html>".getBytes(StandardCharsets.UTF_8), false);
  }

  @Test
  void shouldPublishTheUploadedHtmlThroughTheFileClient() {
    BddLogger.given("an html terms of use file");
    FileUploadRequest request = requestWithMimeType("text/html");
    FileDTO uploaded =
        new FileDTO(
            UUID.randomUUID(), "cgu.html", EFileType.HTML, 13L, "/storage/cgu", Instant.now());
    when(fileClient.upload(request)).thenReturn(uploaded);

    BddLogger.when("publishing it");
    FileDTO result = service.publish(request);

    BddLogger.then("the file client stores it and the published file is returned");
    verify(fileClient).upload(request);
    assertThat(result).isEqualTo(uploaded);
  }

  @Test
  void shouldRejectAFileThatIsNotHtml() {
    BddLogger.given("a terms of use file that is not html");
    FileUploadRequest request = requestWithMimeType("application/pdf");

    BddLogger.when("publishing it");
    BddLogger.then("it should be rejected without reaching the file client");
    assertThrows(FileTypeNotSupportedException.class, () -> service.publish(request));

    verifyNoInteractions(fileClient);
  }

  @Test
  void shouldRejectAFileWithAnUnknownMimeType() {
    BddLogger.given("a terms of use file with an unsupported mime type");
    FileUploadRequest request = requestWithMimeType("application/x-unknown");

    BddLogger.when("publishing it");
    BddLogger.then("it should be rejected without reaching the file client");
    assertThrows(FileTypeNotSupportedException.class, () -> service.publish(request));

    verifyNoInteractions(fileClient);
  }
}
