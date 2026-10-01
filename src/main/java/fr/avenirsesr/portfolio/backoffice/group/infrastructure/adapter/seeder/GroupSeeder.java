package fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.seeder;

import com.fasterxml.jackson.core.type.TypeReference;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.input.GroupService;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.seeder.data.GroupCreationData;
import fr.avenirsesr.portfolio.backoffice.group.infrastructure.adapter.seeder.fake.FakeGroup;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
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
                        data.idSISco(),
                        data.institutionUAI(),
                        data.codeSise(),
                        data.startDate(),
                        data.endDate(),
                        data.type(),
                        data.parentIdSISco());
                savedGroupIds.add(group.getId());
              });
    }

    log.info("✔ {} groups created", savedGroupIds.size());
    return List.copyOf(savedGroupIds);
  }

  private List<GroupCreationData> readGroupCreationDataFromFile() {
    return fileReader.readJSON(PATH_FILE, new TypeReference<>() {});
  }

  private List<GroupCreationData> buildFakeGroups(List<UUID> savedInstitutionIds) {
    List<GroupCreationData> creationDataList = new ArrayList<>();

    savedInstitutionIds.stream()
        .map(this::resolveInstitutionUAI)
        .forEach(
            institutionUAI ->
                IntStream.range(0, PROGRAMS_PER_INSTITUTION_NB)
                    .forEach(
                        i -> {
                          GroupCreationData program =
                              FakeGroup.program(institutionUAI).toCreationData();
                          creationDataList.add(program);
                          creationDataList.add(
                              FakeGroup.studentGroup(institutionUAI, program.idSISco())
                                  .toCreationData());

                          IntStream.range(0, OPTIONS_PER_PROGRAM_NB)
                              .forEach(
                                  j -> {
                                    GroupCreationData option =
                                        FakeGroup.programOption(institutionUAI, program.idSISco())
                                            .toCreationData();
                                    creationDataList.add(option);
                                    creationDataList.add(
                                        FakeGroup.studentGroup(institutionUAI, option.idSISco())
                                            .toCreationData());
                                  });
                        }));

    return creationDataList;
  }

  private String resolveInstitutionUAI(UUID institutionId) {
    return institutionRepository
        .findById(institutionId)
        .orElseThrow(InstitutionNotFoundException::new)
        .getUai();
  }
}
