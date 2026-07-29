package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.openapi;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.Arrays;

public final class SwaggerSchema {
  private SwaggerSchema() {}

  public static final Schema<String> userExternalSourceSchema =
      new StringSchema()
          .name("EExternalSource")
          ._enum(Arrays.stream(EExternalSource.values()).map(Enum::name).toList())
          .description("Enum for external source");

  public static final Schema<String> userCategorySchema =
      new StringSchema()
          .name("EUserCategory")
          ._enum(Arrays.stream(EUserCategory.values()).map(Enum::name).toList())
          .description("Enum for external user category");

  public static final Schema<String> externalUserStatusSchema =
      new StringSchema()
          .name("EUserStatus")
          ._enum(Arrays.stream(EUserStatus.values()).map(Enum::name).toList())
          .description("Enum for external user status");
}
