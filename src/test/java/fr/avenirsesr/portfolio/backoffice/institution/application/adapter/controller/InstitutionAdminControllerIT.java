package fr.avenirsesr.portfolio.backoffice.institution.application.adapter.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionData;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.AvenirsSecurityHeaders;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class InstitutionAdminControllerIT extends ContainerConfigurationTest {

  private static final String BASE_PATH = "/back-office/admin/institutions";
  private static final String ADMIN_TOKEN_HEADER = "X-ADMIN-TOKEN";
  private static final String SEEDED_PRIMARY_HAI = "0350001A";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Value("${security.authentication.api-key}")
  private String apiKeyValue;

  @Value("${admin.token}")
  private String adminTokenValue;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  @Test
  void shouldReturnForbidden_whenAdminTokenIsInvalid() throws Exception {
    BddLogger.given("an invalid admin token");

    BddLogger.when("calling GET /back-office/admin/institutions");
    mockMvc
        .perform(
            get(BASE_PATH)
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .header(ADMIN_TOKEN_HEADER, "wrong-token")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    BddLogger.then("it should return 403");
  }

  @Test
  void shouldCreateAndReturnInstitutions_whenValidBatchPayload() throws Exception {
    BddLogger.given("a batch of new institutions matching the seeder schema");
    List<InstitutionData> payload =
        List.of(
            new InstitutionData(
                "Université de Nantes",
                "0440001A",
                "13000552300010",
                "130005523",
                EInstitutionType.PRIMARY,
                null),
            new InstitutionData(
                "Université de Lyon",
                "0690001A",
                "13000552400018",
                "130005524",
                EInstitutionType.PRIMARY,
                null));

    BddLogger.when("calling POST /back-office/admin/institutions");
    mockMvc
        .perform(
            post(BASE_PATH)
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .header(ADMIN_TOKEN_HEADER, adminTokenValue)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.createdCount", is(2)))
        .andExpect(jsonPath("$.updatedCount", is(0)))
        .andExpect(jsonPath("$.failedCount", is(0)))
        .andExpect(jsonPath("$.created", hasSize(2)))
        .andExpect(jsonPath("$.created[0].hai", is("0440001A")))
        .andExpect(jsonPath("$.created[1].hai", is("0690001A")));

    BddLogger.then("it should create and return a summary of the institutions");
  }

  @Test
  void shouldUpdateExistingInstitution_whenCreateAllBatchReusesAnExistingHai() throws Exception {
    BddLogger.given("a batch reusing the hai of an institution seeded beforehand");
    List<InstitutionData> payload =
        List.of(
            new InstitutionData(
                "Université de Rennes - Renamed via import",
                SEEDED_PRIMARY_HAI,
                "13000550100015",
                "130005501",
                EInstitutionType.PRIMARY,
                null));

    BddLogger.when("calling POST /back-office/admin/institutions");
    mockMvc
        .perform(
            post(BASE_PATH)
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .header(ADMIN_TOKEN_HEADER, adminTokenValue)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.createdCount", is(0)))
        .andExpect(jsonPath("$.updatedCount", is(1)))
        .andExpect(jsonPath("$.failedCount", is(0)))
        .andExpect(jsonPath("$.updated[0].hai", is(SEEDED_PRIMARY_HAI)))
        .andExpect(jsonPath("$.updated[0].name", is("Université de Rennes - Renamed via import")));

    BddLogger.then("it should update the existing institution instead of failing");
  }

  @Test
  void shouldReportFailure_whenOneInstitutionOfTheBatchIsInvalid() throws Exception {
    BddLogger.given("a batch mixing a valid institution and a secondary one without a parent");
    List<InstitutionData> payload =
        List.of(
            new InstitutionData(
                "Université de Toulouse",
                "0310001A",
                "13000552600014",
                "130005526",
                EInstitutionType.PRIMARY,
                null),
            new InstitutionData(
                "IUT sans parent",
                "0310002B",
                "13000552700012",
                "130005527",
                EInstitutionType.SECONDARY,
                null));

    BddLogger.when("calling POST /back-office/admin/institutions");
    mockMvc
        .perform(
            post(BASE_PATH)
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .header(ADMIN_TOKEN_HEADER, adminTokenValue)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.createdCount", is(1)))
        .andExpect(jsonPath("$.updatedCount", is(0)))
        .andExpect(jsonPath("$.failedCount", is(1)))
        .andExpect(jsonPath("$.created[0].hai", is("0310001A")))
        .andExpect(jsonPath("$.failed[0].hai", is("0310002B")));

    BddLogger.then("it should import the valid institution and report the other as failed");
  }

  @Test
  void shouldUpdateAndReturnInstitutions_whenValidBatchPayload() throws Exception {
    BddLogger.given("an update payload targeting an institution seeded by hai");
    List<InstitutionData> payload =
        List.of(
            new InstitutionData(
                "Université de Rennes - Renamed",
                SEEDED_PRIMARY_HAI,
                "13000550100015",
                "130005501",
                EInstitutionType.PRIMARY,
                null));

    BddLogger.when("calling PUT /back-office/admin/institutions");
    mockMvc
        .perform(
            put(BASE_PATH)
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .header(ADMIN_TOKEN_HEADER, adminTokenValue)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].hai", is(SEEDED_PRIMARY_HAI)))
        .andExpect(jsonPath("$[0].name", is("Université de Rennes - Renamed")));

    BddLogger.then("it should update and return the institution");
  }

  @Test
  void shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
    BddLogger.given("a random institution id that does not exist");
    UUID unknownId = UUID.randomUUID();

    BddLogger.when("calling GET /back-office/admin/institutions/{id}");
    mockMvc
        .perform(
            get(BASE_PATH + "/" + unknownId)
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .header(ADMIN_TOKEN_HEADER, adminTokenValue)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    BddLogger.then("it should return 404");
  }

  @Test
  void shouldDeleteInstitution_whenIdExists() throws Exception {
    BddLogger.given("a newly created institution");
    List<InstitutionData> payload =
        List.of(
            new InstitutionData(
                "Université à supprimer",
                "0990001A",
                "13000552500017",
                "130005525",
                EInstitutionType.PRIMARY,
                null));

    String response =
        mockMvc
            .perform(
                post(BASE_PATH)
                    .principal(uuidPrincipal())
                    .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                    .header(ADMIN_TOKEN_HEADER, adminTokenValue)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID createdId =
        UUID.fromString(objectMapper.readTree(response).get("created").get(0).get("id").asText());

    BddLogger.when("calling DELETE /back-office/admin/institutions/{id}");
    mockMvc
        .perform(
            delete(BASE_PATH + "/" + createdId)
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .header(ADMIN_TOKEN_HEADER, adminTokenValue))
        .andExpect(status().isNoContent());

    BddLogger.then("it should delete the institution");
  }
}
