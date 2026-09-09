package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.seeder;

import com.fasterxml.jackson.core.type.TypeReference;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.input.ExternalUserService;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.mapper.ExternalUserMapper;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.model.ExternalUserEntity;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.seeder.data.ExternalUserCreationData;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.seeder.fake.FakeExternalUser;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupNotFoundException;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.common.seeder.domain.port.output.SharedDataGenerator;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.adapter.data.DataGeneratorProvider;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.adapter.data.ESeederSource;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import fr.avenirsesr.portfolio.common.utils.FileReader;
import fr.avenirsesr.portfolio.common.validation.infrastructure.adapter.utils.ValidationUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

  private static final DataGeneratorProvider<SharedDataGenerator> dataGenerator =
      new DataGeneratorProvider<SharedDataGenerator>()
          .init(ExternalUserSeeder.class, SharedDataGenerator.class);

  private static final String PATH_FILE = "seeder/external-users.json";
  private static final int EXTERNAL_USERS_PER_INSTITUTION_NB = 10;

  private final FileReader fileReader;
  private final ExternalUserService externalUserService;
  private final InstitutionRepository institutionRepository;
  private final GroupRepository groupRepository;

  @Value("${seeder.source}")
  private ESeederSource seederSource;

  @Transactional
  public List<ExternalUserEntity> seed(List<UUID> savedInstitutionIds, List<UUID> savedGroupIds) {
    log.info("Seeding External Users...");

    List<ExternalUser> externalUsers =
        switch (seederSource) {
          case CSV -> seedFromFixture();
          case FAKER -> seedFake(savedInstitutionIds, savedGroupIds);
        };

    List<ExternalUserEntity> externalUsersSaved =
        externalUsers.stream().map(ExternalUserMapper.INSTANCE::fromDomain).toList();

    log.info("✔ {} external users synced", externalUsersSaved.size());

    return externalUsersSaved;
  }

  private List<ExternalUser> seedFromFixture() {
    List<ExternalUserCreationData> creationData =
        fileReader.readJSON(PATH_FILE, new TypeReference<List<ExternalUserCreationData>>() {});

    var externalUsers = new ArrayList<ExternalUser>();

    creationData.forEach(
        data -> {
          UUID institutionId = resolveInstitutionId(data.institutionHai());
          UUID groupId = data.groupIdSiSco() != null ? resolveGroupId(data.groupIdSiSco()) : null;

          externalUsers.add(
              externalUserService.importExternalUser(
                  data.eppn(),
                  data.firstName(),
                  data.lastName(),
                  data.email(),
                  data.categories(),
                  data.externalId(),
                  data.source(),
                  institutionId,
                  groupId,
                  data.status() != null ? data.status() : EUserStatus.ACTIVE));
        });

    return externalUsers;
  }

  private List<ExternalUser> seedFake(List<UUID> savedInstitutionIds, List<UUID> savedGroupIds) {
    ValidationUtils.requireNonEmpty(savedInstitutionIds, "savedInstitutionIds cannot be empty");

    var externalUsers = new ArrayList<ExternalUser>();

    savedInstitutionIds.forEach(
        institutionId ->
            IntStream.range(0, EXTERNAL_USERS_PER_INSTITUTION_NB)
                .forEach(
                    i -> {
                      UUID groupId =
                          savedGroupIds.isEmpty()
                              ? null
                              : dataGenerator.with("group").pickIn(savedGroupIds);
                      FakeExternalUser fake = FakeExternalUser.random(institutionId, groupId);

                      externalUsers.add(
                          externalUserService.importExternalUser(
                              fake.eppn(),
                              fake.firstName(),
                              fake.lastName(),
                              fake.email(),
                              fake.categories(),
                              fake.externalId(),
                              fake.source(),
                              fake.institutionId(),
                              fake.groupId(),
                              fake.status()));
                    }));

    return externalUsers;
  }

  private UUID resolveInstitutionId(String institutionHai) {
    return institutionRepository
        .findByHai(institutionHai)
        .orElseThrow(InstitutionNotFoundException::new)
        .getId();
  }

  private UUID resolveGroupId(String groupIdSiSco) {
    return groupRepository
        .findByIdSiSco(groupIdSiSco)
        .orElseThrow(GroupNotFoundException::new)
        .getId();
  }
}
