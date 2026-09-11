package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.openapi;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiGroupEnumConfiguration {
  @Bean
  public OpenApiCustomizer groupEnumCustomizer() {
    return openApi ->
        openApi.getComponents().addSchemas("EGroupType", SwaggerSchema.groupTypeSchema);
  }
}
