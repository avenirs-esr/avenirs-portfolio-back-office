package fr.avenirsesr.portfolio.backoffice.tool.domain.model;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.common.data.domain.model.AvenirsBaseModel;
import fr.avenirsesr.portfolio.common.file.domain.model.File;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tool extends AvenirsBaseModel {
  private String name;
  private String description;
  private String url;

  @Getter(AccessLevel.NONE)
  private File banner;

  @Getter(AccessLevel.NONE)
  private Institution institution;

  private Tool(
      UUID id,
      String name,
      String description,
      String url,
      File banner,
      Institution institution,
      Instant createdAt,
      Instant updatedAt) {
    super(id, createdAt, updatedAt);
    this.name = name;
    this.description = description;
    this.url = url;
    this.banner = banner;
    this.institution = institution;
  }

  public static Tool create(
      String name, String description, String url, File banner, Institution institution) {
    Instant now = Instant.now();
    return new Tool(UUID.randomUUID(), name, description, url, banner, institution, now, now);
  }

  public static Tool toDomain(
      UUID id,
      String name,
      String description,
      String url,
      File banner,
      Institution institution,
      Instant createdAt,
      Instant updatedAt) {
    return new Tool(id, name, description, url, banner, institution, createdAt, updatedAt);
  }

  public Optional<File> getBanner() {
    return Optional.ofNullable(banner);
  }

  public Optional<Institution> getInstitution() {
    return Optional.ofNullable(institution);
  }

  public EToolType getType() {
    if (institution == null) {
      return EToolType.NATIONAL;
    }
    return switch (institution.getType()) {
      case PRIMARY -> EToolType.REGIONAL;
      case SECONDARY -> EToolType.LOCAL;
    };
  }
}
