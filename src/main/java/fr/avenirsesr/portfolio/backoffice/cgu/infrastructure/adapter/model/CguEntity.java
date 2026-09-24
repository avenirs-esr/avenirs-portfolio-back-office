package fr.avenirsesr.portfolio.backoffice.cgu.infrastructure.adapter.model;

import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.model.AvenirsBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "cgu",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "cgu_version_uk",
          columnNames = {"version"})
    })
@NoArgsConstructor
@Getter
@Setter
public class CguEntity extends AvenirsBaseEntity {

  @Column(name = "file_id", nullable = false)
  private UUID fileId;

  @Column(name = "version", nullable = false)
  private int version;

  @Column(name = "uploaded_at", nullable = false)
  private Instant uploadedAt;

  private CguEntity(
      UUID id, UUID fileId, int version, Instant uploadedAt, Instant createdAt, Instant updatedAt) {
    this.setId(id);
    this.fileId = fileId;
    this.version = version;
    this.uploadedAt = uploadedAt;
    this.setCreatedAt(createdAt);
    this.setUpdatedAt(updatedAt);
  }

  public static CguEntity of(
      UUID id, UUID fileId, int version, Instant uploadedAt, Instant createdAt, Instant updatedAt) {
    return new CguEntity(id, fileId, version, uploadedAt, createdAt, updatedAt);
  }
}
