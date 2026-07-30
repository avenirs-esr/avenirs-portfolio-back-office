package fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.seeder;

import com.fasterxml.jackson.core.type.TypeReference;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.input.ExternalUserService;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.mapper.ExternalUserMapper;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.model.ExternalUserEntity;
import fr.avenirsesr.portfolio.backoffice.externaluser.infrastructure.adapter.seeder.data.ExternalUserCreationData;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupNotFoundException;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.common.seeder.domain.port.output.SharedDataGenerator;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.adapter.data.DataGeneratorProvider;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.adapter.data.ESeederSource;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import fr.avenirsesr.portfolio.common.utils.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

  private final FileReader fileReader;
  private final ExternalUserService externalUserService;
  private final InstitutionRepository institutionRepository;
  private final GroupRepository groupRepository;

  @Value("${seeder.source}")
  private ESeederSource seederSource;

  @Transactional
  public List<ExternalUserEntity> seed() {
    log.info("Seeding External Users...");

    List<ExternalUserCreationData> creationData =
        fileReader.readJSON(PATH_FILE, new TypeReference<List<ExternalUserCreationData>>() {});

    var externalUsers = new ArrayList<ExternalUser>();

    creationData.forEach(
        data -> {
          UUID institutionId = resolveInstitutionId(data.institutionHai());
          UUID groupId = data.groupIdSiSco() != null ? resolveGroupId(data.groupIdSiSco()) : null;

          var externalUser =
              externalUserService.importExternalUser(
                  data.eppn(),
                  data.firstName(),
                  data.lastName(),
                  data.email(),
                  data.category(),
                  data.externalId(),
                  data.source(),
                  institutionId,
                  groupId,
                  data.status() != null ? data.status() : EUserStatus.ACTIVE);

          externalUsers.add(externalUser);
        });

    List<ExternalUserEntity> externalUsersSaved =
        externalUsers.stream().map(ExternalUserMapper.INSTANCE::fromDomain).toList();

    log.info("✔ {} external users synced", externalUsersSaved.size());

    return externalUsersSaved;
  }

  /**
   * The fixture references institutions/groups by their stable business keys (hai, id_si_sco),
   * since a seeded institution's/group's id is a fresh random UUID at each run and cannot be
   * hardcoded in the fixture.
   */
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
