package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model;

import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.model.AvenirsBaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "institution_administrator",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_external_user_institution",
          columnNames = {"external_user_id", "institution_id"})
    },
    indexes = {
      @Index(name = "idx_external_user_id", columnList = "external_user_id"),
      @Index(name = "idx_institution_id", columnList = "institution_id")
    })
@NoArgsConstructor
@Getter
@Setter
public class InstitutionAdministratorEntity extends AvenirsBaseEntity {

  @Column(name = "external_user_id", nullable = false)
  private UUID externalUserId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "institution_id", nullable = false)
  private InstitutionEntity institution;

  @Column(name = "created_by", nullable = false)
  private UUID createdBy;

  private InstitutionAdministratorEntity(
      UUID id,
      UUID externalUserId,
      InstitutionEntity institution,
      UUID createdBy,
      Instant createdAt,
      Instant updatedAt) {
    this.setId(id);
    this.externalUserId = externalUserId;
    this.institution = institution;
    this.createdBy = createdBy;
    this.setCreatedAt(createdAt);
    this.setUpdatedAt(updatedAt);
  }

  public static InstitutionAdministratorEntity of(
      UUID id,
      UUID externalUserId,
      InstitutionEntity institution,
      UUID createdBy,
      Instant createdAt,
      Instant updatedAt) {
    return new InstitutionAdministratorEntity(
        id, externalUserId, institution, createdBy, createdAt, updatedAt);
  }
}
