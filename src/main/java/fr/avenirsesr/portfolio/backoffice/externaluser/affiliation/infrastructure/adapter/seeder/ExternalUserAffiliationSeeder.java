package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.seeder;

import com.fasterxml.jackson.core.type.TypeReference;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.infrastructure.adapter.seeder.data.ExternalUserAffiliationCreationData;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupNotFoundException;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.seeder.domain.port.output.SharedDataGenerator;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.adapter.data.DataGeneratorProvider;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.adapter.data.ESeederSource;
import fr.avenirsesr.portfolio.common.user.domain.exceptions.ExternalUserNotFoundException;
import fr.avenirsesr.portfolio.common.utils.FileReader;
import fr.avenirsesr.portfolio.common.validation.infrastructure.adapter.utils.ValidationUtils;
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
public class ExternalUserAffiliationSeeder {

  private static final DataGeneratorProvider<SharedDataGenerator> dataGenerator =
      new DataGeneratorProvider<SharedDataGenerator>()
          .init(ExternalUserAffiliationSeeder.class, SharedDataGenerator.class);

  private static final String PATH_FILE = "seeder/external-user-affiliations.json";

  private final FileReader fileReader;
  private final ExternalUserAffiliationService externalUserAffiliationService;
  private final ExternalUserRepository externalUserRepository;
  private final InstitutionRepository institutionRepository;
  private final GroupRepository groupRepository;

  @Value("${seeder.source}")
  private ESeederSource seederSource;

  @Transactional
  public void seed(
      List<ExternalUser> savedExternalUsers,
      List<UUID> savedInstitutionIds,
      List<UUID> savedGroupIds) {
    log.info("Seeding External User Affiliations...");

    int count =
        switch (seederSource) {
          case CSV -> seedFromFixture();
          case FAKER -> seedFake(savedExternalUsers, savedInstitutionIds, savedGroupIds);
        };

    log.info("✔ {} external user affiliations synced", count);
  }

  private int seedFromFixture() {
    List<ExternalUserAffiliationCreationData> creationData =
        fileReader.readJSON(
            PATH_FILE, new TypeReference<List<ExternalUserAffiliationCreationData>>() {});

    int count = 0;
    for (ExternalUserAffiliationCreationData data : creationData) {
      UUID externalUserId = resolveExternalUserId(data.eppn());

      for (String institutionHai : data.institutionHais()) {
        externalUserAffiliationService.addAffiliation(
            externalUserId, resolveInstitutionId(institutionHai), null, data.category());
        count++;
      }

      for (String groupIdSiSco : data.groupIdSiScos()) {
        Group group = resolveGroup(groupIdSiSco);
        externalUserAffiliationService.addAffiliation(
            externalUserId, group.getInstitution().getId(), group.getId(), data.category());
        count++;
      }
    }

    return count;
  }

  private int seedFake(
      List<ExternalUser> savedExternalUsers,
      List<UUID> savedInstitutionIds,
      List<UUID> savedGroupIds) {
    ValidationUtils.requireNonEmpty(savedInstitutionIds, "savedInstitutionIds cannot be empty");

    int count = 0;
    for (ExternalUser externalUser : savedExternalUsers) {
      if (externalUser.getCategories().contains(EUserCategory.STUDENT)) {
        count += seedFakeStudentAffiliation(externalUser, savedGroupIds, savedInstitutionIds);
      }
      if (externalUser.getCategories().contains(EUserCategory.STAFF)) {
        count += seedFakeStaffAffiliations(externalUser, savedInstitutionIds);
      }
    }

    return count;
  }

  private int seedFakeStudentAffiliation(
      ExternalUser externalUser, List<UUID> savedGroupIds, List<UUID> savedInstitutionIds) {
    if (savedGroupIds.isEmpty()) {
      UUID institutionId = dataGenerator.with("institution").pickIn(savedInstitutionIds);
      externalUserAffiliationService.addAffiliation(
          externalUser.getId(), institutionId, null, EUserCategory.STUDENT);
      return 1;
    }

    UUID groupId = dataGenerator.with("group").pickIn(savedGroupIds);
    Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);
    externalUserAffiliationService.addAffiliation(
        externalUser.getId(), group.getInstitution().getId(), groupId, EUserCategory.STUDENT);
    return 1;
  }

  private int seedFakeStaffAffiliations(ExternalUser externalUser, List<UUID> savedInstitutionIds) {
    int count = 0;
    UUID institutionId = dataGenerator.with("institution").pickIn(savedInstitutionIds);
    externalUserAffiliationService.addAffiliation(
        externalUser.getId(), institutionId, null, EUserCategory.STAFF);
    count++;

    List<UUID> otherInstitutionIds =
        savedInstitutionIds.stream().filter(id -> !id.equals(institutionId)).toList();
    if (!otherInstitutionIds.isEmpty() && dataGenerator.with("multiAffiliation").bool()) {
      UUID secondInstitutionId =
          dataGenerator.with("secondInstitution").pickIn(otherInstitutionIds);
      externalUserAffiliationService.addAffiliation(
          externalUser.getId(), secondInstitutionId, null, EUserCategory.STAFF);
      count++;
    }

    return count;
  }

  private UUID resolveExternalUserId(String eppn) {
    return externalUserRepository
        .findByEppn(eppn)
        .orElseThrow(ExternalUserNotFoundException::new)
        .getId();
  }

  private UUID resolveInstitutionId(String institutionHai) {
    return institutionRepository
        .findByHai(institutionHai)
        .orElseThrow(InstitutionNotFoundException::new)
        .getId();
  }

  private Group resolveGroup(String groupIdSiSco) {
    return groupRepository.findByIdSiSco(groupIdSiSco).orElseThrow(GroupNotFoundException::new);
  }
}
