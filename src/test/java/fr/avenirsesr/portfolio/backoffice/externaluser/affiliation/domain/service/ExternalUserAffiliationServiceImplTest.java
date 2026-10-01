package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.exception.ExternalUserAffiliationCategoryNotAllowedException;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.AffiliationScopeNode;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationScope;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.StaffAffiliationScope;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.output.repository.ExternalUserAffiliationRepository;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.input.GroupService;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionService;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.LoggedInExternalUserService;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExternalUserAffiliationServiceImplTest {

  @Mock private ExternalUserAffiliationRepository externalUserAffiliationRepository;
  @Mock private ExternalUserRepository externalUserRepository;
  @Mock private InstitutionRepository institutionRepository;
  @Mock private GroupRepository groupRepository;
  @Mock private InstitutionService institutionService;
  @Mock private GroupService groupService;
  @Mock private LoggedInExternalUserService loggedInExternalUserService;

  @InjectMocks private ExternalUserAffiliationServiceImpl service;

  private static ExternalUser externalUser(EUserCategory... categories) {
    return ExternalUser.create(
        "lucas.tessier@university.com",
        "PEG-0001",
        EExternalSource.BACK_OFFICE,
        Set.of(categories),
        "lucas.tessier@university.com",
        "Lucas",
        "Tessier",
        EUserStatus.ACTIVE);
  }

  private static Institution institution(UUID id) {
    return institution(id, null);
  }

  private static Institution institution(UUID id, Institution parent) {
    return Institution.create(
        id, "Université de Rennes", "0350001A", null, null, EInstitutionType.PRIMARY, parent);
  }

  @Test
  void shouldThrowCategoryNotAllowed_whenAddingAffiliation_withCategoryNotHeldByUser() {
    BddLogger.given("an external user with only the STUDENT category");
    ExternalUser user = externalUser(EUserCategory.STUDENT);
    UUID institutionId = UUID.randomUUID();
    when(externalUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(institutionRepository.findById(institutionId))
        .thenReturn(Optional.of(institution(institutionId)));

    BddLogger.when("adding a STAFF affiliation for that user");

    BddLogger.then("it should reject the affiliation");
    assertThrows(
        ExternalUserAffiliationCategoryNotAllowedException.class,
        () -> service.addAffiliation(user.getId(), institutionId, null, EUserCategory.STAFF));
    verify(externalUserAffiliationRepository, never()).save(any());
  }

  @Test
  void shouldCreateAffiliation_whenCategoryIsHeldByUser() {
    BddLogger.given("an external user with the STAFF category");
    ExternalUser user = externalUser(EUserCategory.STAFF);
    UUID institutionId = UUID.randomUUID();
    Institution institution = institution(institutionId);
    when(externalUserRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(institutionRepository.findById(institutionId)).thenReturn(Optional.of(institution));
    when(externalUserAffiliationRepository
            .findByExternalUserIdAndInstitutionIdAndGroupIdAndCategory(
                user.getId(), institutionId, null, EUserCategory.STAFF))
        .thenReturn(Optional.empty());
    when(externalUserAffiliationRepository.save(any(ExternalUserAffiliation.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    BddLogger.when("adding a STAFF affiliation for that user");
    ExternalUserAffiliation result =
        service.addAffiliation(user.getId(), institutionId, null, EUserCategory.STAFF);

    BddLogger.then("it should create the affiliation with the STAFF category");
    assertEquals(EUserCategory.STAFF, result.getCategory());
    verify(externalUserAffiliationRepository).save(any(ExternalUserAffiliation.class));
  }

  @Test
  void shouldOnlyUseStaffAffiliations_whenCheckingStaffAccess() {
    BddLogger.given("a multi-profile external user with different STUDENT and STAFF institutions");
    ExternalUser user = externalUser(EUserCategory.STUDENT, EUserCategory.STAFF);
    UUID staffInstitutionId = UUID.randomUUID();
    UUID studentOnlyInstitutionId = UUID.randomUUID();
    when(externalUserRepository.findByEppn(user.getEppn())).thenReturn(Optional.of(user));
    when(externalUserAffiliationRepository.findDistinctInstitutionIds(
            user.getId(), EUserCategory.STAFF))
        .thenReturn(List.of(staffInstitutionId));
    when(institutionService.staffHasAccess(
            List.of(staffInstitutionId), List.of(staffInstitutionId)))
        .thenReturn(true);

    BddLogger.when("checking staff access to the STAFF-affiliated institution");
    boolean hasAccess = service.staffHasAccess(user.getEppn(), List.of(staffInstitutionId), null);

    BddLogger.then("it should grant access using only the STAFF affiliations");
    assertTrue(hasAccess);
    verify(externalUserAffiliationRepository, never())
        .findDistinctInstitutionIds(user.getId(), EUserCategory.STUDENT);

    BddLogger.when("checking staff access to the STUDENT-only institution");
    when(institutionService.staffHasAccess(
            List.of(staffInstitutionId), List.of(studentOnlyInstitutionId)))
        .thenReturn(false);
    boolean deniedAccess =
        service.staffHasAccess(user.getEppn(), List.of(studentOnlyInstitutionId), null);

    BddLogger.then("it should deny access: the STUDENT affiliation must never grant STAFF rights");
    assertFalse(deniedAccess);
  }

  @Test
  void shouldOnlyUseStudentAffiliations_whenResolvingStudentScope() {
    BddLogger.given("a multi-profile external user with different STUDENT and STAFF institutions");
    ExternalUser user = externalUser(EUserCategory.STUDENT, EUserCategory.STAFF);
    UUID studentInstitutionId = UUID.randomUUID();
    UUID staffOnlyInstitutionId = UUID.randomUUID();
    when(externalUserRepository.findByEppn(user.getEppn())).thenReturn(Optional.of(user));
    when(externalUserAffiliationRepository.findDistinctInstitutionIds(
            user.getId(), EUserCategory.STUDENT))
        .thenReturn(List.of(studentInstitutionId));
    when(institutionService.studentAccessibleIds(List.of(studentInstitutionId)))
        .thenReturn(List.of(studentInstitutionId));
    when(groupService.studentAccessibleIds(anyList())).thenReturn(List.of());

    BddLogger.when("resolving the student scope");
    ExternalUserAffiliationScope scope = service.studentScope(user.getEppn());

    BddLogger.then(
        "it should expose only the institution reachable through the STUDENT affiliation, never"
            + " the STAFF-only one");
    assertEquals(List.of(studentInstitutionId), scope.institutionIds());
    assertFalse(scope.institutionIds().contains(staffOnlyInstitutionId));
    verify(externalUserAffiliationRepository, never())
        .findDistinctInstitutionIds(user.getId(), EUserCategory.STAFF);
  }

  @Test
  void shouldBuildAccessTree_whenResolvingStaffScope() {
    BddLogger.given(
        "a STAFF external user affiliated to an institution and a program, both having children");
    ExternalUser user = externalUser(EUserCategory.STAFF);
    UUID parentInstitutionId = UUID.randomUUID();
    UUID childInstitutionId = UUID.randomUUID();
    Institution parentInstitution = institution(parentInstitutionId);
    Institution childInstitution = institution(childInstitutionId, parentInstitution);
    UUID programId = UUID.randomUUID();
    UUID optionId = UUID.randomUUID();
    Group program = group(programId, "Licence Informatique", null);
    Group option = group(optionId, "Parcours Data", program);

    when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(user);
    when(externalUserAffiliationRepository.findDistinctInstitutionIds(
            user.getId(), EUserCategory.STAFF))
        .thenReturn(List.of(parentInstitutionId));
    when(externalUserAffiliationRepository.findDistinctGroupIds(user.getId(), EUserCategory.STAFF))
        .thenReturn(List.of(programId));
    when(institutionRepository.findAllById(List.of(parentInstitutionId)))
        .thenReturn(List.of(parentInstitution));
    when(institutionRepository.findAllByParentIds(List.of(parentInstitutionId)))
        .thenReturn(List.of(childInstitution));
    when(institutionRepository.findAllByParentIds(List.of(childInstitutionId)))
        .thenReturn(List.of());
    when(groupRepository.findAllById(List.of(programId))).thenReturn(List.of(program));
    when(groupRepository.findAllByParentIds(List.of(programId))).thenReturn(List.of(option));
    when(groupRepository.findAllByParentIds(List.of(optionId))).thenReturn(List.of());

    BddLogger.when("resolving the staff scope");
    StaffAffiliationScope scope = service.staffScope();

    BddLogger.then(
        "it should expose the affiliated institution and program, each with their children");
    assertEquals(1, scope.institutions().size());
    AffiliationScopeNode institutionNode = scope.institutions().get(0);
    assertEquals(parentInstitutionId, institutionNode.id());
    assertEquals(1, institutionNode.children().size());
    assertEquals(childInstitutionId, institutionNode.children().get(0).id());

    assertEquals(1, scope.groups().size());
    AffiliationScopeNode groupNode = scope.groups().get(0);
    assertEquals(programId, groupNode.id());
    assertEquals(1, groupNode.children().size());
    assertEquals(optionId, groupNode.children().get(0).id());
  }

  @Test
  void shouldGrantAccessVacuously_whenNoTargetIsGiven() {
    BddLogger.given("a STAFF external user with no affiliation queried");
    ExternalUser user = externalUser(EUserCategory.STAFF);
    when(externalUserRepository.findByEppn(user.getEppn())).thenReturn(Optional.of(user));

    BddLogger.when("checking staff access with empty target lists");
    boolean hasAccess = service.staffHasAccess(user.getEppn(), List.of(), List.of());

    BddLogger.then("it should be vacuously granted, without resolving any affiliation");
    assertTrue(hasAccess);
    verifyNoInteractions(institutionService, groupService);
  }

  private static Group group(UUID id, String name, Group parent) {
    return Group.create(id, name, name, null, null, null, null, EGroupType.PROGRAM, parent);
  }
}
