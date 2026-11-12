package fr.avenirsesr.portfolio.backoffice.externaluser.application.adapter.dto;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalUserRole;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import java.util.List;
import java.util.Set;

public record ProvisionExternalUserRequest(
    String eppn,
    String firstName,
    String lastName,
    String email,
    Set<EExternalUserRole> roles,
    String externalId,
    String institutionUAI,
    List<Affiliation> affiliations) {

  public ProvisionExternalUserRequest {
    affiliations = affiliations == null ? List.of() : affiliations;
  }

  public record Affiliation(EUserCategory category, String institutionUAI, String groupIdSISco) {}
}
