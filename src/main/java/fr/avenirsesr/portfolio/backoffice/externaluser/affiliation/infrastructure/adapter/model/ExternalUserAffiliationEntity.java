package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.model;

import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.model.ExternalUserEntity;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.model.GroupEntity;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionEntity;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.model.AvenirsBaseEntity;
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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
    name = "external_user_affiliation",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "external_user_affiliation_uk",
          columnNames = {"id_external_user", "institution_id", "group_id", "category"})
    })
@NoArgsConstructor
@Getter
@Setter
public class ExternalUserAffiliationEntity extends AvenirsBaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_external_user", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ExternalUserEntity externalUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "institution_id", nullable = false)
  private InstitutionEntity institution;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id")
  private GroupEntity group;

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false)
  private EUserCategory category;

  private ExternalUserAffiliationEntity(
      UUID id,
      ExternalUserEntity externalUser,
      InstitutionEntity institution,
      GroupEntity group,
      EUserCategory category,
      Instant createdAt,
      Instant updatedAt) {
    this.setId(id);
    this.externalUser = externalUser;
    this.institution = institution;
    this.group = group;
    this.category = category;
    this.setCreatedAt(createdAt);
    this.setUpdatedAt(updatedAt);
  }

  public static ExternalUserAffiliationEntity of(
      UUID id,
      ExternalUserEntity externalUser,
      InstitutionEntity institution,
      GroupEntity group,
      EUserCategory category,
      Instant createdAt,
      Instant updatedAt) {
    return new ExternalUserAffiliationEntity(
        id, externalUser, institution, group, category, createdAt, updatedAt);
  }
}
