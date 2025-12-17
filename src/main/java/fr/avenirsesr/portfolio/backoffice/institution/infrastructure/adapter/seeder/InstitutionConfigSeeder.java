package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.seeder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionConfigService;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.seeder.data.InstitutionConfigCreationData;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.seeder.fake.FakeInstitutionConfig;
import fr.avenirsesr.portfolio.common.seeder.infrastructure.adapter.data.ESeederSource;
import fr.avenirsesr.portfolio.common.validation.infrastructure.adapter.utils.ValidationUtils;
import java.io.InputStream;
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
public class InstitutionConfigSeeder {

  private static final String PATH_FILE = "seeder/institution-configs.json";

  private final ObjectMapper objectMapper;

  private final InstitutionConfigService institutionConfigService;

  @Value("${seeder.source}")
  private ESeederSource seederSource;

  @Transactional
  public List<InstitutionConfigCreationData> seed(List<UUID> savedInstitutions) {
    if (seederSource.equals(ESeederSource.FAKER)) {
      ValidationUtils.requireNonEmpty(savedInstitutions, "savedInstitutions cannot be empty");
    }

    log.info("Seeding institution configs...");

    List<InstitutionConfigCreationData> creationDataList =
        switch (seederSource) {
          case CSV -> readInstitutionConfigCreationDataFromFile();
          case FAKER ->
              savedInstitutions.stream()
                  .map(FakeInstitutionConfig::of)
                  .map(FakeInstitutionConfig::toEntity)
                  .map(
                      entity ->
                          new InstitutionConfigCreationData(
                              entity.getInstitutionId(),
                              entity.isApcEnabled(),
                              entity.isLifeProjectEnabled()))
                  .toList();
        };

    creationDataList.forEach(
        creationData -> {
          institutionConfigService.create(
              creationData.institutionId(),
              creationData.apcEnabled(),
              creationData.lifeProjectEnabled());
        });

    log.info("✔ {} institution configs created", creationDataList.size());
    return creationDataList;
  }

  private List<InstitutionConfigCreationData> readInstitutionConfigCreationDataFromFile() {
    try (InputStream is = getClass().getClassLoader().getResourceAsStream(PATH_FILE)) {
      if (is == null) {
        throw new IllegalStateException("File not found: " + PATH_FILE);
      }
      return objectMapper.readValue(is, new TypeReference<>() {});
    } catch (Exception e) {
      throw new RuntimeException("Error while reading " + PATH_FILE, e);
    }
  }
}
