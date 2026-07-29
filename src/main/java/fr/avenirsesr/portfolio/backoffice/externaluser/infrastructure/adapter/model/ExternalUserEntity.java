package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.model;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.model.AvenirsBaseEntity;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "external_user",
    uniqueConstraints = {
      @UniqueConstraint(name = "external_user_eppn_uk", columnNames = "eppn"),
      @UniqueConstraint(
          name = "external_user_external_id_source_uk",
          columnNames = {"external_id", "source"})
    },
    indexes = {
      @Index(name = "idx_ext_user_eppn", columnList = "eppn"),
      @Index(name = "idx_ext_user_email", columnList = "email"),
      @Index(name = "idx_ext_user_source_external_id", columnList = "source, external_id"),
      @Index(name = "idx_ext_user_status", columnList = "status")
    })
@NoArgsConstructor
@Getter
@Setter
public class ExternalUserEntity extends AvenirsBaseEntity {

  @Column(nullable = false, unique = true, length = 255)
  private String eppn;

  @Column(nullable = false, name = "external_id", length = 255)
  private String externalId;

  @Column(nullable = false, length = 100)
  @Enumerated(EnumType.STRING)
  private EExternalSource source;

  @Column(nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private EUserCategory category;

  @Column(nullable = false, length = 255)
  @Email
  private String email;

  @Column(nullable = false, name = "first_name", length = 255)
  private String firstName;

  @Column(nullable = false, name = "last_name", length = 255)
  private String lastName;

  @Column(nullable = false, length = 50)
  @Enumerated(EnumType.STRING)
  private EUserStatus status;

  private ExternalUserEntity(
      UUID id,
      String eppn,
      String externalId,
      EExternalSource source,
      EUserCategory category,
      String email,
      String firstName,
      String lastName,
      EUserStatus status,
      Instant createdAt,
      Instant updatedAt) {
    this.setId(id);
    this.eppn = eppn;
    this.externalId = externalId;
    this.source = source;
    this.category = category;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.status = status;
    this.setCreatedAt(createdAt);
    this.setUpdatedAt(updatedAt);
  }

  public static ExternalUserEntity of(
      UUID id,
      String eppn,
      String externalId,
      EExternalSource source,
      EUserCategory category,
      String email,
      String firstName,
      String lastName,
      EUserStatus status,
      Instant createdAt,
      Instant updatedAt) {
    return new ExternalUserEntity(
        id,
        eppn,
        externalId,
        source,
        category,
        email,
        firstName,
        lastName,
        status,
        createdAt,
        updatedAt);
  }
}
