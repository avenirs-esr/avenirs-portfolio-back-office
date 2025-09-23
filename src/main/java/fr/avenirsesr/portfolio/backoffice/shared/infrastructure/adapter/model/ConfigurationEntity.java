package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.model;

import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfiguration;
import fr.avenirsesr.portfolio.backoffice.shared.domain.model.EConfigurationScope;
import fr.avenirsesr.portfolio.common.data.infrastructure.adapter.model.AvenirsBaseEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "configuration",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"scope", "key"})})
@NoArgsConstructor
@Getter
@Setter
public class ConfigurationEntity extends AvenirsBaseEntity {
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private EConfigurationScope scope;

  @Column(name = "\"key\"", nullable = false)
  private String key;

  @Getter(AccessLevel.NONE)
  @Column(name = "\"value\"")
  private String value;

  @OneToMany(
      mappedBy = "configuration",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private Set<ConfigurationTranslationEntity> translations = new HashSet<>();

  private ConfigurationEntity(
      UUID id, EConfigurationScope scope, EConfiguration key, String value) {
    this.setId(id);
    this.scope = scope;
    this.key = key.name();
    this.value = value;
  }

  public static ConfigurationEntity of(
      UUID id, EConfigurationScope scope, EConfiguration key, String value) {
    return new ConfigurationEntity(id, scope, key, value);
  }

  public Optional<String> getValue() {
    return Optional.ofNullable(value);
  }
}
