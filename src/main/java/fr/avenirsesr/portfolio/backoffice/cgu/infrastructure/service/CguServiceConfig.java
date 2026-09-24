package fr.avenirsesr.portfolio.backoffice.cgu.infrastructure.service;

import fr.avenirsesr.portfolio.backoffice.cgu.domain.port.input.CguService;
import fr.avenirsesr.portfolio.backoffice.cgu.domain.port.output.repository.CguRepository;
import fr.avenirsesr.portfolio.backoffice.cgu.domain.service.CguServiceImpl;
import fr.avenirsesr.portfolio.common.file.application.adapter.client.FileClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CguServiceConfig {
  private final FileClient fileClient;
  private final CguRepository cguRepository;

  @Bean
  public CguService cguService() {
    return new CguServiceImpl(fileClient, cguRepository);
  }
}
