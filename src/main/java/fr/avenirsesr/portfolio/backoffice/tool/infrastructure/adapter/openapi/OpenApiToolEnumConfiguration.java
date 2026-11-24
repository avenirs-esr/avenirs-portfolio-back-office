package fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.openapi;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiToolEnumConfiguration {
  @Bean
  public OpenApiCustomizer toolEnumCustomizer() {
    return openApi -> {
      openApi.getComponents().addSchemas("EToolType", SwaggerSchema.toolTypeSchema);
    };
  }
}
