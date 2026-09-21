package fr.avenirsesr.portfolio.backoffice.externaluser.domain.model;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.common.data.domain.model.AvenirsBaseModel;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ExternalUser extends AvenirsBaseModel {

  private final String eppn;

  @Setter private String externalId;
  @Setter private EExternalSource source;
  @Setter private Set<EUserCategory> categories;
  @Setter private String email;
  @Setter private String firstName;
  @Setter private String lastName;
  @Setter private EUserStatus status;

  private ExternalUser(
      UUID id,
      Instant createdAt,
      Instant updatedAt,
      String eppn,
      String externalId,
      EExternalSource source,
      Set<EUserCategory> categories,
      String email,
      String firstName,
      String lastName,
      EUserStatus status) {
    super(id, createdAt, updatedAt);
    this.eppn = eppn;
    this.externalId = externalId;
    this.source = source;
    this.categories = categories == null ? Set.of() : Set.copyOf(categories);
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.status = status;
  }

  public static ExternalUser create(
      String eppn,
      String externalId,
      EExternalSource source,
      Set<EUserCategory> categories,
      String email,
      String firstName,
      String lastName,
      EUserStatus status) {
    Instant now = Instant.now();

    return new ExternalUser(
        UUID.randomUUID(),
        now,
        now,
        eppn,
        externalId,
        source,
        categories,
        email,
        firstName,
        lastName,
        status);
  }

  public static ExternalUser toDomain(
      UUID id,
      Instant createdAt,
      Instant updatedAt,
      String eppn,
      String externalId,
      EExternalSource source,
      Set<EUserCategory> categories,
      String email,
      String firstName,
      String lastName,
      EUserStatus status) {
    return new ExternalUser(
        id,
        createdAt,
        updatedAt,
        eppn,
        externalId,
        source,
        categories,
        email,
        firstName,
        lastName,
        status);
  }

  public boolean isActive() {
    return EUserStatus.ACTIVE.equals(status);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExternalUser that = (ExternalUser) o;
    return Objects.equals(eppn, that.eppn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eppn);
  }

  @Override
  public String toString() {
    return "ExternalUser[eppn=" + eppn + ", source=" + source + ", externalId=" + externalId + ']';
  }
}
