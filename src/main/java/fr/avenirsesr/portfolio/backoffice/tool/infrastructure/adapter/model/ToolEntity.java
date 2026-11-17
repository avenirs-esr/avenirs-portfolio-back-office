package fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.model;

import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionEntity;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.model.AvenirsBaseEntity;
import fr.avenirsesr.portfolio.common.file.infrastructure.adapter.model.FileEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "tool",
    indexes = {@Index(name = "idx_tool_institution_id", columnList = "institution_id")})
@NoArgsConstructor
@Getter
@Setter
public class ToolEntity extends AvenirsBaseEntity {

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String description;

  @Column(nullable = false, length = 2048)
  private String url;

  @OneToOne
  @JoinColumn(name = "banner_id")
  private FileEntity banner;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "institution_id")
  private InstitutionEntity institution;

  private ToolEntity(
      UUID id,
      String name,
      String description,
      String url,
      FileEntity banner,
      InstitutionEntity institution,
      Instant createdAt,
      Instant updatedAt) {
    this.setId(id);
    this.name = name;
    this.description = description;
    this.url = url;
    this.banner = banner;
    this.institution = institution;
    this.setCreatedAt(createdAt);
    this.setUpdatedAt(updatedAt);
  }

  public static ToolEntity of(
      UUID id,
      String name,
      String description,
      String url,
      FileEntity banner,
      InstitutionEntity institution,
      Instant createdAt,
      Instant updatedAt) {
    return new ToolEntity(id, name, description, url, banner, institution, createdAt, updatedAt);
  }
}
