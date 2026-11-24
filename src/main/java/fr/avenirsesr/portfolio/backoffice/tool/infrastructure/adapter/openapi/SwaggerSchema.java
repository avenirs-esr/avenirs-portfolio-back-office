package fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.openapi;

import fr.avenirsesr.portfolio.backoffice.tool.domain.model.EToolType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.Arrays;

public final class SwaggerSchema {
  private SwaggerSchema() {}

  public static final Schema<String> toolTypeSchema =
      new StringSchema()
          .name("EToolType")
          ._enum(Arrays.stream(EToolType.values()).map(Enum::name).toList())
          .description("Enum for tool type");
}
