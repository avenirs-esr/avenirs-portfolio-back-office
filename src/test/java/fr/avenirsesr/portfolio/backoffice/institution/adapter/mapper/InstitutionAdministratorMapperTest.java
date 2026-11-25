package fr.avenirsesr.portfolio.backoffice.institution.adapter.mapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionAdministrator;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.mapper.InstitutionAdministratorMapper;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.mapper.InstitutionMapper;
import fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.model.InstitutionAdministratorEntity;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class InstitutionAdministratorMapperTest {
  private final InstitutionAdministratorMapper mapper = InstitutionAdministratorMapper.INSTANCE;

  @Nested
  class WhenMappingFromDomain {
    @Test
    void shouldCarryEveryFieldToTheEntity() {
      BddLogger.given("an administrator assignment in the domain");
      UUID id = UUID.randomUUID();
      UUID externalUserId = UUID.randomUUID();
      Institution institution = anInstitution(UUID.randomUUID());
      Instant createdAt = Instant.parse("2025-01-10T09:30:00Z");
      UUID createdBy = UUID.randomUUID();

      InstitutionAdministrator administrator =
          InstitutionAdministrator.toDomain(id, externalUserId, institution, createdAt, createdBy);

      BddLogger.when("mapping it to an entity");
      InstitutionAdministratorEntity entity = mapper.fromDomain(administrator);

      BddLogger.then("every field is carried over and the external user stays a raw id");
      assertThat(entity.getId()).isEqualTo(id);
      assertThat(entity.getCreatedBy()).isEqualTo(createdBy);
      assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
      assertThat(entity.getExternalUserId()).isEqualTo(externalUserId);
      assertThat(entity.getInstitution().getId()).isEqualTo(institution.getId());
    }
  }

  @Nested
  class WhenMappingToDomain {
    @Test
    void shouldCarryEveryFieldToTheDomain() {
      BddLogger.given("an administrator assignment entity");
      UUID id = UUID.randomUUID();
      UUID externalUserId = UUID.randomUUID();
      Institution institution = anInstitution(UUID.randomUUID());
      Instant createdAt = Instant.parse("2025-02-20T14:00:00Z");
      UUID createdBy = UUID.randomUUID();

      InstitutionAdministratorEntity entity =
          InstitutionAdministratorEntity.of(
              id,
              externalUserId,
              InstitutionMapper.INSTANCE.fromDomain(institution),
              createdBy,
              createdAt,
              createdAt);

      BddLogger.when("mapping it to the domain");
      InstitutionAdministrator domain = mapper.toDomain(entity);

      BddLogger.then("every field is carried over and the institution is rebuilt");
      assertThat(domain.getId()).isEqualTo(id);
      assertThat(domain.getCreatedBy()).isEqualTo(createdBy);
      assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
      assertThat(domain.getExternalUserId()).isEqualTo(externalUserId);
      assertThat(domain.getInstitution().getId()).isEqualTo(institution.getId());
    }
  }

  private Institution anInstitution(UUID id) {
    return Institution.toDomain(
        id,
        "Université de Rennes",
        "UR",
        "0350001A",
        "13000550100015",
        EInstitutionType.PRIMARY,
        null,
        Instant.now(),
        Instant.now());
  }
}
