package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.seeder;

import com.fasterxml.jackson.core.type.TypeReference;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.enums.EGroupType;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.input.GroupService;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.seeder.data.GroupCreationData;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.seeder.data.GroupCsvCreationData;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.seeder.fake.FakeGroup;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.adapter.data.ESeederSource;
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
public class GroupSeeder {

  private static final String PATH_FILE = "seeder/groups.json";
  private static final int PROGRAMS_PER_INSTITUTION_NB = 2;
  private static final int OPTIONS_PER_PROGRAM_NB = 2;

  private final FileReader fileReader;

  private final GroupService groupService;

  private final InstitutionRepository institutionRepository;

  @Value("${seeder.source}")
  private ESeederSource seederSource;

  @Transactional
  public List<UUID> seed(List<UUID> savedInstitutionIds) {
    if (seederSource == ESeederSource.FAKER) {
      ValidationUtils.requireNonEmpty(savedInstitutionIds, "savedInstitutionIds cannot be empty");
    }

    log.info("Seeding groups...");

    List<GroupCreationData> creationDataList =
        switch (seederSource) {
          case CSV -> readGroupCreationDataFromFile();
          case FAKER -> buildFakeGroups(savedInstitutionIds);
        };

    List<UUID> savedGroupIds = new ArrayList<>();

    for (EGroupType type :
        List.of(EGroupType.PROGRAM, EGroupType.PROGRAM_OPTION, EGroupType.STUDENT_GROUP)) {
      creationDataList.stream()
          .filter(data -> data.type() == type)
          .forEach(
              data -> {
                Group group =
                    groupService.create(
                        data.name(),
                        data.idSiSco(),
                        data.institutionId(),
                        data.codeSise(),
                        data.startDate(),
                        data.endDate(),
                        data.type(),
                        data.parentIdSiSco());
                savedGroupIds.add(group.getId());
              });
    }

    log.info("✔ {} groups created", savedGroupIds.size());
    return List.copyOf(savedGroupIds);
  }

  /**
   * The CSV fixture references institutions by hai (a stable business key), since a seeded
   * institution's id is a fresh random UUID at each run and cannot be hardcoded in the fixture.
   */
  private List<GroupCreationData> readGroupCreationDataFromFile() {
    List<GroupCsvCreationData> csvCreationDataList =
        fileReader.readJSON(PATH_FILE, new TypeReference<>() {});

    return csvCreationDataList.stream()
        .map(
            data ->
                new GroupCreationData(
                    data.name(),
                    data.idSiSco(),
                    resolveInstitutionId(data.institutionHai()),
                    data.codeSise(),
                    data.startDate(),
                    data.endDate(),
                    data.type(),
                    data.parentIdSiSco()))
        .toList();
  }

  private UUID resolveInstitutionId(String institutionHai) {
    return institutionRepository
        .findByHai(institutionHai)
        .orElseThrow(InstitutionNotFoundException::new)
        .getId();
  }

  private List<GroupCreationData> buildFakeGroups(List<UUID> savedInstitutionIds) {
    List<GroupCreationData> creationDataList = new ArrayList<>();

    savedInstitutionIds.forEach(
        institutionId ->
            IntStream.range(0, PROGRAMS_PER_INSTITUTION_NB)
                .forEach(
                    i -> {
                      GroupCreationData program = FakeGroup.program(institutionId).toCreationData();
                      creationDataList.add(program);
                      creationDataList.add(
                          FakeGroup.studentGroup(institutionId, program.idSiSco())
                              .toCreationData());

                      IntStream.range(0, OPTIONS_PER_PROGRAM_NB)
                          .forEach(
                              j -> {
                                GroupCreationData option =
                                    FakeGroup.programOption(institutionId, program.idSiSco())
                                        .toCreationData();
                                creationDataList.add(option);
                                creationDataList.add(
                                    FakeGroup.studentGroup(institutionId, option.idSiSco())
                                        .toCreationData());
                              });
                    }));

    return creationDataList;
  }
}
