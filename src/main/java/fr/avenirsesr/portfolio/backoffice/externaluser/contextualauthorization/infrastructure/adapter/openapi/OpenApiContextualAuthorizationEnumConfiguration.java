package fr.avenirsesr.portfolio.backoffice.externaluser.contextualauthorization.infrastructure.adapter.openapi;

import fr.avenirsesr.portfolio.common.openapi.infrastructure.adapter.BaseSwaggerSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiContextualAuthorizationEnumConfiguration {

  @Bean
  public OpenApiCustomizer contextualAuthorizationEnumCustomizer() {
    return openApi ->
        openApi
            .getComponents()
            .addSchemas("EAuthorization", BaseSwaggerSchema.authorizationSchema)
            .addSchemas(
                "EAuthorizationEntityType", BaseSwaggerSchema.authorizationEntityTypeSchema);
  }
}
