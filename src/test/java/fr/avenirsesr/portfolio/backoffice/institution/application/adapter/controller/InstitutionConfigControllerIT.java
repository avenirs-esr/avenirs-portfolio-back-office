package fr.avenirsesr.portfolio.backoffice.institution.application.adapter.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.AvenirsSecurityHeaders;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class InstitutionConfigControllerIT extends ContainerConfigurationTest {

  private static final String BASE_PATH = "/back-office/config/institution";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Value("${security.authentication.api-key}")
  private String apiKeyValue;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  @Test
  void shouldReturnInstitutionConfig_whenInstitutionIdIsValid() throws Exception {
    BddLogger.given("a valid institutionId from seeder file");
    SeedInstitutionConfig any = readSeedInstitutionConfigs().getFirst();
    UUID institutionId = any.institutionId();

    BddLogger.when("calling GET /back-office/config/institution/{institutionId}");
    mockMvc
        .perform(
            get(BASE_PATH + "/" + institutionId)
                .principal(uuidPrincipal())
                .accept(MediaType.APPLICATION_JSON)
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.apcEnabled", is(any.apcEnabled())))
        .andExpect(jsonPath("$.lifeProjectEnabled", is(any.lifeProjectEnabled())))
        .andExpect(jsonPath("$", notNullValue()));

    BddLogger.then("it should return the mapped DTO");
  }

  @Test
  void shouldReturnBadRequest_whenInstitutionIdIsNotAUUID() throws Exception {
    BddLogger.given("an invalid institutionId path variable");
    String invalidId = "not-a-uuid";

    BddLogger.when("calling GET with invalid UUID");
    mockMvc
        .perform(
            get(BASE_PATH + "/" + invalidId)
                .principal(uuidPrincipal())
                .accept(MediaType.APPLICATION_JSON)
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    BddLogger.then("Spring should fail UUID conversion and return 400");
  }

  @Test
  void shouldReturnNotFound_whenConfigDoesNotExist() throws Exception {
    BddLogger.given("a valid UUID that has no config");
    UUID institutionId = UUID.randomUUID();

    BddLogger.when("calling GET for an unknown institutionId");
    mockMvc
        .perform(
            get(BASE_PATH + "/" + institutionId)
                .principal(uuidPrincipal())
                .accept(MediaType.APPLICATION_JSON)
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    BddLogger.then("it should return 404 (depending on your exception handler)");
  }

  private List<SeedInstitutionConfig> readSeedInstitutionConfigs() throws Exception {
    InputStream is =
        InstitutionConfigControllerIT.class
            .getClassLoader()
            .getResourceAsStream("seeder/institution-configs.json");

    if (is == null) {
      throw new IllegalStateException("Cannot find resource: seeder/institution-configs.json");
    }

    return objectMapper.readValue(is, new TypeReference<>() {});
  }

  private record SeedInstitutionConfig(
      UUID institutionId, boolean apcEnabled, boolean lifeProjectEnabled) {}
}
