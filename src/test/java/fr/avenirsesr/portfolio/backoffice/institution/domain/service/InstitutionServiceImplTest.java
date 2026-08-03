package fr.avenirsesr.portfolio.backoffice.institution.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionParentMustBePrimaryException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionPrimaryCannotHaveParentException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionSecondaryRequiresParentException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionData;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.InstitutionImportSummary;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.enums.EInstitutionType;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceImplTest {

  @Mock private InstitutionRepository institutionRepository;

  @InjectMocks private InstitutionServiceImpl service;

  @Test
  void shouldUpdateInstitution_whenCreating_withAlreadyUsedHai() {
    BddLogger.given("an existing institution registered under a hai");
    String hai = "0350001A";
    Institution existing =
        Institution.create(
            UUID.randomUUID(),
            "Université de Rennes",
            hai,
            "siret",
            "siren",
            EInstitutionType.PRIMARY,
            null);
    when(institutionRepository.findByHai(hai)).thenReturn(Optional.of(existing));
    when(institutionRepository.save(any(Institution.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("creating an institution with an already used hai");
    Institution result =
        service.create(
            "Université de Rennes - Renamed",
            hai,
            "new-siret",
            "new-siren",
            EInstitutionType.PRIMARY,
            null);

    BddLogger.then("it should update and save the existing institution instead of failing");
    assertEquals("Université de Rennes - Renamed", result.getName());
    assertEquals("new-siret", result.getSiret());
    verify(institutionRepository, never()).existsByHai(any());
    verify(institutionRepository).save(existing);
  }

  @Test
  void shouldCreatePrimaryInstitution_whenParentHaiIsNull() {
    BddLogger.given("an InstitutionServiceImpl service");
    String hai = "0350001A";
    when(institutionRepository.findByHai(hai)).thenReturn(Optional.empty());
    when(institutionRepository.save(any(Institution.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("creating a primary institution without a parent");
    Institution result =
        service.create(
            "Université de Rennes", hai, "siret", "siren", EInstitutionType.PRIMARY, null);

    BddLogger.then("it should save the institution without a parent");
    assertNotNull(result);
    assertTrue(result.getParent().isEmpty());
    verify(institutionRepository).save(any(Institution.class));
  }

  @Test
  void shouldThrowInstitutionPrimaryCannotHaveParentException_whenCreatingPrimary_withParentHai() {
    BddLogger.given("an InstitutionServiceImpl service");
    String hai = "0350001A";
    when(institutionRepository.findByHai(hai)).thenReturn(Optional.empty());

    BddLogger.when("creating a primary institution with a parent hai");
    assertThrows(
        InstitutionPrimaryCannotHaveParentException.class,
        () ->
            service.create(
                "Université de Rennes",
                hai,
                "siret",
                "siren",
                EInstitutionType.PRIMARY,
                "0330001C"));

    BddLogger.then("it should not save anything");
    verify(institutionRepository, never()).save(any());
  }

  @Test
  void
      shouldThrowInstitutionSecondaryRequiresParentException_whenCreatingSecondary_withoutParentHai() {
    BddLogger.given("an InstitutionServiceImpl service");
    String hai = "0350002B";
    when(institutionRepository.findByHai(hai)).thenReturn(Optional.empty());

    BddLogger.when("creating a secondary institution without a parent hai");
    assertThrows(
        InstitutionSecondaryRequiresParentException.class,
        () ->
            service.create(
                "Université de Rennes - IUT",
                hai,
                "siret",
                "siren",
                EInstitutionType.SECONDARY,
                null));

    BddLogger.then("it should not save anything");
    verify(institutionRepository, never()).save(any());
  }

  @Test
  void shouldThrowInstitutionNotFoundException_whenCreatingSecondary_withUnknownParentHai() {
    BddLogger.given("an InstitutionServiceImpl service");
    String hai = "0350002B";
    String parentHai = "0350001A";
    when(institutionRepository.findByHai(hai)).thenReturn(Optional.empty());
    when(institutionRepository.findByHai(parentHai)).thenReturn(Optional.empty());

    BddLogger.when("creating a secondary institution with an unknown parent hai");
    assertThrows(
        InstitutionNotFoundException.class,
        () ->
            service.create(
                "Université de Rennes - IUT",
                hai,
                "siret",
                "siren",
                EInstitutionType.SECONDARY,
                parentHai));

    BddLogger.then("it should not save anything");
    verify(institutionRepository, never()).save(any());
  }

  @Test
  void
      shouldThrowInstitutionParentMustBePrimaryException_whenCreatingSecondary_withSecondaryParent() {
    BddLogger.given("an InstitutionServiceImpl service");
    String hai = "0350002B";
    String parentHai = "0350003C";
    Institution secondaryParent =
        Institution.create(
            UUID.randomUUID(),
            "Autre IUT",
            parentHai,
            "siret",
            "siren",
            EInstitutionType.SECONDARY,
            null);
    when(institutionRepository.findByHai(hai)).thenReturn(Optional.empty());
    when(institutionRepository.findByHai(parentHai)).thenReturn(Optional.of(secondaryParent));

    BddLogger.when("creating a secondary institution attached to a secondary parent");
    assertThrows(
        InstitutionParentMustBePrimaryException.class,
        () ->
            service.create(
                "Université de Rennes - IUT",
                hai,
                "siret",
                "siren",
                EInstitutionType.SECONDARY,
                parentHai));

    BddLogger.then("it should not save anything");
    verify(institutionRepository, never()).save(any());
  }

  @Test
  void shouldCreateSecondaryInstitution_whenParentIsPrimary() {
    BddLogger.given("an InstitutionServiceImpl service");
    String hai = "0350002B";
    String parentHai = "0350001A";
    Institution primaryParent =
        Institution.create(
            UUID.randomUUID(),
            "Université de Rennes",
            parentHai,
            "siret",
            "siren",
            EInstitutionType.PRIMARY,
            null);
    when(institutionRepository.findByHai(hai)).thenReturn(Optional.empty());
    when(institutionRepository.findByHai(parentHai)).thenReturn(Optional.of(primaryParent));
    when(institutionRepository.save(any(Institution.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("creating a secondary institution attached to a primary parent");
    Institution result =
        service.create(
            "Université de Rennes - IUT",
            hai,
            "siret",
            "siren",
            EInstitutionType.SECONDARY,
            parentHai);

    BddLogger.then("it should save the institution with the resolved parent");
    assertNotNull(result);
    assertSame(primaryParent, result.getParent().orElseThrow());
    verify(institutionRepository).save(any(Institution.class));
  }

  @Test
  void shouldCreateAllInstitutions_whenBatchIsValid() {
    BddLogger.given("an InstitutionServiceImpl service");
    when(institutionRepository.findByHai(any())).thenReturn(Optional.empty());
    when(institutionRepository.save(any(Institution.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("creating a batch of primary institutions");
    InstitutionImportSummary result =
        service.createAll(
            List.of(
                new InstitutionData(
                    "Université de Rennes",
                    "0350001A",
                    "siret",
                    "siren",
                    EInstitutionType.PRIMARY,
                    null),
                new InstitutionData(
                    "Université de Bordeaux",
                    "0330001C",
                    "siret",
                    "siren",
                    EInstitutionType.PRIMARY,
                    null)));

    BddLogger.then("it should report every institution of the batch as created");
    assertEquals(2, result.created().size());
    assertEquals(0, result.updated().size());
    assertEquals(0, result.failed().size());
    verify(institutionRepository, times(2)).save(any(Institution.class));
  }

  @Test
  void shouldUpdateInstitution_whenCreatingAllInstitutions_withAlreadyUsedHai() {
    BddLogger.given("an existing institution registered under a hai");
    String hai = "0350001A";
    Institution existing =
        Institution.create(
            UUID.randomUUID(),
            "Université de Rennes",
            hai,
            "siret",
            "siren",
            EInstitutionType.PRIMARY,
            null);
    when(institutionRepository.findByHai(hai)).thenReturn(Optional.of(existing));
    when(institutionRepository.save(any(Institution.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("creating a batch containing that hai");
    InstitutionImportSummary result =
        service.createAll(
            List.of(
                new InstitutionData(
                    "Université de Rennes - Renamed",
                    hai,
                    "new-siret",
                    "new-siren",
                    EInstitutionType.PRIMARY,
                    null)));

    BddLogger.then("it should report the institution as updated instead of created");
    assertEquals(0, result.created().size());
    assertEquals(1, result.updated().size());
    assertEquals(0, result.failed().size());
    assertEquals("Université de Rennes - Renamed", result.updated().get(0).getName());
  }

  @Test
  void shouldContinueBatch_whenOneInstitutionFails() {
    BddLogger.given("a batch with one secondary institution missing its parent hai");
    when(institutionRepository.findByHai("0350001A")).thenReturn(Optional.empty());
    when(institutionRepository.findByHai("0350002B")).thenReturn(Optional.empty());
    when(institutionRepository.save(any(Institution.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("creating the batch");
    InstitutionImportSummary result =
        service.createAll(
            List.of(
                new InstitutionData(
                    "Université de Rennes",
                    "0350001A",
                    "siret",
                    "siren",
                    EInstitutionType.PRIMARY,
                    null),
                new InstitutionData(
                    "Université de Rennes - IUT",
                    "0350002B",
                    "siret",
                    "siren",
                    EInstitutionType.SECONDARY,
                    null)));

    BddLogger.then("it should save the valid institution and report the other as failed");
    assertEquals(1, result.created().size());
    assertEquals(0, result.updated().size());
    assertEquals(1, result.failed().size());
    assertEquals("0350002B", result.failed().get(0).hai());
    verify(institutionRepository, times(1)).save(any(Institution.class));
  }

  @Test
  void shouldUpdateInstitution_whenHaiExists() {
    BddLogger.given("an existing institution");
    String hai = "0350001A";
    Institution existing =
        Institution.create(
            UUID.randomUUID(),
            "Université de Rennes",
            hai,
            "siret",
            "siren",
            EInstitutionType.PRIMARY,
            null);
    when(institutionRepository.findByHai(hai)).thenReturn(Optional.of(existing));
    when(institutionRepository.save(any(Institution.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("updating the institution");
    Institution result =
        service.update(
            hai,
            "Université de Rennes - Renamed",
            "new-siret",
            "new-siren",
            EInstitutionType.PRIMARY,
            null);

    BddLogger.then("it should update and save the institution");
    assertEquals("Université de Rennes - Renamed", result.getName());
    assertEquals("new-siret", result.getSiret());
    assertEquals("new-siren", result.getSiren());
    verify(institutionRepository).save(existing);
  }

  @Test
  void shouldThrowInstitutionNotFoundException_whenUpdating_withUnknownHai() {
    BddLogger.given("an InstitutionServiceImpl service");
    String hai = "unknown-hai";
    when(institutionRepository.findByHai(hai)).thenReturn(Optional.empty());

    BddLogger.when("updating an institution with an unknown hai");
    assertThrows(
        InstitutionNotFoundException.class,
        () -> service.update(hai, "name", "siret", "siren", EInstitutionType.PRIMARY, null));

    BddLogger.then("it should not save anything");
    verify(institutionRepository, never()).save(any());
  }

  @Test
  void shouldReturnAllInstitutions() {
    BddLogger.given("institutions in the repository");
    Institution institution =
        Institution.create(
            UUID.randomUUID(),
            "Université de Rennes",
            "0350001A",
            "siret",
            "siren",
            EInstitutionType.PRIMARY,
            null);
    when(institutionRepository.findAll(null, null)).thenReturn(List.of(institution));

    BddLogger.when("fetching all institutions");
    List<Institution> result = service.findAll(null, null);

    BddLogger.then("it should return every institution");
    assertEquals(List.of(institution), result);
  }

  @Test
  void shouldReturnInstitution_whenIdExists() {
    BddLogger.given("an existing institution");
    UUID id = UUID.randomUUID();
    Institution institution =
        Institution.create(
            id,
            "Université de Rennes",
            "0350001A",
            "siret",
            "siren",
            EInstitutionType.PRIMARY,
            null);
    when(institutionRepository.findById(id)).thenReturn(Optional.of(institution));

    BddLogger.when("fetching the institution by id");
    Institution result = service.findById(id);

    BddLogger.then("it should return the institution");
    assertSame(institution, result);
  }

  @Test
  void shouldThrowInstitutionNotFoundException_whenFindingById_withUnknownId() {
    BddLogger.given("an unknown institution id");
    UUID id = UUID.randomUUID();
    when(institutionRepository.findById(id)).thenReturn(Optional.empty());

    BddLogger.when("fetching the institution by id");
    assertThrows(InstitutionNotFoundException.class, () -> service.findById(id));

    BddLogger.then("it should throw InstitutionNotFoundException");
  }

  @Test
  void shouldDeleteInstitution_whenIdExists() {
    BddLogger.given("an existing institution");
    UUID id = UUID.randomUUID();
    Institution institution =
        Institution.create(
            id,
            "Université de Rennes",
            "0350001A",
            "siret",
            "siren",
            EInstitutionType.PRIMARY,
            null);
    when(institutionRepository.findById(id)).thenReturn(Optional.of(institution));

    BddLogger.when("deleting the institution");
    service.delete(id);

    BddLogger.then("it should remove the institution from the database");
    verify(institutionRepository).removeFromDatabase(institution);
  }
}
