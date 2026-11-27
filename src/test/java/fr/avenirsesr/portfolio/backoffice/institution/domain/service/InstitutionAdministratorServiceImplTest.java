package fr.avenirsesr.portfolio.backoffice.institution.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.ExternalUserNotStaffException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionAdministratorRoleGrantException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotPrimaryException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionAdministrator;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.client.InstitutionAdministratorRoleGranter;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionAdministratorRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.LoggedInExternalUserService;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import fr.avenirsesr.portfolio.common.user.domain.exceptions.ExternalUserNotFoundException;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class InstitutionAdministratorServiceImplTest {

  @Mock private InstitutionAdministratorRepository institutionAdministratorRepository;
  @Mock private InstitutionRepository institutionRepository;
  @Mock private ExternalUserRepository externalUserRepository;
  @Mock private LoggedInExternalUserService loggedInExternalUserService;
  @Mock private InstitutionAdministratorRoleGranter roleGranter;

  @InjectMocks private InstitutionAdministratorServiceImpl service;

  private static ExternalUser staffUser() {
    return externalUser(Set.of(EUserCategory.STAFF));
  }

  private static ExternalUser studentUser() {
    return externalUser(Set.of(EUserCategory.STUDENT));
  }

  private static ExternalUser externalUser(Set<EUserCategory> categories) {
    return ExternalUser.create(
        "ethan.perrin@university.com",
        "TEACH-EP-09",
        EExternalSource.BACK_OFFICE,
        "0350001A",
        categories,
        "ethan.perrin@university.com",
        "Ethan",
        "Perrin",
        EUserStatus.ACTIVE);
  }

  private static Institution primaryInstitution(UUID id) {
    return institution(id, EInstitutionType.PRIMARY);
  }

  private static Institution secondaryInstitution(UUID id) {
    return institution(id, EInstitutionType.SECONDARY);
  }

  private static Institution institution(UUID id, EInstitutionType type) {
    return Institution.create(
        id, "Université de Rennes", "UR", "0350001A", "13000550100015", type, null);
  }

  @Nested
  class WhenDesignatingAStaffUserToPrimaryInstitution {

    @Test
    void shouldPersistEachDesignationAndGrantTheroleonFirstDesignation() {
      BddLogger.given("a staff user, two primary institution, no prior designation");
      UUID userId = UUID.randomUUID();
      UUID rennesId = UUID.randomUUID();
      UUID bordeauxId = UUID.randomUUID();
      Institution rennes = primaryInstitution(rennesId);
      Institution bordeaux = primaryInstitution(bordeauxId);

      when(externalUserRepository.findById(userId)).thenReturn(Optional.of(staffUser()));
      when(institutionAdministratorRepository.findInstitutionIdsByUser(userId))
          .thenReturn(List.of());
      when(institutionRepository.findAllById(List.of(rennesId, bordeauxId)))
          .thenReturn(List.of(rennes, bordeaux));
      when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(staffUser());

      BddLogger.when("designating the user to both institutions");
      List<Institution> administered = service.designate(userId, List.of(rennesId, bordeauxId));

      BddLogger.then("both designations are saved, the role is granted once, all are returned");
      verify(institutionAdministratorRepository, times(2))
          .save(any(InstitutionAdministrator.class));
      verify(roleGranter).grantAdminRole(userId);
      assertThat(administered).containsExactly(rennes, bordeaux);
    }

    @Test
    void shouldIgnoreAnAlreadyExistingDesignationAndNotGrantAgain() {
      BddLogger.given("a staff user already administrating one of the two institutions");
      UUID userId = UUID.randomUUID();
      UUID rennesId = UUID.randomUUID();
      UUID bordeauxId = UUID.randomUUID();
      Institution rennes = primaryInstitution(rennesId);
      Institution bordeaux = primaryInstitution(bordeauxId);
      when(externalUserRepository.findById(userId)).thenReturn(Optional.of(staffUser()));
      when(institutionAdministratorRepository.findInstitutionIdsByUser(userId))
          .thenReturn(List.of(rennesId));
      when(institutionRepository.findAllById(List.of(rennesId, bordeauxId)))
          .thenReturn(List.of(rennes, bordeaux));
      when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(staffUser());
      BddLogger.when("designating the user to both institutions");
      List<Institution> administered = service.designate(userId, List.of(rennesId, bordeauxId));

      BddLogger.then("only the missing designation is saved and the role is not granted again");
      verify(institutionAdministratorRepository, times(1))
          .save(any(InstitutionAdministrator.class));
      verify(roleGranter, never()).grantAdminRole(any());
      assertThat(administered).containsExactly(rennes, bordeaux);
    }
  }

  @Nested
  class WhenTheUserCannotBeDesignated {

    @Test
    void shouldThrowWhenTheUserDoesNotExist() {
      BddLogger.given(" an unknown user");

      UUID userId = UUID.randomUUID();

      when(externalUserRepository.findById(userId)).thenReturn(Optional.empty());
      BddLogger.when("designating them");
      BddLogger.then("an external user not found error is raised and nothing is written");

      assertThrows(
          ExternalUserNotFoundException.class,
          () -> service.designate(userId, List.of(UUID.randomUUID())));

      verifyNoInteractions(institutionAdministratorRepository, institutionRepository, roleGranter);
    }

    @Test
    void shouldThrowWhenTheUserIsNotStaff() {
      BddLogger.given("a non-staff user");
      UUID userId = UUID.randomUUID();
      when(externalUserRepository.findById(userId)).thenReturn(Optional.of(studentUser()));

      BddLogger.when("designating them");
      BddLogger.then("a not-staff error is raised and nothing is written");
      assertThrows(
          ExternalUserNotStaffException.class,
          () -> service.designate(userId, List.of(UUID.randomUUID())));
      verify(institutionAdministratorRepository, never()).save(any());
      verify(roleGranter, never()).grantAdminRole(any());
    }
  }

  @Nested
  class WhenAnInstitutionIsInvalid {

    @Test
    void shouldWriteNothingWhenInstitutionIsUnknown() {
      BddLogger.given("a staff user, one primary and one unknown institution");
      UUID userId = UUID.randomUUID();
      UUID knownId = UUID.randomUUID();
      UUID unknownId = UUID.randomUUID();
      when(externalUserRepository.findById(userId)).thenReturn(Optional.of(staffUser()));
      when(institutionAdministratorRepository.findInstitutionIdsByUser(userId))
          .thenReturn(List.of());
      when(institutionRepository.findAllById(List.of(knownId, unknownId)))
          .thenReturn(List.of(primaryInstitution(knownId)));

      BddLogger.when("designating the user to both");
      BddLogger.then("an institution not found error is raised and nothing is written");
      assertThrows(
          InstitutionNotFoundException.class,
          () -> service.designate(userId, List.of(knownId, unknownId)));
      verify(institutionAdministratorRepository, never()).save(any());
      verify(roleGranter, never()).grantAdminRole(any());
    }

    @Test
    void shouldWriteNothingWhenAnInstitutionIsSecondary() {
      BddLogger.given("a staff user, one primary and one secondary institution");
      UUID userId = UUID.randomUUID();
      UUID primaryId = UUID.randomUUID();
      UUID secondaryId = UUID.randomUUID();
      when(externalUserRepository.findById(userId)).thenReturn(Optional.of(staffUser()));
      when(externalUserRepository.findById(userId)).thenReturn(Optional.of(staffUser()));
      when(institutionAdministratorRepository.findInstitutionIdsByUser(userId))
          .thenReturn(List.of());
      when(institutionRepository.findAllById(List.of(primaryId, secondaryId)))
          .thenReturn(List.of(primaryInstitution(primaryId), secondaryInstitution(secondaryId)));

      BddLogger.when("designating the user to both");
      BddLogger.then("a not-primary error is raised and nothing is written");
      assertThrows(
          InstitutionNotPrimaryException.class,
          () -> service.designate(userId, List.of(primaryId, secondaryId)));
      verify(institutionAdministratorRepository, never()).save(any());
      verify(roleGranter, never()).grantAdminRole(any());
    }
  }

  @Nested
  class WhenTheRoleGrantFails {
    @Test
    void shouldNotPersistDesignationsWhenTheGrantFails() {
      BddLogger.given("a valid first designation whose role grant will fail");
      UUID userId = UUID.randomUUID();
      UUID rennesId = UUID.randomUUID();
      UUID bordeauxId = UUID.randomUUID();
      when(externalUserRepository.findById(userId)).thenReturn(Optional.of(staffUser()));
      when(institutionAdministratorRepository.findInstitutionIdsByUser(userId))
          .thenReturn(List.of());
      when(institutionRepository.findAllById(List.of(rennesId, bordeauxId)))
          .thenReturn(List.of(primaryInstitution(rennesId), primaryInstitution(bordeauxId)));
      when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(staffUser());
      doThrow(new RuntimeException("access-control unavailable"))
          .when(roleGranter)
          .grantAdminRole(userId);

      BddLogger.when("designating the user");
      BddLogger.then("the table writes are compensated and the failure is propagated");
      assertThrows(
          InstitutionAdministratorRoleGrantException.class,
          () -> service.designate(userId, List.of(rennesId, bordeauxId)));
      verify(roleGranter).grantAdminRole(userId);
      verify(institutionAdministratorRepository, never()).save(any(InstitutionAdministrator.class));
    }
  }
}
