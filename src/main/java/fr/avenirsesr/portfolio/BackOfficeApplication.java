package fr.avenirsesr.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication(
    scanBasePackages = {"fr.avenirsesr.portfolio.backoffice", "fr.avenirsesr.portfolio.common"})
public class BackOfficeApplication {

  public static void main(String[] args) {
    SpringApplication.run(BackOfficeApplication.class, args);
  }
}
