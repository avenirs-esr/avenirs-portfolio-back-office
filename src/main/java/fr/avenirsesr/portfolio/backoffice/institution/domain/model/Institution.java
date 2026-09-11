package fr.avenirsesr.portfolio.backoffice.institution.domain.model;

import fr.avenirsesr.portfolio.common.data.domain.model.AvenirsBaseModel;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Institution extends AvenirsBaseModel {
  private String name;
  private String hai;
  private String siret;
  private String siren;
  private EInstitutionType type;

  @Getter(AccessLevel.NONE)
  private Institution parent;

  private Institution(
      UUID id,
      String name,
      String hai,
      String siret,
      String siren,
      EInstitutionType type,
      Institution parent,
      Instant createdAt,
      Instant updatedAt) {
    super(id, createdAt, updatedAt);
    this.name = name;
    this.hai = hai;
    this.siret = siret;
    this.siren = siren;
    this.type = type;
    this.parent = parent;
  }

  public static Institution create(
      UUID id,
      String name,
      String hai,
      String siret,
      String siren,
      EInstitutionType type,
      Institution parent) {
    Instant now = Instant.now();
    return new Institution(id, name, hai, siret, siren, type, parent, now, now);
  }

  public static Institution toDomain(
      UUID id,
      String name,
      String hai,
      String siret,
      String siren,
      EInstitutionType type,
      Institution parent,
      Instant createdAt,
      Instant updatedAt) {
    return new Institution(id, name, hai, siret, siren, type, parent, createdAt, updatedAt);
  }

  public Optional<Institution> getParent() {
    return Optional.ofNullable(parent);
  }
}
