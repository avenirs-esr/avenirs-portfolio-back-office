package fr.avenirsesr.portfolio.backoffice.institution.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionIdNullException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionConfig;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionConfigRepository;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InstitutionConfigServiceImplTest {

  @Mock private InstitutionConfigRepository institutionConfigRepository;

  @InjectMocks private InstitutionConfigServiceImpl service;

  @Test
  void shouldThrowInstitutionIdNullException_whenGettingInstitutionConfig_withNullId() {
    BddLogger.given("an InstitutionConfigServiceImpl service");
    BddLogger.when("getting an institution config with a null institutionId");

    assertThrows(InstitutionIdNullException.class, () -> service.getInstitutionConfig(null));

    BddLogger.then("it should not call the repository");
    verifyNoInteractions(institutionConfigRepository);
  }

  @Test
  void shouldReturnInstitutionConfigFromRepository_whenGettingInstitutionConfig() {
    BddLogger.given("an InstitutionConfigServiceImpl service");
    UUID institutionId = UUID.randomUUID();
    InstitutionConfig expected = mock(InstitutionConfig.class);

    when(institutionConfigRepository.findByInstitutionId(institutionId)).thenReturn(expected);

    BddLogger.when("getting an institution config with a valid institutionId");
    InstitutionConfig result = service.getInstitutionConfig(institutionId);

    BddLogger.then("it should return the config returned by the repository");
    assertNotNull(result);
    assertSame(expected, result);
    verify(institutionConfigRepository).findByInstitutionId(institutionId);
    verifyNoMoreInteractions(institutionConfigRepository);
  }

  @Test
  void shouldCreateAndSaveInstitutionConfig_whenCreating() {
    BddLogger.given("an InstitutionConfigServiceImpl service");
    UUID institutionId = UUID.randomUUID();
    boolean apcEnabled = true;
    boolean programEnabled = false;

    when(institutionConfigRepository.save(any(InstitutionConfig.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("creating an institution config");
    InstitutionConfig saved = service.create(institutionId, apcEnabled, programEnabled);

    BddLogger.then("it should save and return the created config");
    assertNotNull(saved);
    verify(institutionConfigRepository).save(any(InstitutionConfig.class));
    verifyNoMoreInteractions(institutionConfigRepository);
  }

  @Test
  void shouldReturnRepositoryResult_whenCreating() {
    BddLogger.given("an InstitutionConfigServiceImpl service");
    UUID institutionId = UUID.randomUUID();

    InstitutionConfig repositoryReturned = mock(InstitutionConfig.class);
    when(institutionConfigRepository.save(any(InstitutionConfig.class)))
        .thenReturn(repositoryReturned);

    BddLogger.when("creating an institution config");
    InstitutionConfig result = service.create(institutionId, true, true);

    BddLogger.then("it should return what repository returns");
    assertSame(repositoryReturned, result);
    verify(institutionConfigRepository).save(any(InstitutionConfig.class));
    verifyNoMoreInteractions(institutionConfigRepository);
  }
}
