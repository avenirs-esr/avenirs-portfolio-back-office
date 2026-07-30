package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.seeder.fake;

import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.seeder.data.InstitutionCreationData;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.enums.EInstitutionType;
import fr.avenirsesr.portfolio.common.seeder.domain.port.output.SharedDataGenerator;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.adapter.data.DataGeneratorProvider;
import net.datafaker.Faker;

public class FakeInstitution {
  private static final DataGeneratorProvider<SharedDataGenerator> dataGenerator =
      new DataGeneratorProvider<SharedDataGenerator>()
          .init(FakeInstitution.class, SharedDataGenerator.class);

  private static final Faker faker = new Faker();

  private final InstitutionCreationData institution;

  private FakeInstitution(InstitutionCreationData institution) {
    this.institution = institution;
  }

  public static FakeInstitution primary() {
    return new FakeInstitution(
        new InstitutionCreationData(
            faker.university().name(),
            dataGenerator.with("hai").regexify("[0-9]{7}[A-Z]"),
            faker.numerify("#############"),
            faker.numerify("#########"),
            EInstitutionType.PRIMARY,
            null));
  }

  public static FakeInstitution secondary(String parentHai) {
    return new FakeInstitution(
        new InstitutionCreationData(
            faker.university().name(),
            dataGenerator.with("hai").regexify("[0-9]{7}[A-Z]"),
            faker.numerify("#############"),
            faker.numerify("#########"),
            EInstitutionType.SECONDARY,
            parentHai));
  }

  public InstitutionCreationData toCreationData() {
    return institution;
  }
}
