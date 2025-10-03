package fr.avenirsesr.portfolio.backoffice.trace.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import fr.avenirsesr.portfolio.backoffice.shared.domain.model.Configuration;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationScope;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.output.repository.ConfigurationRepository;
import fr.avenirsesr.portfolio.common.configuration.domain.exception.ConfigurationException;
import fr.avenirsesr.portfolio.common.configuration.domain.model.ETraceConfiguration;
import fr.avenirsesr.portfolio.common.configuration.domain.model.TraceConfiguration;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TraceConfigurationServiceImplTest {

  @Mock private ConfigurationRepository configurationRepository;

  @InjectMocks private TraceConfigurationServiceImpl service;

  private List<Configuration> mockConfigurations;

  @BeforeEach
  void setUp() {
    mockConfigurations =
        List.of(
            Configuration.create(
                UUID.randomUUID(),
                EConfigurationScope.TRACE,
                ETraceConfiguration.MAX_REMINING_DAYS,
                "30"),
            Configuration.create(
                UUID.randomUUID(),
                EConfigurationScope.TRACE,
                ETraceConfiguration.MAX_REMINING_DAYS_BEFORE_WARNING,
                "10"),
            Configuration.create(
                UUID.randomUUID(),
                EConfigurationScope.TRACE,
                ETraceConfiguration.MAX_REMINING_DAYS_BEFORE_CRITICAL,
                "5"));

    when(configurationRepository.inScope(EConfigurationScope.TRACE)).thenReturn(mockConfigurations);
  }

  @Test
  void shouldReturnTraceConfigurationFromRepository() {
    BddLogger.given("a TraceConfigurationServiceImpl service");
    BddLogger.when("getting the trace configuration");
    TraceConfiguration traceConfiguration = service.getTraceConfiguration();

    BddLogger.then("it should return the trace configuration from repository");
    assertNotNull(traceConfiguration);
    assertEquals(30, traceConfiguration.maxRemainingDays());
    assertEquals(10, traceConfiguration.maxRemainingDaysBeforeWarning());
    assertEquals(5, traceConfiguration.maxRemainingDaysBeforeCritical());
    verify(configurationRepository).inScope(EConfigurationScope.TRACE);
  }

  @Test
  void shouldThrowExceptionIfConfigurationMissing() {
    BddLogger.given(
        "a TraceConfigurationServiceImpl service and a repository missing CRITICAL key");
    when(configurationRepository.inScope(EConfigurationScope.TRACE))
        .thenReturn(
            List.of(
                Configuration.create(
                    UUID.randomUUID(),
                    EConfigurationScope.TRACE,
                    ETraceConfiguration.MAX_REMINING_DAYS,
                    "30"),
                Configuration.create(
                    UUID.randomUUID(),
                    EConfigurationScope.TRACE,
                    ETraceConfiguration.MAX_REMINING_DAYS_BEFORE_WARNING,
                    "10")));

    BddLogger.when("getting the trace configuration");
    BddLogger.then("it should throw ConfigurationException");
    assertThrows(ConfigurationException.class, () -> service.getTraceConfiguration());
  }

  @Test
  void shouldSaveNewTraceConfigurationValues() {
    BddLogger.given("a TraceConfigurationServiceImpl service");
    TraceConfiguration newConfig = new TraceConfiguration(60, 20, 8);

    BddLogger.when("posting a new trace configuration");
    service.postTraceConfiguration(newConfig);

    BddLogger.then("it should save the new trace configuration values");
    verify(configurationRepository).saveAll(anyList());
  }

  @Test
  void shouldUpdateExistingTraceConfigurationValues() {
    BddLogger.given("a TraceConfigurationServiceImpl service");
    TraceConfiguration updatedConfig = new TraceConfiguration(40, 15, 7);

    BddLogger.when("posting an existing trace configuration with new values");
    service.postTraceConfiguration(updatedConfig);

    BddLogger.then("it should update the new trace configuration values");
    verify(configurationRepository).saveAll(anyList());
  }
}
