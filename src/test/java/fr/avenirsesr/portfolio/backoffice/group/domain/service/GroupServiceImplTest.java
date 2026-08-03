package fr.avenirsesr.portfolio.backoffice.group.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupNotFoundException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupProgramCannotHaveParentException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupProgramOptionParentMustBeProgramException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupProgramOptionRequiresParentException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupStudentGroupParentMustBeProgramOrOptionException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupStudentGroupRequiresParentException;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupData;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupImportSummary;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.enums.EGroupType;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.enums.EInstitutionType;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import java.time.LocalDate;
import java.util.List;
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
  void shouldUpdateGroup_whenCreating_withAlreadyUsedIdSiSco() {
    BddLogger.given("an existing group registered under an id_si_sco");
    String idSiSco = "10000001";
    Group existing =
        Group.create(
            UUID.randomUUID(),
            "Licence Informatique",
            idSiSco,
            institution,
            "11000001",
            startDate,
            endDate,
            EGroupType.PROGRAM,
            null);
    when(groupRepository.findByIdSiSco(idSiSco)).thenReturn(Optional.of(existing));
    when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
    when(groupRepository.save(any(Group.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("creating a group with an already used id_si_sco");
    Group result =
        service.create(
            "Licence Informatique - Renamed",
            idSiSco,
            institutionId,
            "new-code-sise",
            startDate,
            endDate,
            EGroupType.PROGRAM,
            null);

    BddLogger.then("it should update and save the existing group instead of failing");
    assertEquals("Licence Informatique - Renamed", result.getName());
    assertEquals("new-code-sise", result.getCodeSise());
    verify(groupRepository).save(existing);
  }

  @Test
  void shouldCreateProgram_whenParentIdSiScoIsNull() {
    BddLogger.given("a GroupServiceImpl service");
    String idSiSco = "10000001";
    when(groupRepository.findByIdSiSco(idSiSco)).thenReturn(Optional.empty());
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
    when(groupRepository.findByIdSiSco(idSiSco)).thenReturn(Optional.empty());
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
    when(groupRepository.findByIdSiSco(idSiSco)).thenReturn(Optional.empty());
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
    when(groupRepository.findByIdSiSco(idSiSco)).thenReturn(Optional.empty());
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
    when(groupRepository.findByIdSiSco(idSiSco)).thenReturn(Optional.empty());
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
    when(groupRepository.findByIdSiSco(idSiSco)).thenReturn(Optional.empty());
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
    when(groupRepository.findByIdSiSco(idSiSco)).thenReturn(Optional.empty());
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
    when(groupRepository.findByIdSiSco(idSiSco)).thenReturn(Optional.empty());
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
    when(groupRepository.findByIdSiSco(idSiSco)).thenReturn(Optional.empty());
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
    when(groupRepository.findByIdSiSco(idSiSco)).thenReturn(Optional.empty());
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
    when(groupRepository.findByIdSiSco(idSiSco)).thenReturn(Optional.empty());
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
  void shouldCreateAllGroups_whenBatchIsValid() {
    BddLogger.given("a GroupServiceImpl service");
    when(groupRepository.findByIdSiSco(any())).thenReturn(Optional.empty());
    when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
    when(groupRepository.save(any(Group.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("creating a batch of programs");
    GroupImportSummary result =
        service.createAll(
            List.of(
                new GroupData(
                    "Licence Informatique",
                    "10000001",
                    institutionId,
                    "11000001",
                    startDate,
                    endDate,
                    EGroupType.PROGRAM,
                    null),
                new GroupData(
                    "Licence Mathematiques",
                    "10000004",
                    institutionId,
                    "11000004",
                    startDate,
                    endDate,
                    EGroupType.PROGRAM,
                    null)));

    BddLogger.then("it should report every group of the batch as created");
    assertEquals(2, result.created().size());
    assertEquals(0, result.updated().size());
    assertEquals(0, result.failed().size());
    verify(groupRepository, times(2)).save(any(Group.class));
  }

  @Test
  void shouldUpdateGroup_whenCreatingAllGroups_withAlreadyUsedIdSiSco() {
    BddLogger.given("an existing group registered under an id_si_sco");
    String idSiSco = "10000001";
    Group existing =
        Group.create(
            UUID.randomUUID(),
            "Licence Informatique",
            idSiSco,
            institution,
            "11000001",
            startDate,
            endDate,
            EGroupType.PROGRAM,
            null);
    when(groupRepository.findByIdSiSco(idSiSco)).thenReturn(Optional.of(existing));
    when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
    when(groupRepository.save(any(Group.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("creating a batch containing that id_si_sco");
    GroupImportSummary result =
        service.createAll(
            List.of(
                new GroupData(
                    "Licence Informatique - Renamed",
                    idSiSco,
                    institutionId,
                    "new-code-sise",
                    startDate,
                    endDate,
                    EGroupType.PROGRAM,
                    null)));

    BddLogger.then("it should report the group as updated instead of created");
    assertEquals(0, result.created().size());
    assertEquals(1, result.updated().size());
    assertEquals(0, result.failed().size());
    assertEquals("Licence Informatique - Renamed", result.updated().get(0).getName());
  }

  @Test
  void shouldContinueBatch_whenOneGroupFails() {
    BddLogger.given("a batch with one program option missing its parent id_si_sco");
    when(groupRepository.findByIdSiSco("10000001")).thenReturn(Optional.empty());
    when(groupRepository.findByIdSiSco("10000002")).thenReturn(Optional.empty());
    when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
    when(groupRepository.save(any(Group.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("creating the batch");
    GroupImportSummary result =
        service.createAll(
            List.of(
                new GroupData(
                    "Licence Informatique",
                    "10000001",
                    institutionId,
                    "11000001",
                    startDate,
                    endDate,
                    EGroupType.PROGRAM,
                    null),
                new GroupData(
                    "Parcours IA",
                    "10000002",
                    institutionId,
                    "11000002",
                    startDate,
                    endDate,
                    EGroupType.PROGRAM_OPTION,
                    null)));

    BddLogger.then("it should save the valid group and report the other as failed");
    assertEquals(1, result.created().size());
    assertEquals(0, result.updated().size());
    assertEquals(1, result.failed().size());
    assertEquals("10000002", result.failed().get(0).idSiSco());
    verify(groupRepository, times(1)).save(any(Group.class));
  }

  @Test
  void shouldUpdateGroup_whenIdSiScoExists() {
    BddLogger.given("an existing group");
    String idSiSco = "10000001";
    Group existing =
        Group.create(
            UUID.randomUUID(),
            "Licence Informatique",
            idSiSco,
            institution,
            "11000001",
            startDate,
            endDate,
            EGroupType.PROGRAM,
            null);
    when(groupRepository.findByIdSiSco(idSiSco)).thenReturn(Optional.of(existing));
    when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
    when(groupRepository.save(any(Group.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("updating the group");
    Group result =
        service.update(
            idSiSco,
            "Licence Informatique - Renamed",
            institutionId,
            "new-code-sise",
            startDate,
            endDate,
            EGroupType.PROGRAM,
            null);

    BddLogger.then("it should update and save the group");
    assertEquals("Licence Informatique - Renamed", result.getName());
    assertEquals("new-code-sise", result.getCodeSise());
    verify(groupRepository).save(existing);
  }

  @Test
  void shouldThrowGroupNotFoundException_whenUpdating_withUnknownIdSiSco() {
    BddLogger.given("a GroupServiceImpl service");
    String idSiSco = "unknown-id-si-sco";
    when(groupRepository.findByIdSiSco(idSiSco)).thenReturn(Optional.empty());

    BddLogger.when("updating a group with an unknown id_si_sco");
    assertThrows(
        GroupNotFoundException.class,
        () ->
            service.update(
                idSiSco,
                "name",
                institutionId,
                "code-sise",
                startDate,
                endDate,
                EGroupType.PROGRAM,
                null));

    BddLogger.then("it should not save anything");
    verify(groupRepository, never()).save(any());
  }

  @Test
  void shouldReturnAllGroups() {
    BddLogger.given("groups in the repository");
    Group group =
        Group.create(
            UUID.randomUUID(),
            "Licence Informatique",
            "10000001",
            institution,
            "11000001",
            startDate,
            endDate,
            EGroupType.PROGRAM,
            null);
    when(groupRepository.findAll(null, null, null, null, null)).thenReturn(List.of(group));

    BddLogger.when("fetching all groups");
    List<Group> result = service.findAll(null, null, null, null, null);

    BddLogger.then("it should return every group");
    assertEquals(List.of(group), result);
  }

  @Test
  void shouldReturnFilteredGroups_whenFindingAllWithFilters() {
    BddLogger.given("groups in the repository and a set of filter criteria");
    UUID parentId = UUID.randomUUID();
    Group group =
        Group.create(
            UUID.randomUUID(),
            "Parcours IA",
            "10000002",
            institution,
            "11000002",
            startDate,
            endDate,
            EGroupType.PROGRAM_OPTION,
            null);
    when(groupRepository.findAll(
            institutionId, parentId, EGroupType.PROGRAM_OPTION, startDate, endDate))
        .thenReturn(List.of(group));

    BddLogger.when("fetching groups filtered by institution, parent, type and date range");
    List<Group> result =
        service.findAll(institutionId, parentId, EGroupType.PROGRAM_OPTION, startDate, endDate);

    BddLogger.then("it should delegate the filters to the repository and return the match");
    assertEquals(List.of(group), result);
    verify(groupRepository)
        .findAll(institutionId, parentId, EGroupType.PROGRAM_OPTION, startDate, endDate);
  }

  @Test
  void shouldReturnGroup_whenIdExists() {
    BddLogger.given("an existing group");
    UUID id = UUID.randomUUID();
    Group group =
        Group.create(
            id,
            "Licence Informatique",
            "10000001",
            institution,
            "11000001",
            startDate,
            endDate,
            EGroupType.PROGRAM,
            null);
    when(groupRepository.findById(id)).thenReturn(Optional.of(group));

    BddLogger.when("fetching the group by id");
    Group result = service.findById(id);

    BddLogger.then("it should return the group");
    assertSame(group, result);
  }

  @Test
  void shouldThrowGroupNotFoundException_whenFindingById_withUnknownId() {
    BddLogger.given("an unknown group id");
    UUID id = UUID.randomUUID();
    when(groupRepository.findById(id)).thenReturn(Optional.empty());

    BddLogger.when("fetching the group by id");
    assertThrows(GroupNotFoundException.class, () -> service.findById(id));

    BddLogger.then("it should throw GroupNotFoundException");
  }

  @Test
  void shouldDeleteGroup_whenIdExists() {
    BddLogger.given("an existing group");
    UUID id = UUID.randomUUID();
    Group group =
        Group.create(
            id,
            "Licence Informatique",
            "10000001",
            institution,
            "11000001",
            startDate,
            endDate,
            EGroupType.PROGRAM,
            null);
    when(groupRepository.findById(id)).thenReturn(Optional.of(group));

    BddLogger.when("deleting the group");
    service.delete(id);

    BddLogger.then("it should remove the group from the database");
    verify(groupRepository).removeFromDatabase(group);
  }
}
