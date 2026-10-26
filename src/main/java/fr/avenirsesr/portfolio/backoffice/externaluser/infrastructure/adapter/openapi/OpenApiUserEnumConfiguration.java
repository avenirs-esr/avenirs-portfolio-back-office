package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.openapi;

import fr.avenirsesr.portfolio.common.openapi.infrastructure.adapter.BaseSwaggerSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiUserEnumConfiguration {
  @Bean
  public OpenApiCustomizer userEnumCustomizer() {
    return openApi -> {
      openApi
          .getComponents()
          .addSchemas("EExternalSource", SwaggerSchema.userExternalSourceSchema)
          .addSchemas("EUserCategory", BaseSwaggerSchema.userCategorySchema)
          .addSchemas("EExternalUserRole", SwaggerSchema.externalUserRoleSchema)
          .addSchemas("EUserStatus", SwaggerSchema.externalUserStatusSchema)
          .addSchemas("ERole", BaseSwaggerSchema.roleSchema)
          .addSchemas("EUniverse", BaseSwaggerSchema.universeSchema);
    };
  }
}
