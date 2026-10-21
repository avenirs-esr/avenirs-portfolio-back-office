package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.application.adapter.dto.AddExternalUserAffiliationRequest;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationData;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.output.repository.ExternalUserAffiliationRepository;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
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

class ExternalUserAffiliationProvisioningControllerIT extends ContainerConfigurationTest {

  private static final String BASE_PATH = "/back-office/admin/external-users/affiliations";
  private static final String EXTERNAL_USERS_PATH = "/back-office/admin/external-users";
  private static final String ADMIN_TOKEN_HEADER = "X-ADMIN-TOKEN";
  private static final String SEEDED_STAFF_EPPN = "ethan.perrin@university.com";
  private static final String SEEDED_STUDENT_EPPN = "lucas.tessier@university.com";
  private static final String SEEDED_RENNES_UAI = "0350001A";
  private static final String SEEDED_RENNES_IUT_UAI = "0350002B";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private ExternalUserRepository externalUserRepository;
  @Autowired private InstitutionRepository institutionRepository;
  @Autowired private ExternalUserAffiliationRepository externalUserAffiliationRepository;

  @Value("${security.authentication.api-key}")
  private String apiKeyValue;

  @Value("${admin.token}")
  private String adminTokenValue;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  private UUID externalUserIdOf(String eppn) {
    return externalUserRepository.findByEppn(eppn).orElseThrow().getId();
  }

  private UUID institutionIdOf(String uai) {
    return institutionRepository.findByUai(uai).orElseThrow().getId();
  }

  @Test
  void shouldReturnForbidden_whenAdminTokenIsInvalid() throws Exception {
    BddLogger.given("an invalid admin token");

    BddLogger.when("calling POST /back-office/admin/external-users/affiliations");
    mockMvc
        .perform(
            post(BASE_PATH)
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .header(ADMIN_TOKEN_HEADER, "wrong-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
        .andExpect(status().isForbidden());

    BddLogger.then("it should return 403");
  }

  @Test
  void shouldImportAffiliations_whenValidBatchPayload() throws Exception {
    BddLogger.given("a batch holding one new affiliation and one already seeded");
    List<ExternalUserAffiliationData> payload =
        List.of(
            new ExternalUserAffiliationData(
                SEEDED_STAFF_EPPN, SEEDED_RENNES_IUT_UAI, null, EUserCategory.STAFF),
            new ExternalUserAffiliationData(
                SEEDED_STUDENT_EPPN, SEEDED_RENNES_UAI, null, EUserCategory.STUDENT));

    BddLogger.when("calling POST /back-office/admin/external-users/affiliations");
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
        .andExpect(jsonPath("$.existingCount", is(1)))
        .andExpect(jsonPath("$.failedCount", is(0)))
        .andExpect(jsonPath("$.created", hasSize(1)))
        .andExpect(
            jsonPath(
                "$.created[0].institutionId",
                is(institutionIdOf(SEEDED_RENNES_IUT_UAI).toString())))
        .andExpect(jsonPath("$.created[0].category", is("STAFF")));

    BddLogger.then("it should create the missing affiliation and report the existing one");
  }

  @Test
  void shouldReportFailure_whenCategoryIsNotHeldByTheUser() throws Exception {
    BddLogger.given("a batch affiliating a student user with the staff category");
    List<ExternalUserAffiliationData> payload =
        List.of(
            new ExternalUserAffiliationData(
                SEEDED_STUDENT_EPPN, SEEDED_RENNES_IUT_UAI, null, EUserCategory.STAFF));

    BddLogger.when("calling POST /back-office/admin/external-users/affiliations");
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
        .andExpect(jsonPath("$.failedCount", is(1)))
        .andExpect(jsonPath("$.failed[0].eppn", is(SEEDED_STUDENT_EPPN)));

    BddLogger.then("it should report the rejected line without failing the whole batch");
  }

  @Test
  void shouldAddAffiliation_whenPostingOneForAnExternalUser() throws Exception {
    BddLogger.given("a seeded staff not yet affiliated to the Rennes IUT");
    UUID externalUserId = externalUserIdOf(SEEDED_STAFF_EPPN);
    UUID institutionId = institutionIdOf(SEEDED_RENNES_IUT_UAI);
    AddExternalUserAffiliationRequest payload =
        new AddExternalUserAffiliationRequest(institutionId, null, EUserCategory.STAFF);

    BddLogger.when("calling POST /back-office/admin/external-users/{id}/affiliations");
    mockMvc
        .perform(
            post(EXTERNAL_USERS_PATH + "/" + externalUserId + "/affiliations")
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .header(ADMIN_TOKEN_HEADER, adminTokenValue)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.externalUserId", is(externalUserId.toString())))
        .andExpect(jsonPath("$.institutionId", is(institutionId.toString())))
        .andExpect(jsonPath("$.category", is("STAFF")));

    BddLogger.then("it should create and return the affiliation");
  }

  @Test
  void shouldReturnForbidden_whenAddingAnAffiliationWithoutTheAdminToken() throws Exception {
    BddLogger.given("a seeded staff and an invalid admin token");
    UUID externalUserId = externalUserIdOf(SEEDED_STAFF_EPPN);
    AddExternalUserAffiliationRequest payload =
        new AddExternalUserAffiliationRequest(
            institutionIdOf(SEEDED_RENNES_IUT_UAI), null, EUserCategory.STAFF);

    BddLogger.when("calling POST /back-office/admin/external-users/{id}/affiliations");
    mockMvc
        .perform(
            post(EXTERNAL_USERS_PATH + "/" + externalUserId + "/affiliations")
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .header(ADMIN_TOKEN_HEADER, "wrong-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isForbidden());

    BddLogger.then("it should return 403 without creating the affiliation");
  }

  @Test
  void shouldRemoveAffiliation_whenDeletingOneOfTheUser() throws Exception {
    BddLogger.given("a seeded student holding affiliations");
    UUID externalUserId = externalUserIdOf(SEEDED_STUDENT_EPPN);
    UUID affiliationId =
        externalUserAffiliationRepository
            .findAllByExternalUserId(externalUserId)
            .getFirst()
            .getId();

    BddLogger.when("calling DELETE /back-office/admin/external-users/{id}/affiliations/{id}");
    mockMvc
        .perform(
            delete(EXTERNAL_USERS_PATH + "/" + externalUserId + "/affiliations/" + affiliationId)
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .header(ADMIN_TOKEN_HEADER, adminTokenValue))
        .andExpect(status().isNoContent());

    BddLogger.then("it should return 204");
  }

  @Test
  void shouldReturnNotFound_whenDeletingAnAffiliationOfAnotherUser() throws Exception {
    BddLogger.given("an affiliation belonging to another external user");
    UUID studentId = externalUserIdOf(SEEDED_STUDENT_EPPN);
    UUID staffId = externalUserIdOf(SEEDED_STAFF_EPPN);
    UUID affiliationId =
        externalUserAffiliationRepository.findAllByExternalUserId(studentId).getFirst().getId();

    BddLogger.when("calling DELETE /back-office/admin/external-users/{id}/affiliations/{id}");
    mockMvc
        .perform(
            delete(EXTERNAL_USERS_PATH + "/" + staffId + "/affiliations/" + affiliationId)
                .principal(uuidPrincipal())
                .header(AvenirsSecurityHeaders.API_KEY, apiKeyValue)
                .header(ADMIN_TOKEN_HEADER, adminTokenValue))
        .andExpect(status().isNotFound());

    BddLogger.then("it should return 404, the affiliation not belonging to that user");
  }
}
