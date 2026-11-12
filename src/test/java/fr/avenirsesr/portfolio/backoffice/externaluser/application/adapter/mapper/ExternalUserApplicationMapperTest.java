package fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationData;
import fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.dto.ProvisionExternalUserRequest;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUserData;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalUserRole;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ExternalUserApplicationMapperTest {

  private static final String EPPN = "new.user@recia.fr";

  private ProvisionExternalUserRequest requestWith(
      List<ProvisionExternalUserRequest.Affiliation> affiliations) {
    return new ProvisionExternalUserRequest(
        EPPN,
        "New",
        "User",
        "new.user@university.com",
        Set.of(EExternalUserRole.STUDENT, EExternalUserRole.TEACHER),
        "EXT-1",
        "0330001C",
        affiliations);
  }

  @Test
  void shouldMapProvisionRequestToExternalUserData() {
    BddLogger.given("a provisioning request");
    var request = requestWith(List.of());

    BddLogger.when("mapping it to the external user data");
    ExternalUserData data = ExternalUserApplicationMapper.toExternalUserData(request);

    BddLogger.then("every user field is carried over");
    assertEquals(
        new ExternalUserData(
            EPPN,
            "New",
            "User",
            "new.user@university.com",
            Set.of(EExternalUserRole.STUDENT, EExternalUserRole.TEACHER),
            "EXT-1",
            "0330001C"),
        data);
  }

  @Test
  void shouldMapProvisionRequestToOneAffiliationDataPerAffiliation() {
    BddLogger.given("a request with an institution and a group affiliation");
    var request =
        requestWith(
            List.of(
                new ProvisionExternalUserRequest.Affiliation(
                    EUserCategory.STUDENT, "0330001C", null),
                new ProvisionExternalUserRequest.Affiliation(
                    EUserCategory.STAFF, "0330001C", "10000004")));

    BddLogger.when("mapping it to the affiliation data");
    List<ExternalUserAffiliationData> data =
        ExternalUserApplicationMapper.toExternalUserAffiliationData(request);

    BddLogger.then("each line is keyed by the user eppn");
    assertEquals(
        List.of(
            new ExternalUserAffiliationData(EPPN, "0330001C", null, EUserCategory.STUDENT),
            new ExternalUserAffiliationData(EPPN, "0330001C", "10000004", EUserCategory.STAFF)),
        data);
  }

  @Test
  void shouldMapProvisionRequestWithoutAffiliationsToNoAffiliationData() {
    BddLogger.given("a request without affiliations");
    var request = requestWith(null);

    BddLogger.when("mapping it to the affiliation data");
    var data = ExternalUserApplicationMapper.toExternalUserAffiliationData(request);

    BddLogger.then("the result is empty");
    assertTrue(data.isEmpty());
  }
}
