package fr.avenirsesr.portfolio.backoffice.additionalskill.infrastructure.seeder;

import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.model.AdditionalSkillConfiguration;
import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.model.AdditionalSkillLevel;
import fr.avenirsesr.portfolio.backoffice.additionalskill.domain.port.input.AdditionalSkillConfigurationService;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdditionalSkillConfigSeeder {
  private final AdditionalSkillConfigurationService additionalSkillConfigurationService;

  @Transactional
  public void seed() {
    log.info("Seeding additional skills configuration...");
    var frenchConfig =
        new AdditionalSkillConfiguration(
            new AdditionalSkillLevel("Débutant", "pas beaucoup d'experience"),
            new AdditionalSkillLevel("Intermediaire", "un peu d'experience"),
            new AdditionalSkillLevel("Compétent", "bonne connaissance"),
            new AdditionalSkillLevel("Avancé", "pas mal d'experience"),
            new AdditionalSkillLevel("Expert", "parfaite maîtrise"));

    var englishConfig =
        new AdditionalSkillConfiguration(
            new AdditionalSkillLevel("Beginner", "not much experience"),
            new AdditionalSkillLevel("Intermediate", "some experience"),
            new AdditionalSkillLevel("Competent", "good knowledge"),
            new AdditionalSkillLevel("Advanced", "quite a lot of experience"),
            new AdditionalSkillLevel("Expert", "perfect mastery"));

    var spanishConfig =
        new AdditionalSkillConfiguration(
            new AdditionalSkillLevel("Principiante", "poca experiencia"),
            new AdditionalSkillLevel("Intermedio", "algo de experiencia"),
            new AdditionalSkillLevel("Competente", "buen conocimiento"),
            new AdditionalSkillLevel("Avanzado", "bastante experiencia"),
            new AdditionalSkillLevel("Experto", "dominio perfecto"));

    additionalSkillConfigurationService.postConfiguration(
        Map.of(
            ELanguage.FRENCH, frenchConfig,
            ELanguage.ENGLISH, englishConfig,
            ELanguage.SPANISH, spanishConfig));

    log.info("✔ additional skills configuration saved : {}", frenchConfig);
  }
}
