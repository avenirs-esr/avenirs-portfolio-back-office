package fr.avenirsesr.portfolio.backoffice.file.infrastructure.adapter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.error.domain.exception.UserNotFoundException;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ExternalUserFileUploaderProviderTest {
  private static final String EPPN = "super.admin@university.com";

  @Mock private ExternalUserRepository externalUserRepository;

  @InjectMocks private ExternalUserFileUploaderProvider provider;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  private static void authenticateAs(String principal) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
  }

  private static ExternalUser externalUser() {
    return ExternalUser.create(
        EPPN,
        "PEG-0001",
        EExternalSource.BACK_OFFICE,
        "0350001A",
        Set.of(EUserCategory.STAFF),
        EPPN,
        "Super",
        "Admin",
        EUserStatus.ACTIVE);
  }

  @Test
  void shouldReturnTheIdOfTheLoggedInExternalUser() {
    BddLogger.given("an authenticated principal known as an external user");
    ExternalUser user = externalUser();
    authenticateAs(EPPN);
    when(externalUserRepository.findByEppn(EPPN)).thenReturn(Optional.of(user));

    BddLogger.when("resolving the uploader");
    UUID uploaderId = provider.currentUploaderId();

    BddLogger.then("the uploader is that external user");
    assertThat(uploaderId).isEqualTo(user.getId());
  }

  @Test
  void shouldDeriveAStableIdFromAPrincipalWithoutExternalUser() {
    BddLogger.given("an authenticated principal that is no external user");
    authenticateAs("internal-service");
    when(externalUserRepository.findByEppn("internal-service")).thenReturn(Optional.empty());

    BddLogger.when("resolving the uploader twice");
    UUID first = provider.currentUploaderId();
    UUID second = provider.currentUploaderId();

    BddLogger.then("the uploader is the same identifier derived from the principal");
    assertThat(first)
        .isEqualTo(second)
        .isEqualTo(UUID.nameUUIDFromBytes("internal-service".getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void shouldRejectAnUnauthenticatedUpload() {
    BddLogger.given("no authentication in the security context");
    SecurityContextHolder.clearContext();

    BddLogger.when("resolving the uploader");
    BddLogger.then("no uploader can be resolved");
    assertThrows(UserNotFoundException.class, () -> provider.currentUploaderId());
  }
}
