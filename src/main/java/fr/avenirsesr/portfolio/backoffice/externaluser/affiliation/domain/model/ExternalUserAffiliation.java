package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.common.data.domain.model.AvenirsBaseModel;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ExternalUserAffiliation extends AvenirsBaseModel {

  private final ExternalUser externalUser;
  private final Institution institution;
  private final Group group;

  private ExternalUserAffiliation(
      UUID id,
      Instant createdAt,
      Instant updatedAt,
      ExternalUser externalUser,
      Institution institution,
      Group group) {
    super(id, createdAt, updatedAt);
    this.externalUser = externalUser;
    this.institution = institution;
    this.group = group;
  }

  public static ExternalUserAffiliation create(
      ExternalUser externalUser, Institution institution, Group group) {
    Instant now = Instant.now();
    return new ExternalUserAffiliation(
        UUID.randomUUID(), now, now, externalUser, institution, group);
  }

  public static ExternalUserAffiliation toDomain(
      UUID id,
      Instant createdAt,
      Instant updatedAt,
      ExternalUser externalUser,
      Institution institution,
      Group group) {
    return new ExternalUserAffiliation(id, createdAt, updatedAt, externalUser, institution, group);
  }
}
