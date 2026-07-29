package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.openapi;

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
          .addSchemas("EUserCategory", SwaggerSchema.userCategorySchema)
          .addSchemas("EUserStatus", SwaggerSchema.externalUserStatusSchema);
    };
  }
}
