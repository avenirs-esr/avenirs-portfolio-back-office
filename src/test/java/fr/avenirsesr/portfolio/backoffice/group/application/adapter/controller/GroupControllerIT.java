package fr.avenirsesr.portfolio.backoffice.group.application.adapter.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.AvenirsSecurityHeaders;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class GroupControllerIT extends ContainerConfigurationTest {

  private static final String BASE_PATH = "/back-office/groups";
  private static final String SEEDED_PROGRAM_ID_SI_SCO = "10000001";
  private static final String SEEDED_PROGRAM_OPTION_ID_SI_SCO = "10000002";
  private static final String SEEDED_STUDENT_GROUP_ID_SI_SCO = "10000003";

  @Autowired private MockMvc mockMvc;
  @Autowired private GroupRepository groupRepository;

  @Value("${security.authentication.api-key}")
  private String apiKeyValue;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  private UUID groupIdOf(String idSiSco) {
    return groupRepository.findByIdSiSco(idSiSco).orElseThrow().getId();
  }

  @Test
  void shouldReturnGroupWithItsParentId_whenGroupHasAParent() throws Exception {
    BddLogger.given("a seeded program option attached to a program");
    UUID programOptionId = groupIdOf(SEEDED_PROGRAM_OPTION_ID_SI_SCO);
    UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);

    BddLogger.when("calling GET /back-office/groups/{id} with the api key only");
    mockMvc
        .perform(
            get(BASE_PATH + "/" + programOptionId)
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(programOptionId.toString())))
        .andExpect(jsonPath("$.name", is("Licence Informatique - Parcours IA")))
        .andExpect(jsonPath("$.type", is("PROGRAM_OPTION")))
        .andExpect(jsonPath("$.parentId", is(programId.toString())));

    BddLogger.then("it should return the group name, type and parent id");
  }

  @Test
  void shouldReturnGroupWithoutParentId_whenGroupIsARootProgram() throws Exception {
    BddLogger.given("a seeded program without parent");
    UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);

    BddLogger.when("calling GET /back-office/groups/{id}");
    mockMvc
        .perform(
            get(BASE_PATH + "/" + programId)
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.type", is("PROGRAM")))
        .andExpect(jsonPath("$.parentId", is(nullValue())));

    BddLogger.then("it should return the group with a null parent id");
  }

  @Test
  void shouldReturnProgram_whenWalkingUpFromAStudentGroup() throws Exception {
    BddLogger.given("a seeded student group whose grand-parent is a program");
    UUID studentGroupId = groupIdOf(SEEDED_STUDENT_GROUP_ID_SI_SCO);
    UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);

    BddLogger.when("calling GET /back-office/groups/{id}/program");
    mockMvc
        .perform(
            get(BASE_PATH + "/" + studentGroupId + "/program")
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(programId.toString())))
        .andExpect(jsonPath("$.name", is("Licence Informatique")))
        .andExpect(jsonPath("$.type", is("PROGRAM")))
        .andExpect(jsonPath("$.parentId", is(nullValue())));

    BddLogger.then("it should return the topmost ancestor, which is the program");
  }

  @Test
  void shouldReturnProgram_whenWalkingUpFromAProgramOption() throws Exception {
    BddLogger.given("a seeded program option whose parent is a program");
    UUID programOptionId = groupIdOf(SEEDED_PROGRAM_OPTION_ID_SI_SCO);
    UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);

    BddLogger.when("calling GET /back-office/groups/{id}/program");
    mockMvc
        .perform(
            get(BASE_PATH + "/" + programOptionId + "/program")
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(programId.toString())))
        .andExpect(jsonPath("$.type", is("PROGRAM")));

    BddLogger.then("it should return the parent program");
  }

  @Test
  void shouldReturnTheGroupItself_whenItIsAlreadyAProgram() throws Exception {
    BddLogger.given("a seeded program");
    UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);

    BddLogger.when("calling GET /back-office/groups/{id}/program");
    mockMvc
        .perform(
            get(BASE_PATH + "/" + programId + "/program")
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(programId.toString())));

    BddLogger.then("it should return the program itself");
  }

  @Test
  void shouldReturnNotFound_whenAskingTheProgramOfAnUnknownGroup() throws Exception {
    BddLogger.given("a random group id that does not exist");
    UUID unknownId = UUID.randomUUID();

    BddLogger.when("calling GET /back-office/groups/{id}/program");
    mockMvc
        .perform(
            get(BASE_PATH + "/" + unknownId + "/program")
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    BddLogger.then("it should return 404");
  }

  @Test
  void shouldReturnNotFound_whenIdDoesNotExist() throws Exception {
    BddLogger.given("a random group id that does not exist");
    UUID unknownId = UUID.randomUUID();

    BddLogger.when("calling GET /back-office/groups/{id}");
    mockMvc
        .perform(
            get(BASE_PATH + "/" + unknownId)
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    BddLogger.then("it should return 404");
  }

  @Test
  void shouldReturnUnauthorized_whenApiKeyIsMissing() throws Exception {
    BddLogger.given("a request without api key");
    UUID programId = groupIdOf(SEEDED_PROGRAM_ID_SI_SCO);

    BddLogger.when("calling GET /back-office/groups/{id}");
    mockMvc
        .perform(get(BASE_PATH + "/" + programId).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());

    BddLogger.then("it should return 401");
  }
}
