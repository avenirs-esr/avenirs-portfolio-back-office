package fr.avenirsesr.portfolio.backoffice.group.domain.model;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.common.data.domain.model.AvenirsBaseModel;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Group extends AvenirsBaseModel {
  private String name;
  private String idSiSco;
  private Institution institution;
  private String codeSise;
  private LocalDate startDate;
  private LocalDate endDate;
  private EGroupType type;

  @Getter(AccessLevel.NONE)
  private Group parent;

  private Group(
      UUID id,
      String name,
      String idSiSco,
      Institution institution,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      Group parent,
      Instant createdAt,
      Instant updatedAt) {
    super(id, createdAt, updatedAt);
    this.name = name;
    this.idSiSco = idSiSco;
    this.institution = institution;
    this.codeSise = codeSise;
    this.startDate = startDate;
    this.endDate = endDate;
    this.type = type;
    this.parent = parent;
  }

  public static Group create(
      UUID id,
      String name,
      String idSiSco,
      Institution institution,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      Group parent) {
    Instant now = Instant.now();
    return new Group(
        id, name, idSiSco, institution, codeSise, startDate, endDate, type, parent, now, now);
  }

  public static Group toDomain(
      UUID id,
      String name,
      String idSiSco,
      Institution institution,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      Group parent,
      Instant createdAt,
      Instant updatedAt) {
    return new Group(
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

  public Optional<Group> getParent() {
    return Optional.ofNullable(parent);
  }
}
