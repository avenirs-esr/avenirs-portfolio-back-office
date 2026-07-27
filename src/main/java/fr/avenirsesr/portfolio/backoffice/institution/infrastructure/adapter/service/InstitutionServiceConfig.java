package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.service;

import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionService;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.service.InstitutionServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class InstitutionServiceConfig {
  private final InstitutionRepository institutionRepository;

  @Bean
  public InstitutionService institutionService() {
    return new InstitutionServiceImpl(institutionRepository);
  }
}
