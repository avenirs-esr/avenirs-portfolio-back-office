package fr.avenirsesr.portfolio.backoffice.tool.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.common.file.domain.model.File;
import fr.avenirsesr.portfolio.common.file.domain.model.enums.EFileType;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ToolTest {
  private static final Institution PRIMARY_INSTITUTION =
      Institution.create(
          UUID.randomUUID(),
          "Université de Rennes",
          "UR",
          "0350001A",
          "13000550100015",
          EInstitutionType.PRIMARY,
          null);
  private static final Institution SECONDARY_INSTITUTION =
      Institution.create(
          UUID.randomUUID(),
          "Université de Rennes - IUT",
          "IUT-R",
          "0350002B",
          "13000550100023",
          EInstitutionType.SECONDARY,
          PRIMARY_INSTITUTION);

  private static Tool toolOf(Institution institution) {
    return Tool.create(
        "Moodle", "Plateforme de cours", "https://moodle.example.fr", null, institution);
  }

  @Test
  void shouldBeNationalWithoutInstitution() {
    BddLogger.given("a tool without institution");
    var tool = toolOf(null);

    BddLogger.when("reading its type");
    var type = tool.getType();

    BddLogger.then("it is national");
    assertEquals(EToolType.NATIONAL, type);
    assertTrue(tool.getInstitution().isEmpty());
  }

  @Test
  void shouldBeRegionalWithPrimaryInstitution() {
    BddLogger.given("a tool of a primary institution");
    var tool = toolOf(PRIMARY_INSTITUTION);

    BddLogger.when("reading its type");
    var type = tool.getType();

    BddLogger.then("it is regional");
    assertEquals(EToolType.REGIONAL, type);
  }

  @Test
  void shouldBeLocalWithSecondaryInstitution() {
    BddLogger.given("a tool of a secondary institution");
    var tool = toolOf(SECONDARY_INSTITUTION);

    BddLogger.when("reading its type");
    var type = tool.getType();

    BddLogger.then("it is local");
    assertEquals(EToolType.LOCAL, type);
  }

  @Test
  void shouldFollowItsInstitutionWhenMoved() {
    BddLogger.given("a national tool");
    var tool = toolOf(null);

    BddLogger.when("attaching it to a secondary institution");
    tool.setInstitution(SECONDARY_INSTITUTION);

    BddLogger.then("it becomes local");
    assertEquals(EToolType.LOCAL, tool.getType());
  }

  @Test
  void shouldExposeOptionalBanner() {
    BddLogger.given("a tool with a banner and a tool without");
    var banner =
        File.create(
            UUID.randomUUID(),
            EFileType.PNG,
            "banner.png",
            1024,
            "banner.png",
            UUID.randomUUID(),
            false);
    var withBanner =
        Tool.create("Moodle", "Plateforme de cours", "https://moodle.example.fr", banner, null);
    var withoutBanner = toolOf(null);

    BddLogger.when("reading their banners");
    BddLogger.then("only the first one has a banner");
    assertEquals(banner, withBanner.getBanner().orElseThrow());
    assertTrue(withoutBanner.getBanner().isEmpty());
  }
}
