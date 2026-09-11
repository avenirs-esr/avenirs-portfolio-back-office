package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.model;

import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionEntity;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.model.AvenirsBaseEntity;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "groups",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_group_id_si_sco",
          columnNames = {"id_si_sco"})
    },
    indexes = {@Index(name = "idx_groups_id_si_sco", columnList = "id_si_sco")})
@NoArgsConstructor
@Getter
@Setter
public class GroupEntity extends AvenirsBaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(name = "id_si_sco", nullable = false)
  private String idSiSco;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "institution_id", nullable = false)
  private InstitutionEntity institution;

  @Column(name = "code_sise")
  private String codeSise;

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EGroupType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private GroupEntity parent;

  private GroupEntity(
      UUID id,
      String name,
      String idSiSco,
      InstitutionEntity institution,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      GroupEntity parent,
      Instant createdAt,
      Instant updatedAt) {
    this.setId(id);
    this.name = name;
    this.idSiSco = idSiSco;
    this.institution = institution;
    this.codeSise = codeSise;
    this.startDate = startDate;
    this.endDate = endDate;
    this.type = type;
    this.parent = parent;
    this.setCreatedAt(createdAt);
    this.setUpdatedAt(updatedAt);
  }

  public static GroupEntity of(
      UUID id,
      String name,
      String idSiSco,
      InstitutionEntity institution,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      GroupEntity parent,
      Instant createdAt,
      Instant updatedAt) {
    return new GroupEntity(
        id,
        name,
        idSiSco,
        institution,
        codeSise,
        startDate,
        endDate,
        type,
        parent,
        createdAt,
        updatedAt);
  }
}
