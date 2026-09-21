package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.seeder.fake;

import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.seeder.domain.port.output.SharedDataGenerator;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.adapter.FakeExternalSource;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.adapter.data.DataGeneratorProvider;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import java.util.EnumSet;
import java.util.Set;
import net.datafaker.Faker;

public record FakeExternalUser(
    String eppn,
    String firstName,
    String lastName,
    String email,
    Set<EUserCategory> categories,
    String externalId,
    EExternalSource source,
    EUserStatus status) {

  private static final DataGeneratorProvider<SharedDataGenerator> dataGenerator =
      new DataGeneratorProvider<SharedDataGenerator>()
          .init(FakeExternalUser.class, SharedDataGenerator.class);

  private static final Faker faker = new Faker();

  public static FakeExternalUser random() {
    String firstName = faker.name().firstName();
    String lastName = faker.name().lastName();
    String eppn = faker.internet().emailAddress();

    boolean isStudent = dataGenerator.with("isStudent").bool();
    Set<EUserCategory> categories =
        isStudent ? EnumSet.of(EUserCategory.STUDENT) : EnumSet.of(EUserCategory.STAFF);

    return new FakeExternalUser(
        eppn,
        firstName,
        lastName,
        eppn,
        categories,
        FakeExternalSource.generateExternalSourceId(),
        EExternalSource.BACK_OFFICE,
        EUserStatus.ACTIVE);
  }
}
