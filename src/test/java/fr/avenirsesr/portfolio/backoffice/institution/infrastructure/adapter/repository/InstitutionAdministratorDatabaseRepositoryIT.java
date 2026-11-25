package fr.avenirsesr.portfolio.backoffice.institution.infrastructure.adapter.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.avenirsesr.portfolio.backoffice.ContainerConfigurationTest;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionAdministrator;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionAdministratorRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.infrastructure.adapter.seeder.SeederRunner;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class InstitutionAdministratorDatabaseRepositoryIT extends ContainerConfigurationTest {

  private static final String RENNES_UAI = "0350001A";
  private static final String RENNES_IUT_UAI = "0350002B";
  private static final String BORDEAUX_UAI = "0330001C";
  private static final String EPPN = "lucas.tessier@university.com";
  @Autowired private InstitutionAdministratorRepository repository;
  @Autowired private InstitutionRepository institutionRepository;
  @Autowired private EntityManager entityManager;

  private UUID userId;
  private Institution institution;

  @BeforeAll
  void setup(@Autowired SeederRunner seederRunner) {
    seederRunner.run();
  }

  @BeforeEach
  void init() {
    institution = seededInstitution(RENNES_UAI);
    userId = savedExternalUserId();
  }

  private Institution seededInstitution(String uai) {
    return institutionRepository
        .findByUai(uai)
        .orElseThrow(() -> new IllegalStateException("Institution non seedée pour l'UAI " + uai));
  }

  private UUID savedExternalUserId() {
    ExternalUser user =
        ExternalUser.create(
            EPPN,
            "PEG-0001",
            EExternalSource.BACK_OFFICE,
            "0350001A",
            Set.of(EUserCategory.STAFF),
            EPPN,
            "Lucas",
            "Tessier",
            EUserStatus.ACTIVE);

    return user.getId();
  }

  @Test
  void save_then_existsByUserAndInstitution_returnsTrue() {
    repository.save(InstitutionAdministrator.create(userId, institution, userId));

    assertThat(repository.existsByUserAndInstitution(userId, institution.getId())).isTrue();
  }

  @Test
  void existsByUserAndInstitution_isFalse_whenNoAffectation() {
    assertThat(repository.existsByUserAndInstitution(userId, institution.getId())).isFalse();
  }

  @Test
  void duplicate_userInstitution_isRejected() {
    repository.save(InstitutionAdministrator.create(userId, institution, userId));

    assertThatThrownBy(
            () -> {
              repository.save(InstitutionAdministrator.create(userId, institution, userId));
              entityManager.flush();
            })
        .isInstanceOf(PersistenceException.class);
  }

  @Test
  void deleteByUserAndInstitution_removesAffectation() {
    repository.save(InstitutionAdministrator.create(userId, institution, userId));

    repository.deleteByUserAndInstitution(userId, institution.getId());

    assertThat(repository.existsByUserAndInstitution(userId, institution.getId())).isFalse();
  }

  @Test
  void findInstitutionIdsByUser_returnsAdministeredInstitutions() {
    Institution rennes = seededInstitution(RENNES_UAI);
    Institution bordeaux = seededInstitution(BORDEAUX_UAI);
    UUID adminId = savedExternalUserId();
    repository.save(InstitutionAdministrator.create(adminId, rennes, adminId));
    repository.save(InstitutionAdministrator.create(adminId, bordeaux, adminId));

    List<UUID> ids = repository.findInstitutionIdsByUser(adminId);
    assertThat(ids).containsExactlyInAnyOrder(rennes.getId(), bordeaux.getId());
  }

  @Test
  void findByInstitution_returnsAffectationsOfInstitution() {

    Institution iut = seededInstitution(RENNES_IUT_UAI);
    UUID first = savedExternalUserId();
    UUID second = savedExternalUserId();
    repository.save(InstitutionAdministrator.create(first, iut, first));
    repository.save(InstitutionAdministrator.create(second, iut, second));

    List<InstitutionAdministrator> result = repository.findByInstitution(iut.getId());

    assertThat(result)
        .extracting(InstitutionAdministrator::getExternalUserId)
        .containsExactlyInAnyOrder(first, second);
  }
}
