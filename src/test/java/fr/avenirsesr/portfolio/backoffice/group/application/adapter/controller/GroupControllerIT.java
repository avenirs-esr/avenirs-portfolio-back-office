package fr.avenirsesr.portfolio.backoffice.group.application.adapter.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupData;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.enums.EGroupType;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.AvenirsSecurityHeaders;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class GroupControllerIT extends ContainerConfigurationTest {

  private static final String BASE_PATH = "/back-office/admin/groups";
  private static final String ADMIN_TOKEN_HEADER = "X-ADMIN-TOKEN";
  private static final String SEEDED_PROGRAM_ID_SI_SCO = "10000001";
  private static final String SEEDED_INSTITUTION_HAI = "0350001A";
  private static final LocalDate START_DATE = LocalDate.of(2024, 9, 1);
  private static final LocalDate END_DATE = LocalDate.of(2027, 8, 31);

  private UUID institutionId;

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Value("${security.authentication.api-key}")
  private String apiKeyValue;

  @Value("${admin.token}")
  private String adminTokenValue;

  @BeforeAll
  void setup(
      @Autowired SeederRunner seederRunner,
      @Autowired InstitutionRepository institutionRepository) {
    seederRunner.run();
    institutionId = institutionRepository.findByHai(SEEDED_INSTITUTION_HAI).orElseThrow().getId();
  }

  @Test
  void shouldReturnForbidden_whenAdminTokenIsInvalid() throws Exception {
    BddLogger.given("an invalid admin token");

    BddLogger.when("calling GET /back-office/admin/groups");
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
  void shouldCreateAndReturnGroups_whenValidBatchPayload() throws Exception {
    BddLogger.given("a batch of new programs matching the seeder schema");
    List<GroupData> payload =
        List.of(
            new GroupData(
                "Licence Physique",
                "20000001",
                institutionId,
                "21000001",
                START_DATE,
                END_DATE,
                EGroupType.PROGRAM,
                null),
            new GroupData(
                "Licence Chimie",
                "20000002",
                institutionId,
                "21000002",
                START_DATE,
                END_DATE,
                EGroupType.PROGRAM,
                null));

    BddLogger.when("calling POST /back-office/admin/groups");
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
        .andExpect(jsonPath("$.created[0].idSiSco", is("20000001")))
        .andExpect(jsonPath("$.created[1].idSiSco", is("20000002")));

    BddLogger.then("it should create and return a summary of the groups");
  }

  @Test
  void shouldUpdateExistingGroup_whenCreateAllBatchReusesAnExistingIdSiSco() throws Exception {
    BddLogger.given("a batch reusing the id_si_sco of a program seeded beforehand");
    List<GroupData> payload =
        List.of(
            new GroupData(
                "Licence Informatique - Renamed via import",
                SEEDED_PROGRAM_ID_SI_SCO,
                institutionId,
                "31000001",
                START_DATE,
                END_DATE,
                EGroupType.PROGRAM,
                null));

    BddLogger.when("calling POST /back-office/admin/groups");
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
        .andExpect(jsonPath("$.updated[0].idSiSco", is(SEEDED_PROGRAM_ID_SI_SCO)))
        .andExpect(jsonPath("$.updated[0].name", is("Licence Informatique - Renamed via import")));

    BddLogger.then("it should update the existing program instead of failing");
  }

  @Test
  void shouldReportFailure_whenOneGroupOfTheBatchIsInvalid() throws Exception {
    BddLogger.given("a batch mixing a valid program and a program option without a parent");
    List<GroupData> payload =
        List.of(
            new GroupData(
                "Licence Biologie",
                "20000003",
                institutionId,
                "21000003",
                START_DATE,
                END_DATE,
                EGroupType.PROGRAM,
                null),
            new GroupData(
                "Option sans parent",
                "20000004",
                institutionId,
                "21000004",
                START_DATE,
                END_DATE,
                EGroupType.PROGRAM_OPTION,
                null));

    BddLogger.when("calling POST /back-office/admin/groups");
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
        .andExpect(jsonPath("$.created[0].idSiSco", is("20000003")))
        .andExpect(jsonPath("$.failed[0].idSiSco", is("20000004")));

    BddLogger.then("it should import the valid program and report the other as failed");
  }

  @Test
  void shouldUpdateAndReturnGroups_whenValidBatchPayload() throws Exception {
    BddLogger.given("an update payload targeting a program seeded by id_si_sco");
    List<GroupData> payload =
        List.of(
            new GroupData(
                "Licence Informatique - Renamed",
                SEEDED_PROGRAM_ID_SI_SCO,
                institutionId,
                "31000001",
                START_DATE,
                END_DATE,
                EGroupType.PROGRAM,
                null));

    BddLogger.when("calling PUT /back-office/admin/groups");
    mockMvc
        .perform(
            put(BASE_PATH)
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .header(ADMIN_TOKEN_HEADER, adminTokenValue)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].idSiSco", is(SEEDED_PROGRAM_ID_SI_SCO)))
        .andExpect(jsonPath("$[0].name", is("Licence Informatique - Renamed")));

    BddLogger.then("it should update and return the program");
  }

  @Test
  void shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
    BddLogger.given("a random group id that does not exist");
    UUID unknownId = UUID.randomUUID();

    BddLogger.when("calling GET /back-office/admin/groups/{id}");
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
  void shouldDeleteGroup_whenIdExists() throws Exception {
    BddLogger.given("a newly created program");
    List<GroupData> payload =
        List.of(
            new GroupData(
                "Licence a supprimer",
                "20000005",
                institutionId,
                "21000005",
                START_DATE,
                END_DATE,
                EGroupType.PROGRAM,
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

    BddLogger.when("calling DELETE /back-office/admin/groups/{id}");
    mockMvc
        .perform(
            delete(BASE_PATH + "/" + createdId)
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .header(ADMIN_TOKEN_HEADER, adminTokenValue))
        .andExpect(status().isNoContent());

    BddLogger.then("it should delete the program");
  }
}
