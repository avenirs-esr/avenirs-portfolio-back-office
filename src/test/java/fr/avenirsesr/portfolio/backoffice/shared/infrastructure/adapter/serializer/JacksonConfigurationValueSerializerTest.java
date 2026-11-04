package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.serializer;

import static org.junit.jupiter.api.Assertions.*;

import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.AlertConfiguration;
import fr.avenirsesr.portfolio.common.configuration.domain.exception.ConfigurationException;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class JacksonConfigurationValueSerializerTest {
  private final JacksonConfigurationValueSerializer serializer =
      new JacksonConfigurationValueSerializer();

  @Test
  void shouldWriteDatesAsIsoStrings() {
    BddLogger.given("a scheduled alert");
    var alert =
        new AlertConfiguration(
            "Maintenance",
            null,
            Instant.parse("2026-11-10T08:00:00Z"),
            Instant.parse("2026-11-10T12:30:00Z"));

    BddLogger.when("serializing it");
    var json = serializer.serialize(alert);

    BddLogger.then("it is a JSON object with ISO-8601 dates");
    assertEquals(
        "{\"message\":\"Maintenance\",\"active\":null,"
            + "\"startDate\":\"2026-11-10T08:00:00Z\",\"endDate\":\"2026-11-10T12:30:00Z\"}",
        json);
  }

  @Test
  void shouldReadBackWhatItWrote() {
    BddLogger.given("a serialized alert");
    var alert =
        new AlertConfiguration(
            "Maintenance",
            null,
            Instant.parse("2026-11-10T08:00:00Z"),
            Instant.parse("2026-11-10T12:30:00Z"));
    var json = serializer.serialize(alert);

    BddLogger.when("deserializing it");
    var result = serializer.deserialize(json, AlertConfiguration.class);

    BddLogger.then("the same alert is returned");
    assertEquals(alert, result);
  }

  @Test
  void shouldIgnoreUnknownFields() {
    BddLogger.given("an alert saved with a field this version does not know");
    var json = "{\"message\":\"Maintenance\",\"active\":false,\"severity\":\"HIGH\"}";

    BddLogger.when("deserializing it");
    var result = serializer.deserialize(json, AlertConfiguration.class);

    BddLogger.then("the known fields are read");
    assertEquals(new AlertConfiguration("Maintenance", false, null, null), result);
  }

  @Test
  void shouldRaiseConfigurationErrorOnInvalidStoredValue() {
    BddLogger.given("a stored alert breaking its own rules");
    var json = "{\"message\":\"\",\"active\":true}";

    BddLogger.when("deserializing it");
    BddLogger.then("a configuration error is raised");
    assertThrows(
        ConfigurationException.class, () -> serializer.deserialize(json, AlertConfiguration.class));
  }

  @Test
  void shouldRaiseConfigurationErrorOnMalformedJson() {
    BddLogger.given("a malformed JSON document");

    BddLogger.when("deserializing it");
    BddLogger.then("a configuration error is raised");
    assertThrows(
        ConfigurationException.class,
        () -> serializer.deserialize("{\"message\":", AlertConfiguration.class));
  }
}
