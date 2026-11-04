package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.repository;

import static org.junit.jupiter.api.Assertions.*;

import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationScope;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.model.ConfigurationEntity;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.backoffice.trace.domain.port.input.TraceConfigurationService;
import fr.avenirsesr.portfolio.common.configuration.domain.model.EConfiguration;
import fr.avenirsesr.portfolio.common.configuration.domain.model.ETraceConfiguration;
import fr.avenirsesr.portfolio.common.configuration.domain.model.TraceConfiguration;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ConfigurationDatabaseRepositoryIT extends ContainerConfigurationTest {
  @Autowired private ConfigurationDatabaseRepository configurationDatabaseRepository;
  @Autowired private TraceConfigurationService traceConfigurationService;
  @Autowired private EntityManager entityManager;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

  private ConfigurationEntity storedEntity(EConfigurationScope scope, EConfiguration key) {
    return configurationDatabaseRepository.inScopeEntities(scope).stream()
        .filter(e -> e.getKey().equals(key.name()))
        .findAny()
        .orElseThrow();
  }

  @Test
  void shouldStoreTextConfigurationAsJsonStringAndReadItBack() {
    BddLogger.given("a trace configuration");
    var traceConfiguration = new TraceConfiguration(30, 10, 3);

    BddLogger.when("saving it and reading it back from the database");
    traceConfigurationService.postTraceConfiguration(traceConfiguration);
    flushAndClear();

    BddLogger.then("each value is stored as a JSON string and read back as before");
    assertEquals(
        "\"30\"",
        storedEntity(EConfigurationScope.TRACE, ETraceConfiguration.MAX_REMINING_DAYS)
            .getValue()
            .orElseThrow());
    assertEquals(traceConfiguration, traceConfigurationService.getTraceConfiguration());
  }
}
