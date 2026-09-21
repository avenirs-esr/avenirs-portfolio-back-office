package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.seeder;

import com.fasterxml.jackson.core.type.TypeReference;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.input.ExternalUserService;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.seeder.data.ExternalUserCreationData;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.seeder.fake.FakeExternalUser;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.adapter.data.ESeederSource;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import fr.avenirsesr.portfolio.common.utils.FileReader;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalUserSeeder {

  private static final String PATH_FILE = "seeder/external-users.json";
  private static final int EXTERNAL_USERS_TOTAL_NB = 100;

  private final FileReader fileReader;
  private final ExternalUserService externalUserService;

  @Value("${seeder.source}")
  private ESeederSource seederSource;

  @Transactional
  public List<ExternalUser> seed() {
    log.info("Seeding External Users...");

    List<ExternalUser> externalUsers =
        switch (seederSource) {
          case CSV -> seedFromFixture();
          case FAKER -> seedFake();
        };

    log.info("✔ {} external users synced", externalUsers.size());

    return externalUsers;
  }

  private List<ExternalUser> seedFromFixture() {
    List<ExternalUserCreationData> creationData =
        fileReader.readJSON(PATH_FILE, new TypeReference<List<ExternalUserCreationData>>() {});

    return creationData.stream()
        .map(
            data ->
                externalUserService.importExternalUser(
                    data.eppn(),
                    data.firstName(),
                    data.lastName(),
                    data.email(),
                    data.categories(),
                    data.externalId(),
                    data.source(),
                    data.status() != null ? data.status() : EUserStatus.ACTIVE))
        .toList();
  }

  private List<ExternalUser> seedFake() {
    return IntStream.range(0, EXTERNAL_USERS_TOTAL_NB)
        .mapToObj(
            i -> {
              FakeExternalUser fake = FakeExternalUser.random();
              return externalUserService.importExternalUser(
                  fake.eppn(),
                  fake.firstName(),
                  fake.lastName(),
                  fake.email(),
                  fake.categories(),
                  fake.externalId(),
                  fake.source(),
                  fake.status());
            })
        .toList();
  }
}
