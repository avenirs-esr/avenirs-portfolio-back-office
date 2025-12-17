package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionConfigService;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionConfigRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.service.InstitutionConfigServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class InstitutionConfigServiceConfig {
  private final InstitutionConfigRepository institutionConfigRepository;

  @Bean
  public InstitutionConfigService institutionConfigService() {
    return new InstitutionConfigServiceImpl(institutionConfigRepository);
  }
}
