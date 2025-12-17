package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.seeder.fake;

import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionConfigEntity;
import fr.avenirsesr.portfolio.common.seeder.domain.port.output.SharedDataGenerator;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.adapter.data.DataGeneratorProvider;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import net.datafaker.Faker;

public class FakeInstitutionConfig {
  private static final DataGeneratorProvider<SharedDataGenerator> dataGenerator =
      new DataGeneratorProvider<SharedDataGenerator>()
          .init(FakeInstitutionConfig.class, SharedDataGenerator.class);

  private static final Faker faker = new Faker();
  private static final Random random = new Random();

  private final InstitutionConfigEntity institutionConfig;

  private FakeInstitutionConfig(InstitutionConfigEntity institutionConfig) {
    this.institutionConfig = institutionConfig;
  }

  public static FakeInstitutionConfig of(UUID institutionId) {
    return new FakeInstitutionConfig(
        InstitutionConfigEntity.of(
            dataGenerator.with("id").uuid(),
            institutionId,
            faker.random().nextBoolean(),
            faker.random().nextBoolean(),
            Instant.now(),
            Instant.now()));
  }

  public InstitutionConfigEntity toEntity() {
    return institutionConfig;
  }
}
