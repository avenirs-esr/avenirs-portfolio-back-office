package fr.avenirsesr.portfolio.backoffice.file.infrastructure.adapter.openapi;

import fr.avenirsesr.portfolio.common.file.domain.model.enums.EFileType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.Arrays;

public final class SwaggerSchema {
  private SwaggerSchema() {}

  public static final Schema<String> fileTypeSchema =
      new StringSchema()
          .name("EFileType")
          ._enum(Arrays.stream(EFileType.values()).map(Enum::name).toList())
          .description("Enum for file type");
}
