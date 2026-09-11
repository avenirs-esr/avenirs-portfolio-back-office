package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.openapi;

import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.Arrays;

public final class SwaggerSchema {
  private SwaggerSchema() {}

  public static final Schema<String> institutionTypeSchema =
      new StringSchema()
          .name("EInstitutionType")
          ._enum(Arrays.stream(EInstitutionType.values()).map(Enum::name).toList())
          .description("Enum for institution type");
}
