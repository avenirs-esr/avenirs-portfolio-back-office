package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.openapi;

import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.Arrays;

public final class SwaggerSchema {
  private SwaggerSchema() {}

  public static final Schema<String> groupTypeSchema =
      new StringSchema()
          .name("EGroupType")
          ._enum(Arrays.stream(EGroupType.values()).map(Enum::name).toList())
          .description("Enum for group type");
}
