package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.seeder.fake;

import fr.avenirsesr.portfolio.backoffice.group.domain.model.enums.EGroupType;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.seeder.data.GroupCreationData;
import fr.avenirsesr.portfolio.common.seeder.domain.port.output.SharedDataGenerator;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.adapter.data.DataGeneratorProvider;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.datafaker.Faker;

public class FakeGroup {
  private static final DataGeneratorProvider<SharedDataGenerator> dataGenerator =
      new DataGeneratorProvider<SharedDataGenerator>()
          .init(FakeGroup.class, SharedDataGenerator.class);

  private static final Faker faker = new Faker();

  private final GroupCreationData group;

  private FakeGroup(GroupCreationData group) {
    this.group = group;
  }

  public static FakeGroup program(UUID institutionId) {
    LocalDate startDate = pastDate(730);
    return new FakeGroup(
        new GroupCreationData(
            faker.educator().course(),
            dataGenerator.with("idSiSco").regexify("[0-9]{8}"),
            institutionId,
            faker.numerify("###########"),
            startDate,
            startDate.plusYears(3),
            EGroupType.PROGRAM,
            null));
  }

  public static FakeGroup programOption(UUID institutionId, String parentIdSiSco) {
    LocalDate startDate = pastDate(730);
    return new FakeGroup(
        new GroupCreationData(
            faker.educator().course() + " - " + faker.educator().subjectWithNumber(),
            dataGenerator.with("idSiSco").regexify("[0-9]{8}"),
            institutionId,
            faker.numerify("###########"),
            startDate,
            startDate.plusYears(2),
            EGroupType.PROGRAM_OPTION,
            parentIdSiSco));
  }

  public static FakeGroup studentGroup(UUID institutionId, String parentIdSiSco) {
    LocalDate startDate = pastDate(365);
    return new FakeGroup(
        new GroupCreationData(
            "Groupe " + faker.letterify("?").toUpperCase(),
            dataGenerator.with("idSiSco").regexify("[0-9]{8}"),
            institutionId,
            faker.numerify("###########"),
            startDate,
            startDate.plusYears(1),
            EGroupType.STUDENT_GROUP,
            parentIdSiSco));
  }

  private static LocalDate pastDate(int maxDaysAgo) {
    return faker.timeAndDate().past(maxDaysAgo, TimeUnit.DAYS).atZone(ZoneOffset.UTC).toLocalDate();
  }

  public GroupCreationData toCreationData() {
    return group;
  }
}
