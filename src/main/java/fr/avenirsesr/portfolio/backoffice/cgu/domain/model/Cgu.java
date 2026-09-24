package fr.avenirsesr.portfolio.backoffice.cgu.domain.model;

import fr.avenirsesr.portfolio.common.data.domain.model.AvenirsBaseModel;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Cgu extends AvenirsBaseModel {

  private final UUID fileId;
  private final int version;
  private final Instant uploadedAt;

  private Cgu(
      UUID id, Instant createdAt, Instant updatedAt, UUID fileId, int version, Instant uploadedAt) {
    super(id, createdAt, updatedAt);
    this.fileId = fileId;
    this.version = version;
    this.uploadedAt = uploadedAt;
  }

  public static Cgu create(UUID fileId, int version) {
    Instant now = Instant.now();
    return new Cgu(UUID.randomUUID(), now, now, fileId, version, now);
  }

  public static Cgu toDomain(
      UUID id, Instant createdAt, Instant updatedAt, UUID fileId, int version, Instant uploadedAt) {
    return new Cgu(id, createdAt, updatedAt, fileId, version, uploadedAt);
  }
}
