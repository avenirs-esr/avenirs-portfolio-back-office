package fr.avenirsesr.portfolio.backoffice.externaluser.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExternalUserServiceImplTest {

  private static final UUID EXTERNAL_USER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000001");

  private static final String EPPN = "lucas.tessier@university.com";
  private static final String UNKNOWN_EPPN = "unknown@university.com";
  private static final String FIRST_NAME = "Lucas";
  private static final String LAST_NAME = "Tessier";
  private static final String EMAIL = "lucas.tessier@university.com";
  private static final EUserCategory CATEGORY = EUserCategory.STUDENT;
  private static final String EXTERNAL_ID = "PEG-0001";
  private static final EExternalSource SOURCE = EExternalSource.PEGASE;
  private static final UUID INSTITUTION_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

  @Mock private ExternalUserRepository externalUserRepository;
  @Mock private InstitutionRepository institutionRepository;
  @Mock private GroupRepository groupRepository;

  @InjectMocks private ExternalUserServiceImpl service;

  private Institution institution;
  private Group group;

  @BeforeEach
  void setupCommonStubs() {
    institution = mock(Institution.class);
    lenient().when(institution.getId()).thenReturn(INSTITUTION_ID);

    group = mock(Group.class);
    lenient().when(group.getId()).thenReturn(GROUP_ID);

    lenient()
        .when(institutionRepository.findById(INSTITUTION_ID))
        .thenReturn(Optional.of(institution));
    lenient().when(groupRepository.findById(GROUP_ID)).thenReturn(Optional.of(group));
  }

  @Nested
  class GivenExternalUserService {

    @BeforeEach
    void setupGiven() {
      BddLogger.given("an external user service");
    }

    @Nested
    class WhenImportingExternalUserWithStatus {

      private ExternalUser result;

      @BeforeEach
      void setupWhen() {
        BddLogger.when("importing an external user with explicit status");

        result =
            service.importExternalUser(
                EPPN,
                FIRST_NAME,
                LAST_NAME,
                EMAIL,
                CATEGORY,
                EXTERNAL_ID,
                SOURCE,
                INSTITUTION_ID,
                GROUP_ID,
                EUserStatus.ACTIVE);
      }

      @Test
      void thenItShouldCreateExternalUser() {
        BddLogger.then("it should create external user");

        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        assertEquals(EPPN, result.getEppn());
        assertEquals(FIRST_NAME, result.getFirstName());
        assertEquals(LAST_NAME, result.getLastName());
        assertEquals(EMAIL, result.getEmail());
        assertEquals(CATEGORY, result.getCategory());
        assertEquals(EXTERNAL_ID, result.getExternalId());
        assertEquals(SOURCE, result.getSource());
        assertEquals(INSTITUTION_ID, result.getInstitution().getId());
        assertEquals(GROUP_ID, result.getGroup().getId());
        assertEquals(EUserStatus.ACTIVE, result.getStatus());
      }

      @Test
      void thenItShouldSaveExternalUser() {
        BddLogger.then("it should save external user");

        ArgumentCaptor<ExternalUser> captor = ArgumentCaptor.forClass(ExternalUser.class);

        verify(institutionRepository).findById(INSTITUTION_ID);
        verify(groupRepository).findById(GROUP_ID);
        verify(externalUserRepository).save(captor.capture());
        verifyNoMoreInteractions(externalUserRepository, institutionRepository, groupRepository);

        ExternalUser savedExternalUser = captor.getValue();

        assertEquals(result, savedExternalUser);
        assertEquals(EPPN, savedExternalUser.getEppn());
        assertEquals(FIRST_NAME, savedExternalUser.getFirstName());
        assertEquals(LAST_NAME, savedExternalUser.getLastName());
        assertEquals(EMAIL, savedExternalUser.getEmail());
        assertEquals(CATEGORY, savedExternalUser.getCategory());
        assertEquals(EXTERNAL_ID, savedExternalUser.getExternalId());
        assertEquals(SOURCE, savedExternalUser.getSource());
        assertEquals(INSTITUTION_ID, savedExternalUser.getInstitution().getId());
        assertEquals(GROUP_ID, savedExternalUser.getGroup().getId());
        assertEquals(EUserStatus.ACTIVE, savedExternalUser.getStatus());
      }
    }

    @Nested
    class WhenImportingExternalUserWithoutStatus {

      private ExternalUser result;

      @BeforeEach
      void setupWhen() {
        BddLogger.when("importing an external user without status");

        result =
            service.importExternalUser(
                EPPN,
                FIRST_NAME,
                LAST_NAME,
                EMAIL,
                CATEGORY,
                EXTERNAL_ID,
                SOURCE,
                INSTITUTION_ID,
                GROUP_ID,
                null);
      }

      @Test
      void thenItShouldDefaultStatusToActive() {
        BddLogger.then("it should default status to active");

        assertNotNull(result);
        assertEquals(EUserStatus.ACTIVE, result.getStatus());

        ArgumentCaptor<ExternalUser> captor = ArgumentCaptor.forClass(ExternalUser.class);

        verify(externalUserRepository).save(captor.capture());
        verifyNoMoreInteractions(externalUserRepository);

        assertEquals(EUserStatus.ACTIVE, captor.getValue().getStatus());
      }
    }

    @Nested
    class WhenImportingInactiveExternalUser {

      private ExternalUser result;

      @BeforeEach
      void setupWhen() {
        BddLogger.when("importing an inactive external user");

        result =
            service.importExternalUser(
                EPPN,
                FIRST_NAME,
                LAST_NAME,
                EMAIL,
                CATEGORY,
                EXTERNAL_ID,
                SOURCE,
                INSTITUTION_ID,
                GROUP_ID,
                EUserStatus.INACTIVE);
      }

      @Test
      void thenItShouldKeepInactiveStatus() {
        BddLogger.then("it should keep inactive status");

        assertEquals(EUserStatus.INACTIVE, result.getStatus());

        ArgumentCaptor<ExternalUser> captor = ArgumentCaptor.forClass(ExternalUser.class);

        verify(externalUserRepository).save(captor.capture());
        verifyNoMoreInteractions(externalUserRepository);

        assertEquals(EUserStatus.INACTIVE, captor.getValue().getStatus());
      }
    }

    @Nested
    class WhenGettingAllExternalUsers {

      private ExternalUser externalUser;
      private List<ExternalUser> result;

      @BeforeEach
      void setupWhen() {
        BddLogger.when("getting all external users");

        externalUser = externalUser();

        when(externalUserRepository.findAll()).thenReturn(List.of(externalUser));

        result = service.getAllExternalUsers();
      }

      @Test
      void thenItShouldReturnAllExternalUsers() {
        BddLogger.then("it should return all external users");

        assertEquals(List.of(externalUser), result);

        verify(externalUserRepository).findAll();
        verifyNoMoreInteractions(externalUserRepository);
      }
    }

    @Nested
    class WhenGettingExternalUserById {

      @Nested
      class AndExternalUserExists {

        private ExternalUser externalUser;
        private Optional<ExternalUser> result;

        @BeforeEach
        void setupWhen() {
          BddLogger.when("getting external user by id");
          BddLogger.and("external user exists");

          externalUser = externalUser();

          when(externalUserRepository.findById(EXTERNAL_USER_ID))
              .thenReturn(Optional.of(externalUser));

          result = service.getById(EXTERNAL_USER_ID);
        }

        @Test
        void thenItShouldReturnExternalUser() {
          BddLogger.then("it should return external user");

          assertTrue(result.isPresent());
          assertEquals(externalUser, result.orElseThrow());

          verify(externalUserRepository).findById(EXTERNAL_USER_ID);
          verifyNoMoreInteractions(externalUserRepository);
        }
      }

      @Nested
      class AndExternalUserDoesNotExist {

        private Optional<ExternalUser> result;

        @BeforeEach
        void setupWhen() {
          BddLogger.when("getting external user by id");
          BddLogger.and("external user does not exist");

          when(externalUserRepository.findById(EXTERNAL_USER_ID)).thenReturn(Optional.empty());

          result = service.getById(EXTERNAL_USER_ID);
        }

        @Test
        void thenItShouldReturnEmpty() {
          BddLogger.then("it should return empty");

          assertTrue(result.isEmpty());

          verify(externalUserRepository).findById(EXTERNAL_USER_ID);
          verifyNoMoreInteractions(externalUserRepository);
        }
      }
    }

    @Nested
    class WhenGettingExternalUserByEppn {

      @Nested
      class AndExternalUserExists {

        private ExternalUser externalUser;
        private Optional<ExternalUser> result;

        @BeforeEach
        void setupWhen() {
          BddLogger.when("getting external user by eppn");
          BddLogger.and("external user exists");

          externalUser = externalUser();

          when(externalUserRepository.findByEppn(EPPN)).thenReturn(Optional.of(externalUser));

          result = service.getByEppn(EPPN);
        }

        @Test
        void thenItShouldReturnExternalUser() {
          BddLogger.then("it should return external user");

          assertTrue(result.isPresent());
          assertEquals(externalUser, result.orElseThrow());

          verify(externalUserRepository).findByEppn(EPPN);
          verifyNoMoreInteractions(externalUserRepository);
        }
      }

      @Nested
      class AndExternalUserDoesNotExist {

        private Optional<ExternalUser> result;

        @BeforeEach
        void setupWhen() {
          BddLogger.when("getting external user by eppn");
          BddLogger.and("external user does not exist");

          when(externalUserRepository.findByEppn(UNKNOWN_EPPN)).thenReturn(Optional.empty());

          result = service.getByEppn(UNKNOWN_EPPN);
        }

        @Test
        void thenItShouldReturnEmpty() {
          BddLogger.then("it should return empty");

          assertTrue(result.isEmpty());

          verify(externalUserRepository).findByEppn(UNKNOWN_EPPN);
          verifyNoMoreInteractions(externalUserRepository);
        }
      }
    }

    @Nested
    class WhenRepositoryFails {

      private RuntimeException exception;

      @BeforeEach
      void setupWhen() {
        BddLogger.when("repository fails while saving external user");

        exception = new RuntimeException("Unable to save external user");

        doThrow(exception).when(externalUserRepository).save(any(ExternalUser.class));
      }

      @Test
      void thenItShouldPropagateException() {
        BddLogger.then("it should propagate exception");

        RuntimeException result =
            assertThrows(
                RuntimeException.class,
                () ->
                    service.importExternalUser(
                        EPPN,
                        FIRST_NAME,
                        LAST_NAME,
                        EMAIL,
                        CATEGORY,
                        EXTERNAL_ID,
                        SOURCE,
                        INSTITUTION_ID,
                        GROUP_ID,
                        EUserStatus.ACTIVE));

        assertEquals(exception, result);

        verify(externalUserRepository).save(any(ExternalUser.class));
        verifyNoMoreInteractions(externalUserRepository);
      }
    }
  }

  private ExternalUser externalUser() {
    return ExternalUser.toDomain(
        EXTERNAL_USER_ID,
        Instant.parse("2026-01-01T00:00:00Z"),
        Instant.parse("2026-01-01T00:00:00Z"),
        EPPN,
        EXTERNAL_ID,
        SOURCE,
        CATEGORY,
        EMAIL,
        FIRST_NAME,
        LAST_NAME,
        mock(Institution.class),
        mock(Group.class),
        EUserStatus.ACTIVE);
  }
}
