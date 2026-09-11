package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.openapi;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiInstitutionEnumConfiguration {
  @Bean
  public OpenApiCustomizer institutionEnumCustomizer() {
    return openApi ->
        openApi.getComponents().addSchemas("EInstitutionType", SwaggerSchema.institutionTypeSchema);
  }
}
