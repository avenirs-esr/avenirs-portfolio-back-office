package fr.avenirsesr.portfolio.backoffice.institution.domain.model;

import fr.avenirsesr.portfolio.common.data.domain.model.AvenirsBaseModel;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class InstitutionConfig extends AvenirsBaseModel {
  private final UUID institutionId;
  private final boolean apcEnabled;
  private final boolean lifeProjectEnabled;

  private InstitutionConfig(
      UUID id,
      UUID institutionId,
      boolean apcEnabled,
      boolean lifeProjectEnabled,
      Instant createdAt,
      Instant updatedAt) {
    super(id, createdAt, updatedAt);
    this.institutionId = institutionId;
    this.apcEnabled = apcEnabled;
    this.lifeProjectEnabled = lifeProjectEnabled;
  }

  public static InstitutionConfig create(
      UUID institutionId, boolean apcEnabled, boolean lifeProjectEnabled) {
    return new InstitutionConfig(
        UUID.randomUUID(),
        institutionId,
        apcEnabled,
        lifeProjectEnabled,
        Instant.now(),
        Instant.now());
  }

  public static InstitutionConfig of(
      UUID id,
      UUID institutionId,
      boolean apcEnabled,
      boolean lifeProjectEnabled,
      Instant createdAt,
      Instant updatedAt) {
    return new InstitutionConfig(
        id, institutionId, apcEnabled, lifeProjectEnabled, createdAt, updatedAt);
  }
}
