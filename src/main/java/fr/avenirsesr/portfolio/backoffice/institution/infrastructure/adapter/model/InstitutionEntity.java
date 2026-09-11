package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model;

import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.model.AvenirsBaseEntity;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "institution",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_institution_hai",
          columnNames = {"hai"})
    })
@NoArgsConstructor
@Getter
@Setter
public class InstitutionEntity extends AvenirsBaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String hai;

  private String siret;

  private String siren;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EInstitutionType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private InstitutionEntity parent;

  private InstitutionEntity(
      UUID id,
      String name,
      String hai,
      String siret,
      String siren,
      EInstitutionType type,
      InstitutionEntity parent,
      Instant createdAt,
      Instant updatedAt) {
    this.setId(id);
    this.name = name;
    this.hai = hai;
    this.siret = siret;
    this.siren = siren;
    this.type = type;
    this.parent = parent;
    this.setCreatedAt(createdAt);
    this.setUpdatedAt(updatedAt);
  }

  public static InstitutionEntity of(
      UUID id,
      String name,
      String hai,
      String siret,
      String siren,
      EInstitutionType type,
      InstitutionEntity parent,
      Instant createdAt,
      Instant updatedAt) {
    return new InstitutionEntity(id, name, hai, siret, siren, type, parent, createdAt, updatedAt);
  }
}
