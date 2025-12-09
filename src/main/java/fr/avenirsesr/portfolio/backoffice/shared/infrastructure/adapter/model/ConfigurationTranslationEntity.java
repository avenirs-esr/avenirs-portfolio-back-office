package fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.model;

import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import fr.avenirsesr.portfolio.common.language.infrastructure.adapter.model.TranslationEntity;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "configuration_translation",
    indexes = {
      @Index(
          name = "idx_configuration_translation_configuration_id",
          columnList = "configuration_id")
    })
@NoArgsConstructor
@Getter
@Setter
public class ConfigurationTranslationEntity extends TranslationEntity {
  @Column(name = "\"value\"", columnDefinition = "TEXT", nullable = false)
  private String value;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "configuration_id", nullable = false)
  private ConfigurationEntity configuration;

  private ConfigurationTranslationEntity(
      UUID id, ELanguage language, ConfigurationEntity configuration, String value) {
    super();
    this.setId(id);
    this.language = language;
    this.configuration = configuration;
    this.value = value;
  }

  public static ConfigurationTranslationEntity of(
      UUID id, ELanguage language, ConfigurationEntity configuration, String value) {
    return new ConfigurationTranslationEntity(id, language, configuration, value);
  }
}
