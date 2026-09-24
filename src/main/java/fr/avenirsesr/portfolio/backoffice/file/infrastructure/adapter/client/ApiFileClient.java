package fr.avenirsesr.portfolio.backoffice.file.infrastructure.adapter.client;

import fr.avenirsesr.portfolio.common.file.application.adapter.client.FileClient;
import fr.avenirsesr.portfolio.common.file.application.adapter.dto.FileDTO;
import fr.avenirsesr.portfolio.common.file.application.adapter.request.FileUploadRequest;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.AvenirsSecurityHeaders;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class ApiFileClient implements FileClient {

  private final WebClient webClient;
  private final HttpServletRequest currentRequest;

  @Value("${avenirs.api.file.endpoint}")
  private String fileEndpoint;

  public ApiFileClient(WebClient webClient, HttpServletRequest currentRequest) {
    this.webClient = webClient;
    this.currentRequest = currentRequest;
  }

  @Override
  public FileDTO upload(FileUploadRequest request) {
    log.debug("Uploading file {} to the portfolio api", request.fileName());

    MultipartBodyBuilder body = new MultipartBodyBuilder();
    body.part("file", new ByteArrayResource(request.content()))
        .filename(request.fileName())
        .contentType(MediaType.parseMediaType(request.mimeType()));
    body.part("isRestricted", String.valueOf(request.isRestricted()));

    return webClient
        .post()
        .uri(fileEndpoint)
        .headers(this::forwardLoggedInUser)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(body.build()))
        .retrieve()
        .bodyToMono(FileDTO.class)
        .block();
  }

  @Override
  public FileDTO get(UUID fileId) {
    log.debug("Fetching file {} from the portfolio api", fileId);

    return webClient
        .get()
        .uri(fileEndpoint + "/" + fileId)
        .headers(this::forwardLoggedInUser)
        .retrieve()
        .bodyToMono(FileDTO.class)
        .block();
  }

  @Override
  public void delete(UUID fileId) {
    log.debug("Deleting file {} through the portfolio api", fileId);

    webClient
        .delete()
        .uri(fileEndpoint + "/" + fileId)
        .headers(this::forwardLoggedInUser)
        .retrieve()
        .toBodilessEntity()
        .block();
  }

  private void forwardLoggedInUser(HttpHeaders headers) {
    copyHeader(headers, AvenirsSecurityHeaders.SIGNED_CONTEXT);
    copyHeader(headers, AvenirsSecurityHeaders.CONTEXT_SIGNATURE);
  }

  private void copyHeader(HttpHeaders headers, String name) {
    String value = currentRequest.getHeader(name);
    if (value != null) {
      headers.set(name, value);
    }
  }
}
