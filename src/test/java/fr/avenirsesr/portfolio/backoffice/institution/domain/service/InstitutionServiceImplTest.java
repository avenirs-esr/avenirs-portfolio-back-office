package fr.avenirsesr.portfolio.backoffice.institution.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionHaiAlreadyExistsException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionParentMustBePrimaryException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionPrimaryCannotHaveParentException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionSecondaryRequiresParentException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.common.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
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
  void shouldThrowInstitutionHaiAlreadyExistsException_whenCreating_withAlreadyUsedHai() {
    BddLogger.given("an InstitutionServiceImpl service");
    String hai = "0350001A";
    when(institutionRepository.existsByHai(hai)).thenReturn(true);

    BddLogger.when("creating an institution with an already used hai");
    assertThrows(
        InstitutionHaiAlreadyExistsException.class,
        () ->
            service.create(
                "Université de Rennes", hai, "siret", "siren", EInstitutionType.PRIMARY, null));

    BddLogger.then("it should not save anything");
    verify(institutionRepository, never()).save(any());
  }

  @Test
  void shouldCreatePrimaryInstitution_whenParentHaiIsNull() {
    BddLogger.given("an InstitutionServiceImpl service");
    String hai = "0350001A";
    when(institutionRepository.existsByHai(hai)).thenReturn(false);
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
    when(institutionRepository.existsByHai(hai)).thenReturn(false);

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
    when(institutionRepository.existsByHai(hai)).thenReturn(false);

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
    when(institutionRepository.existsByHai(hai)).thenReturn(false);
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
    when(institutionRepository.existsByHai(hai)).thenReturn(false);
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
    when(institutionRepository.existsByHai(hai)).thenReturn(false);
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
}
