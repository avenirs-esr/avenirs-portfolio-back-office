package fr.avenirsesr.portfolio.backoffice.shared.domain.model;

import fr.avenirsesr.portfolio.common.data.domain.model.AvenirsBaseModel;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Configuration extends AvenirsBaseModel {
  private final EConfigurationScope scope;
  private final EConfiguration key;
  private String value;

  private Configuration(
      UUID id,
      EConfigurationScope scope,
      EConfiguration key,
      String value,
      Instant createdAt,
      Instant updatedAt) {
    super(id, createdAt, updatedAt);
    this.scope = scope;
    this.key = key;
    this.value = value;
  }

  public static Configuration create(
      UUID id, EConfigurationScope scope, EConfiguration key, String value) {
    return new Configuration(id, scope, key, value, Instant.now(), Instant.now());
  }

  public static Configuration toDomain(
      UUID id,
      EConfigurationScope scope,
      EConfiguration key,
      String value,
      Instant createdAt,
      Instant updatedAt) {
    return new Configuration(id, scope, key, value, createdAt, updatedAt);
  }
}
