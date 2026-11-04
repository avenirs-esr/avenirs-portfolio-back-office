package fr.avenirsesr.portfolio.backoffice.websitecontent.application.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.Configuration;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationScope;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.output.repository.ConfigurationRepository;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.AlertConfiguration;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.EWebsiteContentConfiguration;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.port.input.WebsiteContentConfigurationService;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.AvenirsSecurityHeaders;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.time.Instant;
import java.util.List;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

class WebsiteContentControllerIT extends ContainerConfigurationTest {
  private static final String ALERT_SETUP_PATH = "/back-office/config/website-content/setup/alert";
  private static final String ALERT_MESSAGE_PATH =
      "/back-office/config/website-content/alert/message";
  private static final String SCHEDULED_ALERT =
      """
      {"message":"Maintenance",\
      "startDate":"2026-11-10T08:00:00Z","endDate":"2026-11-10T12:00:00Z"}\
      """;

  @Autowired private MockMvc mockMvc;
  @Autowired private ConfigurationRepository configurationRepository;
  @Autowired private WebsiteContentConfigurationService websiteContentConfigurationService;
  @Autowired private ObjectMapper objectMapper;

  @Value("${security.authentication.api-key}")
  private String apiKeyValue;

  private MockHttpServletRequestBuilder putAlert(String body) {
    return put(ALERT_SETUP_PATH)
        .principal(uuidPrincipal())
        .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body);
  }

  private List<Configuration> storedAlerts() {
    return configurationRepository.inScope(EConfigurationScope.WEBSITE_CONTENT).stream()
        .filter(c -> c.getKey() == EWebsiteContentConfiguration.ALERT)
        .toList();
  }

  @Test
  void shouldSaveTheAlertAndReturnIt() throws Exception {
    BddLogger.given("a scheduled alert");

    BddLogger.when("putting it with the website-content-config:update authority");
    BddLogger.then("200 OK is returned with the saved alert");
    mockMvc
        .perform(putAlert(SCHEDULED_ALERT))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", is("Maintenance")))
        .andExpect(jsonPath("$.active", nullValue()))
        .andExpect(jsonPath("$.startDate", is("2026-11-10T08:00:00Z")))
        .andExpect(jsonPath("$.endDate", is("2026-11-10T12:00:00Z")));

    BddLogger.and("the alert is stored as the ALERT configuration");
    var stored = storedAlerts();
    assertEquals(1, stored.size());
    assertEquals(
        objectMapper.readTree(
            "{\"message\":\"Maintenance\",\"active\":null,"
                + "\"startDate\":\"2026-11-10T08:00:00Z\",\"endDate\":\"2026-11-10T12:00:00Z\"}"),
        objectMapper.readTree(stored.getFirst().getValue()));
  }

  @Test
  void shouldReplaceTheExistingAlert() throws Exception {
    BddLogger.given("an alert already saved");
    mockMvc.perform(putAlert(SCHEDULED_ALERT)).andExpect(status().isOk());

    BddLogger.when("putting another alert");
    BddLogger.then("200 OK is returned with the new alert");
    mockMvc
        .perform(putAlert("{\"message\":\"Nouvelle alerte\",\"active\":false}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", is("Nouvelle alerte")))
        .andExpect(jsonPath("$.active", is(false)))
        .andExpect(jsonPath("$.startDate", nullValue()))
        .andExpect(jsonPath("$.endDate", nullValue()));

    BddLogger.and("it replaces the previous one");
    var stored = storedAlerts();
    assertEquals(1, stored.size());
    assertEquals(
        "Nouvelle alerte",
        objectMapper.readTree(stored.getFirst().getValue()).get("message").asText());
  }

  @Test
  void shouldRejectAStartDateWithoutEndDate() throws Exception {
    BddLogger.given("an alert with a start date but no end date");
    var body = "{\"message\":\"Maintenance\",\"startDate\":\"2026-11-10T08:00:00Z\"}";

    BddLogger.when("putting it");
    BddLogger.then("400 BAD REQUEST is returned on the end date");
    mockMvc
        .perform(putAlert(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("NOT_NULL")))
        .andExpect(jsonPath("$.message", is("The field endDate cannot be null")));

    BddLogger.and("nothing is stored");
    assertTrue(storedAlerts().isEmpty());
  }

  @Test
  void shouldRejectAnAlertWithoutMessage() throws Exception {
    BddLogger.given("an alert without message");

    BddLogger.when("putting it");
    BddLogger.then("400 BAD REQUEST is returned on the message");
    mockMvc
        .perform(putAlert("{\"active\":true}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("NOT_BLANK")));
  }

  @Test
  void shouldRejectAnAlertEndingBeforeItStarts() throws Exception {
    BddLogger.given("an alert ending before it starts");
    var body =
        "{\"message\":\"Maintenance\","
            + "\"startDate\":\"2026-11-10T12:00:00Z\",\"endDate\":\"2026-11-10T08:00:00Z\"}";

    BddLogger.when("putting it");
    BddLogger.then("400 BAD REQUEST is returned");
    mockMvc
        .perform(putAlert(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("END_DATE_BEFORE_START_DATE")));
  }

  @Test
  void shouldRejectAnAlertNeitherActivatedNorScheduled() throws Exception {
    BddLogger.given("an alert without active flag nor period");

    BddLogger.when("putting it");
    BddLogger.then("400 BAD REQUEST is returned on the active flag");
    mockMvc
        .perform(putAlert("{\"message\":\"Maintenance\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("NOT_NULL")))
        .andExpect(jsonPath("$.message", is("The field active cannot be null")));
  }

  @Test
  void shouldRejectAnAlertBothActivatedAndScheduled() throws Exception {
    BddLogger.given("an alert with an active flag and a period");
    var body =
        "{\"message\":\"Maintenance\",\"active\":true,"
            + "\"startDate\":\"2026-11-10T08:00:00Z\",\"endDate\":\"2026-11-10T12:00:00Z\"}";

    BddLogger.when("putting it");
    BddLogger.then("400 BAD REQUEST is returned and nothing is stored");
    mockMvc
        .perform(putAlert(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code", is("INVALID_ARGUMENT_TYPE")))
        .andExpect(
            jsonPath(
                "$.message", is("An alert is either activated manually or scheduled, not both")));
    assertTrue(storedAlerts().isEmpty());
  }

  @Test
  void shouldRejectAnUpdateWithoutTheApiKey() throws Exception {
    BddLogger.given("a scheduled alert");

    BddLogger.when("putting it without the api key");
    BddLogger.then("401 UNAUTHORIZED is returned and nothing is stored");
    mockMvc
        .perform(
            put(ALERT_SETUP_PATH)
                .principal(uuidPrincipal())
                .contentType(MediaType.APPLICATION_JSON)
                .content(SCHEDULED_ALERT))
        .andExpect(status().isUnauthorized());
    assertTrue(storedAlerts().isEmpty());
  }

  @Test
  void shouldReturnAnInactiveAlertWhenNoneIsConfigured() throws Exception {
    BddLogger.given("no alert configured");

    BddLogger.when("getting it with the website-content-config:read authority");
    BddLogger.then("200 OK is returned with an inactive alert");
    mockMvc
        .perform(
            get(ALERT_SETUP_PATH)
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", nullValue()))
        .andExpect(jsonPath("$.active", is(false)))
        .andExpect(jsonPath("$.startDate", nullValue()))
        .andExpect(jsonPath("$.endDate", nullValue()));
  }

  @Test
  void shouldReturnTheSavedAlert() throws Exception {
    BddLogger.given("a scheduled alert saved");
    mockMvc.perform(putAlert(SCHEDULED_ALERT)).andExpect(status().isOk());

    BddLogger.when("getting it with the website-content-config:read authority");
    BddLogger.then("200 OK is returned with the saved alert");
    mockMvc
        .perform(
            get(ALERT_SETUP_PATH)
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", is("Maintenance")))
        .andExpect(jsonPath("$.active", nullValue()))
        .andExpect(jsonPath("$.startDate", is("2026-11-10T08:00:00Z")))
        .andExpect(jsonPath("$.endDate", is("2026-11-10T12:00:00Z")));
  }

  @Test
  void shouldRejectAReadWithoutTheApiKey() throws Exception {
    BddLogger.given("the " + ALERT_SETUP_PATH + " endpoint");

    BddLogger.when("getting the alert without the api key");
    BddLogger.then("401 UNAUTHORIZED is returned");
    mockMvc
        .perform(get(ALERT_SETUP_PATH).principal(uuidPrincipal()))
        .andExpect(status().isUnauthorized());
  }

  private void expectPublicAlertMessage(Matcher<? super String> message) throws Exception {
    mockMvc
        .perform(get(ALERT_MESSAGE_PATH))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", message));
  }

  @Test
  void shouldReturnNoMessageWithoutAuthenticationWhenNoAlertIsConfigured() throws Exception {
    BddLogger.given("no alert configured");

    BddLogger.when("reading " + ALERT_MESSAGE_PATH + " without any credentials");
    BddLogger.then("200 OK is returned without message");
    expectPublicAlertMessage(nullValue());
  }

  @Test
  void shouldReturnTheMessageOfAnActiveAlertWithoutPeriod() throws Exception {
    BddLogger.given("an active alert without period");
    websiteContentConfigurationService.putAlertConfiguration(
        new AlertConfiguration("Maintenance", true, null, null));

    BddLogger.when("reading " + ALERT_MESSAGE_PATH + " without any credentials");
    BddLogger.then("its message is returned");
    expectPublicAlertMessage(is("Maintenance"));
  }

  @Test
  void shouldReturnNoMessageForAnInactiveAlertWithoutPeriod() throws Exception {
    BddLogger.given("an inactive alert without period");
    websiteContentConfigurationService.putAlertConfiguration(
        new AlertConfiguration("Maintenance", false, null, null));

    BddLogger.when("reading " + ALERT_MESSAGE_PATH + " without any credentials");
    BddLogger.then("no message is returned");
    expectPublicAlertMessage(nullValue());
  }

  @Test
  void shouldReturnTheMessageOfAnAlertScheduledAroundNow() throws Exception {
    BddLogger.given("an alert scheduled around now");
    var now = Instant.now();
    websiteContentConfigurationService.putAlertConfiguration(
        new AlertConfiguration("Maintenance", null, now.minusSeconds(3600), now.plusSeconds(3600)));

    BddLogger.when("reading " + ALERT_MESSAGE_PATH + " without any credentials");
    BddLogger.then("its message is returned");
    expectPublicAlertMessage(is("Maintenance"));
  }

  @Test
  void shouldReturnNoMessageForAnAlertWhosePeriodIsOver() throws Exception {
    BddLogger.given("an alert whose scheduled period is over");
    var now = Instant.now();
    websiteContentConfigurationService.putAlertConfiguration(
        new AlertConfiguration(
            "Maintenance", null, now.minusSeconds(7200), now.minusSeconds(3600)));

    BddLogger.when("reading " + ALERT_MESSAGE_PATH + " without any credentials");
    BddLogger.then("no message is returned");
    expectPublicAlertMessage(nullValue());
  }
}
