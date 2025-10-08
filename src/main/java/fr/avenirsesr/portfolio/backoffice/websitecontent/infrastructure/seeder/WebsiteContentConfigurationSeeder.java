package fr.avenirsesr.portfolio.backoffice.websitecontent.infrastructure.seeder;

import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.model.BuildLifeProjectConfiguration;
import fr.avenirsesr.portfolio.backoffice.websitecontent.domain.port.input.WebsiteContentConfigurationService;
import fr.avenirsesr.portfolio.common.language.domain.model.enums.ELanguage;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebsiteContentConfigurationSeeder {
  private final WebsiteContentConfigurationService service;

  @Transactional
  public void seed() {
    log.info("Seeding website content configuration...");
    var config =
        Map.of(
            ELanguage.FRENCH,
                new BuildLifeProjectConfiguration(
                    "### Imaginez votre futur en identifiant des trajectoires possibles ###\n\n"
                        + "@@\n"
                        + "Suite au travail sur vos intérêts, motivations et valeurs, et sur"
                        + " l’exploration des futurs possibles, une ou plusieurs idées de projet de"
                        + " vie peuvent se dessiner.\n"
                        + "**La phase de validation de votre/vos projet(s) peut alors débuter.**\n"
                        + "Les questions à se poser à ce stade sont :\n\n"
                        + "-- **Quelles sont mes compétences acquises et utiles à ce projet ?**\n"
                        + "-- **Quelles sont les nouvelles compétences à acquérir pour réaliser ce"
                        + " projet ?**\n"
                        + "-- **Comment les acquérir ?**\n"
                        + "Les réponses à ces questions vous aideront à **construire une ou"
                        + " plusieurs trajectoires cohérentes avec votre projet en combinant"
                        + " plusieurs fiches de futurs possibles** (formations, mobilités, métiers"
                        + " etc.) que vous avez ajoutées dans votre portfolio.\n"
                        + "**Afin de vous guider dans votre réflexion, vous pourrez associer des"
                        + " traces que vous avez ajoutées à votre portfolio de compétences.** Ces"
                        + " traces vous serviront à prouver vos apprentissages et contribueront à"
                        + " démontrer votre engagement et votre progression dans :\n\n"
                        + "-- le développement de vos compétences\n"
                        + "-- la construction d’un projet de vie cohérent avec votre parcours, vos"
                        + " compétences et appétences.\n"
                        + "@@\n"),
            ELanguage.ENGLISH,
                new BuildLifeProjectConfiguration(
                    "### Imagine your future by identifying possible trajectories ###\n\n"
                        + "@@\n"
                        + "Following the work on your interests, motivations, and values, and on"
                        + " exploring possible futures, one or more life project ideas may"
                        + " emerge.\n"
                        + "**The validation phase of your project(s) can then begin.**\n"
                        + "The questions to ask at this stage are :\n\n"
                        + "-- **What skills have I acquired that are useful for this project?**\n"
                        + "-- **What new skills do I need to acquire to complete this project?**\n"
                        + "-- **How can I acquire them?**\n"
                        + "The answers to these questions will help you **build one or more"
                        + " coherent trajectories with your project by combining several possible"
                        + " future sheets** (training, mobility, careers, etc.) that you have added"
                        + " to your portfolio.\n"
                        + "**To guide you in your reflection, you can associate traces that you"
                        + " have added to your skills portfolio.** These traces will serve to"
                        + " demonstrate your learning and contribute to showing your commitment and"
                        + " progress in :\n\n"
                        + "-- the development of your skills\n"
                        + "-- the construction of a coherent life project aligned with your path,"
                        + " skills, and inclinations.\n"
                        + "@@\n"));
    service.postLifeProjectConfiguration(config);
    log.info("✔ additional skills configuration saved : {}", config);
  }
}
