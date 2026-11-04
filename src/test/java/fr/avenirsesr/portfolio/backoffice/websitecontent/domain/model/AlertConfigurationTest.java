package fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.exception.AlertActivationModeConflictException;
import fr.avenirsesr.portfolio.common.error.domain.exception.FieldValidationException;
import fr.avenirsesr.portfolio.common.error.domain.model.enums.EErrorCode;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AlertConfigurationTest {
  private static final Instant START = Instant.parse("2026-11-10T08:00:00Z");
  private static final Instant END = Instant.parse("2026-11-10T12:00:00Z");

  @Test
  void shouldAcceptManualAlert() {
    BddLogger.given("alerts activated or deactivated manually");
    BddLogger.when("creating them");
    BddLogger.then("they are accepted");
    assertDoesNotThrow(() -> new AlertConfiguration("Maintenance", true, null, null));
    assertDoesNotThrow(() -> new AlertConfiguration("Maintenance", false, null, null));
  }

  @Test
  void shouldAcceptScheduledAlert() {
    BddLogger.given("an alert scheduled over a period");
    BddLogger.when("creating it");
    var alert = new AlertConfiguration("Maintenance", null, START, END);

    BddLogger.then("it holds its values");
    assertEquals("Maintenance", alert.message());
    assertNull(alert.active());
    assertEquals(START, alert.startDate());
    assertEquals(END, alert.endDate());
  }

  @Test
  void shouldRejectAlertWithoutMessage() {
    BddLogger.given("manual and scheduled alerts without message");
    BddLogger.when("creating them");
    BddLogger.then("they are rejected as a blank message");
    var exception =
        assertThrows(
            FieldValidationException.class, () -> new AlertConfiguration(null, true, null, null));
    assertThrows(
        FieldValidationException.class, () -> new AlertConfiguration(" ", false, null, null));
    assertThrows(
        FieldValidationException.class, () -> new AlertConfiguration(null, null, START, END));
    assertEquals(EErrorCode.NOT_BLANK, exception.getErrorCode());
    assertEquals("The field message cannot be blank", exception.getMessage());
  }

  @Test
  void shouldRejectAlertNeitherManualNorScheduled() {
    BddLogger.given("an alert without active flag nor period");
    BddLogger.when("creating it");
    BddLogger.then("it is rejected as a missing active flag");
    var exception =
        assertThrows(
            FieldValidationException.class,
            () -> new AlertConfiguration("Maintenance", null, null, null));
    assertEquals(EErrorCode.NOT_NULL, exception.getErrorCode());
    assertEquals("The field active cannot be null", exception.getMessage());
  }

  @Test
  void shouldRejectAlertBothManualAndScheduled() {
    BddLogger.given("alerts with an active flag and a period");
    BddLogger.when("creating them");
    BddLogger.then("they are rejected");
    assertThrows(
        AlertActivationModeConflictException.class,
        () -> new AlertConfiguration("Maintenance", true, START, END));
    assertThrows(
        AlertActivationModeConflictException.class,
        () -> new AlertConfiguration("Maintenance", false, START, END));
    assertThrows(
        AlertActivationModeConflictException.class,
        () -> new AlertConfiguration("Maintenance", true, START, null));
  }

  @Test
  void shouldRejectIncompletePeriod() {
    BddLogger.given("scheduled alerts missing one end of their period");
    BddLogger.when("creating them");
    BddLogger.then("they are rejected on the missing date");
    var missingEnd =
        assertThrows(
            FieldValidationException.class,
            () -> new AlertConfiguration("Maintenance", null, START, null));
    var missingStart =
        assertThrows(
            FieldValidationException.class,
            () -> new AlertConfiguration("Maintenance", null, null, END));
    assertEquals("The field endDate cannot be null", missingEnd.getMessage());
    assertEquals("The field startDate cannot be null", missingStart.getMessage());
  }

  @Test
  void shouldRejectPeriodNotEndingAfterItStarts() {
    BddLogger.given("alerts ending before or when they start");
    BddLogger.when("creating them");
    BddLogger.then("they are rejected");
    var exception =
        assertThrows(
            FieldValidationException.class,
            () -> new AlertConfiguration("Maintenance", null, END, START));
    assertThrows(
        FieldValidationException.class,
        () -> new AlertConfiguration("Maintenance", null, START, START));
    assertEquals(EErrorCode.END_DATE_BEFORE_START_DATE, exception.getErrorCode());
  }
}
