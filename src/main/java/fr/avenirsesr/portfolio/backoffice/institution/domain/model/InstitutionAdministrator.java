package fr.avenirsesr.portfolio.backoffice.institution.domain.model;

import fr.avenirsesr.portfolio.common.data.domain.model.AvenirsBaseModel;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstitutionAdministrator extends AvenirsBaseModel {

  private UUID externalUserId;
  private Institution institution;
  private UUID createdBy;

  private InstitutionAdministrator(
      UUID id, UUID externalUserId, Institution institution, Instant createdAt, UUID createdBy) {
    super(id, createdAt, createdAt);
    this.externalUserId = externalUserId;
    this.institution = institution;
    this.createdBy = createdBy;
  }

  public static InstitutionAdministrator create(
      UUID externalUserId, Institution institution, UUID createdBy) {
    Instant now = Instant.now();
    return new InstitutionAdministrator(
        UUID.randomUUID(), externalUserId, institution, now, createdBy);
  }

  public static InstitutionAdministrator toDomain(
      UUID id, UUID externalUserId, Institution institution, Instant createdAt, UUID createdBy) {
    return new InstitutionAdministrator(id, externalUserId, institution, createdAt, createdBy);
  }
}
