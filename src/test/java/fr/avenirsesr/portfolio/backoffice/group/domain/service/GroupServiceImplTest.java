package fr.avenirsesr.portfolio.backoffice.group.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupIdSiScoAlreadyExistsException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupNotFoundException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupProgramCannotHaveParentException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupProgramOptionParentMustBeProgramException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupProgramOptionRequiresParentException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupStudentGroupParentMustBeProgramOrOptionException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupStudentGroupRequiresParentException;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.common.group.domain.model.Group;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import fr.avenirsesr.portfolio.common.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

  @Mock private GroupRepository groupRepository;

  @Mock private InstitutionRepository institutionRepository;

  @InjectMocks private GroupServiceImpl service;

  private final UUID institutionId = UUID.randomUUID();
  private final Institution institution =
      Institution.create(
          institutionId,
          "Universite de Rennes",
          "0350001A",
          "siret",
          "siren",
          EInstitutionType.PRIMARY,
          null);
  private final LocalDate startDate = LocalDate.of(2023, 9, 1);
  private final LocalDate endDate = LocalDate.of(2026, 8, 31);

  @Test
  void shouldCreateProgram_whenParentIdSiScoIsNull() {
    BddLogger.given("a GroupServiceImpl service");
    String idSiSco = "10000001";
    when(groupRepository.existsByIdSiSco(idSiSco)).thenReturn(false);
    when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
    when(groupRepository.save(any(Group.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("creating a program without a parent");
    Group result =
        service.create(
            "Licence Informatique",
            idSiSco,
            institutionId,
            "11000001",
            startDate,
            endDate,
            EGroupType.PROGRAM,
            null);

    BddLogger.then("it should save the program without a parent");
    assertNotNull(result);
    assertSame(institution, result.getInstitution());
    assertTrue(result.getParent().isEmpty());
    verify(groupRepository).save(any(Group.class));
  }

  @Test
  void shouldThrowInstitutionNotFoundException_whenCreating_withUnknownInstitutionId() {
    BddLogger.given("an unknown institution id");
    String idSiSco = "10000001";
    when(groupRepository.existsByIdSiSco(idSiSco)).thenReturn(false);
    when(institutionRepository.findById(institutionId)).thenReturn(Optional.empty());

    BddLogger.when("creating a group with an unknown institution id");
    assertThrows(
        InstitutionNotFoundException.class,
        () ->
            service.create(
                "Licence Informatique",
                idSiSco,
                institutionId,
                "11000001",
                startDate,
                endDate,
                EGroupType.PROGRAM,
                null));

    BddLogger.then("it should not save anything");
    verify(groupRepository, never()).save(any());
  }

  @Test
  void shouldThrowGroupProgramCannotHaveParentException_whenCreatingProgram_withParentIdSiSco() {
    BddLogger.given("a GroupServiceImpl service");
    String idSiSco = "10000001";
    when(groupRepository.existsByIdSiSco(idSiSco)).thenReturn(false);
    when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));

    BddLogger.when("creating a program with a parent id_si_sco");
    assertThrows(
        GroupProgramCannotHaveParentException.class,
        () ->
            service.create(
                "Licence Informatique",
                idSiSco,
                institutionId,
                "11000001",
                startDate,
                endDate,
                EGroupType.PROGRAM,
                "10000000"));

    BddLogger.then("it should not save anything");
    verify(groupRepository, never()).save(any());
  }

  @Test
  void
      shouldThrowGroupProgramOptionRequiresParentException_whenCreatingProgramOption_withoutParentIdSiSco() {
    BddLogger.given("a GroupServiceImpl service");
    String idSiSco = "10000002";
    when(groupRepository.existsByIdSiSco(idSiSco)).thenReturn(false);
    when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));

    BddLogger.when("creating a program option without a parent id_si_sco");
    assertThrows(
        GroupProgramOptionRequiresParentException.class,
        () ->
            service.create(
                "Parcours IA",
                idSiSco,
                institutionId,
                "11000002",
                startDate,
                endDate,
                EGroupType.PROGRAM_OPTION,
                null));

    BddLogger.then("it should not save anything");
    verify(groupRepository, never()).save(any());
  }

  @Test
  void shouldThrowGroupNotFoundException_whenCreatingProgramOption_withUnknownParentIdSiSco() {
    BddLogger.given("a GroupServiceImpl service");
    String idSiSco = "10000002";
    String parentIdSiSco = "10000001";
    when(groupRepository.existsByIdSiSco(idSiSco)).thenReturn(false);
    when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
    when(groupRepository.findByIdSiSco(parentIdSiSco)).thenReturn(Optional.empty());

    BddLogger.when("creating a program option with an unknown parent id_si_sco");
    assertThrows(
        GroupNotFoundException.class,
        () ->
            service.create(
                "Parcours IA",
                idSiSco,
                institutionId,
                "11000002",
                startDate,
                endDate,
                EGroupType.PROGRAM_OPTION,
                parentIdSiSco));

    BddLogger.then("it should not save anything");
    verify(groupRepository, never()).save(any());
  }

  @Test
  void
      shouldThrowGroupProgramOptionParentMustBeProgramException_whenCreatingProgramOption_withNonProgramParent() {
    BddLogger.given("a program option and a student group parent");
    String idSiSco = "10000002";
    String parentIdSiSco = "10000003";
    Group studentGroupParent =
        Group.create(
            UUID.randomUUID(),
            "Groupe A",
            parentIdSiSco,
            institution,
            "11000003",
            startDate,
            endDate,
            EGroupType.STUDENT_GROUP,
            null);
    when(groupRepository.existsByIdSiSco(idSiSco)).thenReturn(false);
    when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
    when(groupRepository.findByIdSiSco(parentIdSiSco)).thenReturn(Optional.of(studentGroupParent));

    BddLogger.when("creating a program option attached to a student group");
    assertThrows(
        GroupProgramOptionParentMustBeProgramException.class,
        () ->
            service.create(
                "Parcours IA",
                idSiSco,
                institutionId,
                "11000002",
                startDate,
                endDate,
                EGroupType.PROGRAM_OPTION,
                parentIdSiSco));

    BddLogger.then("it should not save anything");
    verify(groupRepository, never()).save(any());
  }

  @Test
  void shouldCreateProgramOption_whenParentIsProgram() {
    BddLogger.given("a program parent");
    String idSiSco = "10000002";
    String parentIdSiSco = "10000001";
    Group programParent =
        Group.create(
            UUID.randomUUID(),
            "Licence Informatique",
            parentIdSiSco,
            institution,
            "11000001",
            startDate,
            endDate,
            EGroupType.PROGRAM,
            null);
    when(groupRepository.existsByIdSiSco(idSiSco)).thenReturn(false);
    when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
    when(groupRepository.findByIdSiSco(parentIdSiSco)).thenReturn(Optional.of(programParent));
    when(groupRepository.save(any(Group.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("creating a program option attached to the program");
    Group result =
        service.create(
            "Parcours IA",
            idSiSco,
            institutionId,
            "11000002",
            startDate,
            endDate,
            EGroupType.PROGRAM_OPTION,
            parentIdSiSco);

    BddLogger.then("it should save the program option with the resolved parent");
    assertNotNull(result);
    assertSame(programParent, result.getParent().orElseThrow());
    verify(groupRepository).save(any(Group.class));
  }

  @Test
  void
      shouldThrowGroupStudentGroupRequiresParentException_whenCreatingStudentGroup_withoutParentIdSiSco() {
    BddLogger.given("a GroupServiceImpl service");
    String idSiSco = "10000003";
    when(groupRepository.existsByIdSiSco(idSiSco)).thenReturn(false);
    when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));

    BddLogger.when("creating a student group without a parent id_si_sco");
    assertThrows(
        GroupStudentGroupRequiresParentException.class,
        () ->
            service.create(
                "Groupe A",
                idSiSco,
                institutionId,
                "11000003",
                startDate,
                endDate,
                EGroupType.STUDENT_GROUP,
                null));

    BddLogger.then("it should not save anything");
    verify(groupRepository, never()).save(any());
  }

  @Test
  void
      shouldThrowGroupStudentGroupParentMustBeProgramOrOptionException_whenParentIsAnotherStudentGroup() {
    BddLogger.given("a student group used as parent");
    String idSiSco = "10000003";
    String parentIdSiSco = "10000004";
    Group studentGroupParent =
        Group.create(
            UUID.randomUUID(),
            "Groupe B",
            parentIdSiSco,
            institution,
            "11000004",
            startDate,
            endDate,
            EGroupType.STUDENT_GROUP,
            null);
    when(groupRepository.existsByIdSiSco(idSiSco)).thenReturn(false);
    when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
    when(groupRepository.findByIdSiSco(parentIdSiSco)).thenReturn(Optional.of(studentGroupParent));

    BddLogger.when("creating a student group attached to another student group");
    assertThrows(
        GroupStudentGroupParentMustBeProgramOrOptionException.class,
        () ->
            service.create(
                "Groupe A",
                idSiSco,
                institutionId,
                "11000003",
                startDate,
                endDate,
                EGroupType.STUDENT_GROUP,
                parentIdSiSco));

    BddLogger.then("it should not save anything");
    verify(groupRepository, never()).save(any());
  }

  @Test
  void shouldCreateStudentGroup_whenParentIsProgram() {
    BddLogger.given("a program parent");
    String idSiSco = "10000003";
    String parentIdSiSco = "10000001";
    Group programParent =
        Group.create(
            UUID.randomUUID(),
            "Licence Informatique",
            parentIdSiSco,
            institution,
            "11000001",
            startDate,
            endDate,
            EGroupType.PROGRAM,
            null);
    when(groupRepository.existsByIdSiSco(idSiSco)).thenReturn(false);
    when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
    when(groupRepository.findByIdSiSco(parentIdSiSco)).thenReturn(Optional.of(programParent));
    when(groupRepository.save(any(Group.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("creating a student group attached to the program");
    Group result =
        service.create(
            "Groupe A",
            idSiSco,
            institutionId,
            "11000003",
            startDate,
            endDate,
            EGroupType.STUDENT_GROUP,
            parentIdSiSco);

    BddLogger.then("it should save the student group with the resolved parent");
    assertNotNull(result);
    assertSame(programParent, result.getParent().orElseThrow());
    verify(groupRepository).save(any(Group.class));
  }

  @Test
  void shouldCreateStudentGroup_whenParentIsProgramOption() {
    BddLogger.given("a program option parent");
    String idSiSco = "10000003";
    String parentIdSiSco = "10000002";
    Group programOptionParent =
        Group.create(
            UUID.randomUUID(),
            "Parcours IA",
            parentIdSiSco,
            institution,
            "11000002",
            startDate,
            endDate,
            EGroupType.PROGRAM_OPTION,
            null);
    when(groupRepository.existsByIdSiSco(idSiSco)).thenReturn(false);
    when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
    when(groupRepository.findByIdSiSco(parentIdSiSco)).thenReturn(Optional.of(programOptionParent));
    when(groupRepository.save(any(Group.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("creating a student group attached to the program option");
    Group result =
        service.create(
            "Groupe A",
            idSiSco,
            institutionId,
            "11000003",
            startDate,
            endDate,
            EGroupType.STUDENT_GROUP,
            parentIdSiSco);

    BddLogger.then("it should save the student group with the resolved parent");
    assertNotNull(result);
    assertSame(programOptionParent, result.getParent().orElseThrow());
    verify(groupRepository).save(any(Group.class));
  }

  @Test
  void shouldThrowGroupIdSiScoAlreadyExistsException_whenIdSiScoIsAlreadyUsed() {
    BddLogger.given("an existing group registered under an id_si_sco");
    String idSiSco = "10000001";
    when(groupRepository.existsByIdSiSco(idSiSco)).thenReturn(true);

    BddLogger.when("creating a group with an already used id_si_sco");
    assertThrows(
        GroupIdSiScoAlreadyExistsException.class,
        () ->
            service.create(
                "Licence Informatique",
                idSiSco,
                institutionId,
                "11000001",
                startDate,
                endDate,
                EGroupType.PROGRAM,
                null));

    BddLogger.then("it should not save anything");
    verify(groupRepository, never()).save(any());
  }
}
