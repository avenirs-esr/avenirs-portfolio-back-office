package fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.repository;

import static org.junit.jupiter.api.Assertions.*;

import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.EToolType;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.common.file.domain.model.File;
import fr.avenirsesr.portfolio.common.file.domain.model.enums.EFileType;
import fr.avenirsesr.portfolio.common.file.domain.port.output.repository.FileRepository;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ToolDatabaseRepositoryIT extends ContainerConfigurationTest {
  private static final String RENNES_UAI = "0350001A";
  private static final String RENNES_IUT_UAI = "0350002B";

  @Autowired private ToolDatabaseRepository toolDatabaseRepository;
  @Autowired private InstitutionRepository institutionRepository;
  @Autowired private FileRepository fileRepository;
  @Autowired private EntityManager entityManager;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  private File savedBanner() {
    return fileRepository.save(
        File.create(
            UUID.randomUUID(),
            EFileType.PNG,
            "banner.png",
            1024,
            "banner.png",
            UUID.randomUUID(),
            false));
  }

  private Tool saveAndReload(Tool tool) {
    toolDatabaseRepository.save(tool);
    entityManager.flush();
    entityManager.clear();
    return toolDatabaseRepository.findById(tool.getId()).orElseThrow();
  }

  @Test
  void shouldStoreNationalToolWithoutBanner() {
    BddLogger.given("a tool without institution nor banner");
    var tool =
        Tool.create("Moodle", "Plateforme de cours", "https://moodle.example.fr", null, null);

    BddLogger.when("saving it and reading it back from the database");
    var stored = saveAndReload(tool);

    BddLogger.then("it is read back as a national tool without banner");
    assertEquals("Moodle", stored.getName());
    assertEquals("Plateforme de cours", stored.getDescription());
    assertEquals("https://moodle.example.fr", stored.getUrl());
    assertTrue(stored.getBanner().isEmpty());
    assertTrue(stored.getInstitution().isEmpty());
    assertEquals(EToolType.NATIONAL, stored.getType());
  }

  @Test
  void shouldStoreRegionalToolOfPrimaryInstitution() {
    BddLogger.given("a tool of the seeded Rennes university, with a registered banner");
    var rennes = institutionRepository.findByUai(RENNES_UAI).orElseThrow();
    var banner = savedBanner();
    var tool =
        Tool.create("ENT", "Espace numérique de travail", "https://ent.example.fr", banner, rennes);

    BddLogger.when("saving it and reading it back from the database");
    var stored = saveAndReload(tool);

    BddLogger.then("it keeps its banner file and institution and is regional");
    var storedBanner = stored.getBanner().orElseThrow();
    assertEquals(banner.getId(), storedBanner.getId());
    assertEquals("banner.png", storedBanner.getFileName());
    assertEquals(EFileType.PNG, storedBanner.getFileType());
    assertEquals(rennes.getId(), stored.getInstitution().orElseThrow().getId());
    assertEquals(EToolType.REGIONAL, stored.getType());
  }

  @Test
  void shouldStoreLocalToolOfSecondaryInstitution() {
    BddLogger.given("a tool of the seeded Rennes IUT");
    var rennesIut = institutionRepository.findByUai(RENNES_IUT_UAI).orElseThrow();
    var tool =
        Tool.create(
            "Emploi du temps", "Planning de l'IUT", "https://edt.example.fr", null, rennesIut);

    BddLogger.when("saving it and reading it back from the database");
    var stored = saveAndReload(tool);

    BddLogger.then("it keeps its institution and is local");
    assertEquals(rennesIut.getId(), stored.getInstitution().orElseThrow().getId());
    assertEquals(EToolType.LOCAL, stored.getType());
  }
}
