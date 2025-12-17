package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model;

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
    name = "institution_config",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_institution_config_institution",
          columnNames = {"institution_id"})
    })
@NoArgsConstructor
@Getter
@Setter
public class InstitutionConfigEntity extends AvenirsBaseEntity {

  @Column(name = "institution_id", nullable = false)
  private UUID institutionId;

  @Column(name = "apc_enabled", nullable = false)
  private boolean apcEnabled;

  @Column(name = "life_project_enabled", nullable = false)
  private boolean lifeProjectEnabled;

  private InstitutionConfigEntity(
      UUID id,
      UUID institutionId,
      boolean apcEnabled,
      boolean lifeProjectEnabled,
      Instant createdAt,
      Instant updatedAt) {
    this.setId(id);
    this.institutionId = institutionId;
    this.apcEnabled = apcEnabled;
    this.lifeProjectEnabled = lifeProjectEnabled;
    this.setCreatedAt(createdAt);
    this.setUpdatedAt(updatedAt);
  }

  public static InstitutionConfigEntity of(
      UUID id,
      UUID institutionId,
      boolean apcEnabled,
      boolean lifeProjectEnabled,
      Instant createdAt,
      Instant updatedAt) {
    return new InstitutionConfigEntity(
        id, institutionId, apcEnabled, lifeProjectEnabled, createdAt, updatedAt);
  }
}
