package fr.avenirsesr.portfolio.backoffice.tool.infrastructure.adapter.repository;

import static org.junit.jupiter.api.Assertions.*;

import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.EToolType;
import fr.avenirsesr.portfolio.backoffice.tool.domain.model.Tool;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.file.domain.model.File;
import fr.avenirsesr.portfolio.common.file.domain.model.enums.EFileType;
import fr.avenirsesr.portfolio.common.file.domain.port.output.repository.FileRepository;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ToolDatabaseRepositoryIT extends ContainerConfigurationTest {
  private static final String RENNES_UAI = "0350001A";
  private static final String RENNES_IUT_UAI = "0350002B";
  private static final String BORDEAUX_UAI = "0330001C";

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

  private Tool saved(String name, Institution institution) {
    return toolDatabaseRepository.save(
        Tool.create(name, "Description", "https://tool.example.fr", null, institution));
  }

  private Institution institution(String uai) {
    return institutionRepository.findByUai(uai).orElseThrow();
  }

  private static List<UUID> idsOf(PagedResult<Tool> page) {
    return page.content().stream().map(Tool::getId).toList();
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

  @Test
  void shouldPageOverTheNationalToolsOnly_sortedByName() {
    BddLogger.given("three national tools and a regional one");
    var zotero = saved("zotero", null);
    var moodle = saved("Moodle", null);
    var agenda = saved("Agenda", null);
    saved("ENT", institution(RENNES_UAI));
    entityManager.flush();
    entityManager.clear();

    BddLogger.when("reading the first two national tools");
    var page = toolDatabaseRepository.findAllNational(new PageCriteria(0, 2));

    BddLogger.then("it returns them sorted by name whatever the case, counting national ones only");
    assertEquals(List.of(agenda.getId(), moodle.getId()), idsOf(page));
    assertEquals(3, page.pageInfo().totalElements());
    assertEquals(
        List.of(zotero.getId()),
        idsOf(toolDatabaseRepository.findAllNational(new PageCriteria(1, 2))));
  }

  @Test
  void shouldReturnTheToolsOfTheGivenInstitutions_whateverTheirType() {
    BddLogger.given("national, regional and local tools of several institutions");
    saved("National", null);
    var regional = saved("Regional", institution(RENNES_UAI));
    var local = saved("Local", institution(RENNES_IUT_UAI));
    saved("Bordeaux", institution(BORDEAUX_UAI));
    entityManager.flush();
    entityManager.clear();

    BddLogger.when("reading the tools of Rennes and its IUT without type");
    var page =
        toolDatabaseRepository.findAllOfInstitutions(
            List.of(institution(RENNES_UAI).getId(), institution(RENNES_IUT_UAI).getId()),
            null,
            new PageCriteria(0, 10));

    BddLogger.then("it returns the regional and local tools of those institutions only");
    assertEquals(List.of(local.getId(), regional.getId()), idsOf(page));
    assertEquals(2, page.pageInfo().totalElements());
  }

  @Test
  void shouldFilterTheToolsOfTheGivenInstitutionsOnTheirType() {
    BddLogger.given("a regional and a local tool of Rennes");
    var regional = saved("Regional", institution(RENNES_UAI));
    var local = saved("Local", institution(RENNES_IUT_UAI));
    entityManager.flush();
    entityManager.clear();
    var rennesIds = List.of(institution(RENNES_UAI).getId(), institution(RENNES_IUT_UAI).getId());

    BddLogger.when("reading the tools of Rennes and its IUT by type");
    var regionals =
        toolDatabaseRepository.findAllOfInstitutions(
            rennesIds, EToolType.REGIONAL, new PageCriteria(0, 10));
    var locals =
        toolDatabaseRepository.findAllOfInstitutions(
            rennesIds, EToolType.LOCAL, new PageCriteria(0, 10));
    var nationals =
        toolDatabaseRepository.findAllOfInstitutions(
            rennesIds, EToolType.NATIONAL, new PageCriteria(0, 10));

    BddLogger.then("each type only returns the matching tools, none being national");
    assertEquals(List.of(regional.getId()), idsOf(regionals));
    assertEquals(List.of(local.getId()), idsOf(locals));
    assertTrue(nationals.content().isEmpty());
  }

  @Test
  void shouldReturnNoTool_whenNoInstitutionIsGiven() {
    BddLogger.given("a national and a regional tool");
    saved("National", null);
    saved("Regional", institution(RENNES_UAI));
    entityManager.flush();
    entityManager.clear();

    BddLogger.when("reading the tools of no institution");
    var page =
        toolDatabaseRepository.findAllOfInstitutions(List.of(), null, new PageCriteria(0, 10));

    BddLogger.then("it returns no tool");
    assertTrue(page.content().isEmpty());
    assertEquals(0, page.pageInfo().totalElements());
  }
}
