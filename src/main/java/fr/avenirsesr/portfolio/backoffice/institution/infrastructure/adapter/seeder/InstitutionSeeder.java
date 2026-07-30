package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.seeder;

import com.fasterxml.jackson.core.type.TypeReference;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionService;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.seeder.data.InstitutionCreationData;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.seeder.fake.FakeInstitution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.enums.EInstitutionType;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.adapter.data.ESeederSource;
import fr.avenirsesr.portfolio.common.utils.FileReader;
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
public class InstitutionSeeder {

  private static final String PATH_FILE = "seeder/institutions.json";
  private static final int PRIMARY_INSTITUTIONS_NB = 3;
  private static final int SECONDARY_INSTITUTIONS_PER_PRIMARY_NB = 2;

  private final FileReader fileReader;

  private final InstitutionService institutionService;

  @Value("${seeder.source}")
  private ESeederSource seederSource;

  @Transactional
  public List<UUID> seed() {
    log.info("Seeding institutions...");

    List<InstitutionCreationData> creationDataList =
        switch (seederSource) {
          case CSV -> fileReader.readJSON(PATH_FILE, new TypeReference<>() {});
          case FAKER -> buildFakeInstitutions();
        };

    List<UUID> savedInstitutionIds = new ArrayList<>();

    creationDataList.stream()
        .filter(data -> data.type() == EInstitutionType.PRIMARY)
        .forEach(
            data -> {
              Institution institution =
                  institutionService.create(
                      data.name(), data.hai(), data.siret(), data.siren(), data.type(), null);
              savedInstitutionIds.add(institution.getId());
            });

    creationDataList.stream()
        .filter(data -> data.type() == EInstitutionType.SECONDARY)
        .forEach(
            data -> {
              Institution institution =
                  institutionService.create(
                      data.name(),
                      data.hai(),
                      data.siret(),
                      data.siren(),
                      data.type(),
                      data.parentHai());
              savedInstitutionIds.add(institution.getId());
            });

    log.info("✔ {} institutions created", savedInstitutionIds.size());
    return List.copyOf(savedInstitutionIds);
  }

  private List<InstitutionCreationData> buildFakeInstitutions() {
    List<InstitutionCreationData> creationDataList = new ArrayList<>();

    IntStream.range(0, PRIMARY_INSTITUTIONS_NB)
        .forEach(
            i -> {
              InstitutionCreationData primary = FakeInstitution.primary().toCreationData();
              creationDataList.add(primary);
              IntStream.range(0, SECONDARY_INSTITUTIONS_PER_PRIMARY_NB)
                  .forEach(
                      j ->
                          creationDataList.add(
                              FakeInstitution.secondary(primary.hai()).toCreationData()));
            });

    return creationDataList;
  }
}
