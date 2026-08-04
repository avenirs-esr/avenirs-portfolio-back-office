package fr.avenirsesr.portfolio.backoffice.shared.application.adapter.controller;

import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("back-office/seeder")
public class SeederController {

  private final SeederOrchestrator seederOrchestrator;

  @Value("${admin.token:}")
  private String adminToken;

  @PostMapping("/reset")
  public ResponseEntity<Void> resetAndSeed(@RequestHeader(name = "X-ADMIN-TOKEN") String token) {
    HttpStatus rejection = checkAdminToken(token);
    if (rejection != null) {
      return ResponseEntity.status(rejection).build();
    }

    log.warn("Resetting and reseeding database via admin endpoint");
    seederOrchestrator.resetAll();
    seederOrchestrator.seedAll();
    log.info("✔ Database reset and reseeded successfully");

    return ResponseEntity.noContent().build();
  }

  private HttpStatus checkAdminToken(String token) {
    if (adminToken == null || adminToken.isBlank()) {
      return HttpStatus.SERVICE_UNAVAILABLE;
    }
    if (!adminToken.equals(token)) {
      return HttpStatus.FORBIDDEN;
    }
    return null;
  }
}
