package fr.avenirsesr.portfolio.backoffice.institution.application.adapter.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.institution.application.adapter.dto.DesignateInstitutionAdministratorRequest;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionAdministratorRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.EPermission;
import fr.avenirsesr.portfolio.common.security.infrastructure.adapter.model.HmacAuthenticationToken;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

public class InstitutionAdministratorControllerIT extends ContainerConfigurationTest {

  private static final String BASE_PATH = "/back-office/institutions/administrators";
  private static final String STAFF_EPPN = "ethan.perrin@university.com";
  private static final String LEARNER_EPPN = "arthur.lambert@university.com";
  private static final String RENNES_UAI = "0350001A";
  private static final String RENNES_IUT_UAI = "0350002B";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private InstitutionRepository institutionRepository;
  @Autowired private ExternalUserRepository externalUserRepository;
  @Autowired private InstitutionAdministratorRepository institutionAdministratorRepository;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  private String path(UUID userId) {
    return BASE_PATH + "/" + userId;
  }

  private UUID institutionIdOf(String uai) {
    return institutionRepository.findByUai(uai).map(Institution::getId).orElseThrow();
  }

  private UUID userIdOf(String eppn) {
    return externalUserRepository.findByEppn(eppn).map(ExternalUser::getId).orElseThrow();
  }

  private DesignateInstitutionAdministratorRequest request(UUID... institutionIds) {
    return new DesignateInstitutionAdministratorRequest(List.of(institutionIds));
  }

  private ResultActions postAsSuperAdmin(String path, Object payload) throws Exception {
    return postAs(path, payload, EPermission.PERM_RBAC_ASSIGN);
  }

  private ResultActions postAs(String path, Object payload, EPermission... permissions)
      throws Exception {
    return mockMvc.perform(
        post(path)
            .header("X-Forwarded-For", "203.0.113.10")
            .with(
                authentication(
                    new HmacAuthenticationToken(
                        STAFF_EPPN,
                        Arrays.stream(permissions)
                            .map(permission -> new SimpleGrantedAuthority(permission.authority()))
                            .toList())))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(payload)));
  }

  @Nested
  class WhenDesignatingInstitutionAdministrators {

    @Test
    void shouldReturnForbidenWhenCallerIsNotSuperAdmin() throws Exception {
      BddLogger.given(
          " staff user, a primary institution and a caller without the reserved permission");
      UUID userId = userIdOf(STAFF_EPPN);
      UUID rennesId = institutionIdOf(RENNES_UAI);

      BddLogger.when("posting the designation without the super admin permission");
      BddLogger.then("the request is forbidden");
      postAs(path(userId), request(rennesId), EPermission.PERM_PRIMARY_ESTABLISHMENT_READ)
          .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnNotFoundWhenTheUserDoesNotExist() throws Exception {
      BddLogger.given("an unknown user and a primary institution");
      UUID unknownUser = UUID.randomUUID();

      BddLogger.when("posting the designation as super admin");
      BddLogger.then("the user is not found");
      postAsSuperAdmin(path(unknownUser), request(institutionIdOf(RENNES_UAI)))
          .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenAnInstitutionDoesNotExist() throws Exception {
      BddLogger.given("a staff user and an unknown institution");

      BddLogger.when("posting the designation as super admin");
      BddLogger.then("the institution is not found");
      postAsSuperAdmin(path(userIdOf(STAFF_EPPN)), request(UUID.randomUUID()))
          .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUnprocessableEntityWhenAnInstitutionIsSecondary() throws Exception {
      BddLogger.given("a staff user and a secondary institution");

      BddLogger.when("posting the designation as super admin");
      BddLogger.then("the institution is rejected as not primary");
      postAsSuperAdmin(path(userIdOf(STAFF_EPPN)), request(institutionIdOf(RENNES_IUT_UAI)))
          .andExpect(status().isUnprocessableContent());
    }

    @Test
    void shouldReturnUnprocessableEntityWhenTheUserIsNotStaff() throws Exception {
      BddLogger.given("a non-staff user and a primary institution");

      BddLogger.when("posting the designation as super admin");
      BddLogger.then("the user is rejected as not staff");
      postAsSuperAdmin(path(userIdOf(LEARNER_EPPN)), request(institutionIdOf(RENNES_UAI)))
          .andExpect(status().isForbidden());
    }

    @Test
    void shouldDesignateAndPersistOnTheNominalCase() throws Exception {
      BddLogger.given("a staff user and a primary institution");
      UUID userId = userIdOf(STAFF_EPPN);
      UUID rennesId = institutionIdOf(RENNES_UAI);

      BddLogger.when("posting the designation as super admin");
      postAsSuperAdmin(path(userId), request(rennesId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[*].uai", contains(RENNES_UAI)));

      BddLogger.then("the designation is persisted");
      assertThat(institutionAdministratorRepository.existsByUserAndInstitution(userId, rennesId))
          .isTrue();
    }
  }
}
