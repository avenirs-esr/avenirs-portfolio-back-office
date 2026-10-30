package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.exception.ExternalUserAffiliationCategoryNotAllowedException;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.AffiliatedExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.AffiliationScopeNode;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.EAffiliationScopeNodeType;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationScope;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserDetail;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserGlobalAuthorization;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.StaffAffiliationScope;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.output.repository.ExternalUserAffiliationRepository;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.enums.EExternalSource;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.client.SecurityClient;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.input.GroupService;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionService;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.LoggedInExternalUserService;
import fr.avenirsesr.portfolio.common.data.domain.model.PageCriteria;
import fr.avenirsesr.portfolio.common.data.domain.model.PageInfo;
import fr.avenirsesr.portfolio.common.data.domain.model.PagedResult;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import fr.avenirsesr.portfolio.common.institution.domain.model.enums.EInstitutionType;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.ERole;
import fr.avenirsesr.portfolio.common.security.accesscontrol.domain.model.enums.EUniverse;
import fr.avenirsesr.portfolio.common.security.domain.exception.UserNotAuthorizedException;
import fr.avenirsesr.portfolio.common.testutils.BddLogger;
import fr.avenirsesr.portfolio.common.user.domain.exceptions.ExternalUserNotFoundException;
import fr.avenirsesr.portfolio.common.user.domain.model.enums.EUserStatus;
import java.util.Arrays;
import java.util.Collection;
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
  @Mock private SecurityClient securityClient;

  @InjectMocks private ExternalUserAffiliationServiceImpl service;

  private static ExternalUser externalUser(EUserCategory... categories) {
    return ExternalUser.create(
        "lucas.tessier@university.com",
        "PEG-0001",
        EExternalSource.BACK_OFFICE,
        "0350001A",
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
    return institution(id, "Université de Rennes", EInstitutionType.PRIMARY, parent);
  }

  private static Institution institution(
      UUID id, String name, EInstitutionType type, Institution parent) {
    return Institution.create(id, name, null, "0350001A", null, type, parent);
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
  void shouldConfirmAffiliation_whenUserIsAffiliatedToTheInstitution() {
    BddLogger.given("a user affiliated to an institution");
    ExternalUser user = externalUser(EUserCategory.STAFF);
    UUID institutionId = UUID.randomUUID();
    when(externalUserAffiliationRepository.findAllByExternalUserId(user.getId()))
        .thenReturn(
            List.of(
                ExternalUserAffiliation.create(
                    user, institution(institutionId), null, EUserCategory.STAFF)));

    BddLogger.when("checking its affiliation to that institution");
    boolean result = service.isAffiliated(user, institutionId);

    BddLogger.then("it should confirm the affiliation");
    assertTrue(result);
  }

  @Test
  void shouldDenyAffiliation_whenUserIsAffiliatedToAnotherInstitutionOnly() {
    BddLogger.given("a user affiliated to a single institution");
    ExternalUser user = externalUser(EUserCategory.STAFF);
    when(externalUserAffiliationRepository.findAllByExternalUserId(user.getId()))
        .thenReturn(
            List.of(
                ExternalUserAffiliation.create(
                    user, institution(UUID.randomUUID()), null, EUserCategory.STAFF)));

    BddLogger.when("checking its affiliation to another institution");
    boolean result = service.isAffiliated(user, UUID.randomUUID());

    BddLogger.then("it should deny the affiliation");
    assertFalse(result);
  }

  @Test
  void shouldDenyAffiliation_whenInstitutionIdIsNull() {
    BddLogger.given("a user and no institution to check");
    ExternalUser user = externalUser(EUserCategory.STAFF);

    BddLogger.when("checking its affiliation to a null institution");
    boolean result = service.isAffiliated(user, null);

    BddLogger.then("it should deny the affiliation without hitting the repository");
    assertFalse(result);
    verify(externalUserAffiliationRepository, never()).findAllByExternalUserId(any());
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
    assertEquals(EAffiliationScopeNodeType.PRIMARY, institutionNode.type());
    assertEquals(EAffiliationScopeNodeType.PRIMARY, institutionNode.children().get(0).type());

    assertEquals(1, scope.groups().size());
    AffiliationScopeNode groupNode = scope.groups().get(0);
    assertEquals(programId, groupNode.id());
    assertEquals(1, groupNode.children().size());
    assertEquals(optionId, groupNode.children().get(0).id());
    assertEquals(EAffiliationScopeNodeType.PROGRAM, groupNode.type());
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
    return group(id, name, EGroupType.PROGRAM, parent);
  }

  private static Group group(UUID id, String name, EGroupType type, Group parent) {
    return Group.create(id, name, name, null, null, null, null, type, parent);
  }

  private StaffAffiliationScope resolveGroupScope(Group root, Group... descendants) {
    ExternalUser user = externalUser(EUserCategory.STAFF);
    when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(user);
    when(externalUserAffiliationRepository.findDistinctInstitutionIds(
            user.getId(), EUserCategory.STAFF))
        .thenReturn(List.of());
    when(externalUserAffiliationRepository.findDistinctGroupIds(user.getId(), EUserCategory.STAFF))
        .thenReturn(List.of(root.getId()));
    when(institutionRepository.findAllById(List.of())).thenReturn(List.of());
    when(groupRepository.findAllById(List.of(root.getId()))).thenReturn(List.of(root));
    when(groupRepository.findAllByParentIds(anyCollection()))
        .thenAnswer(
            invocation -> {
              Collection<UUID> parentIds = invocation.getArgument(0);
              return Arrays.stream(descendants)
                  .filter(g -> parentIds.contains(g.getParent().orElseThrow().getId()))
                  .toList();
            });
    return service.staffScope();
  }

  private StaffAffiliationScope resolveInstitutionScope(
      Institution root, Institution... descendants) {
    ExternalUser user = externalUser(EUserCategory.STAFF);
    when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(user);
    when(externalUserAffiliationRepository.findDistinctInstitutionIds(
            user.getId(), EUserCategory.STAFF))
        .thenReturn(List.of(root.getId()));
    when(externalUserAffiliationRepository.findDistinctGroupIds(user.getId(), EUserCategory.STAFF))
        .thenReturn(List.of());
    when(institutionRepository.findAllById(List.of(root.getId()))).thenReturn(List.of(root));
    when(groupRepository.findAllById(List.of())).thenReturn(List.of());
    when(institutionRepository.findAllByParentIds(anyCollection()))
        .thenAnswer(
            invocation -> {
              Collection<UUID> parentIds = invocation.getArgument(0);
              return Arrays.stream(descendants)
                  .filter(i -> parentIds.contains(i.getParent().orElseThrow().getId()))
                  .toList();
            });
    return service.staffScope();
  }

  private static void assertNode(
      AffiliationScopeNode node, UUID id, String title, EAffiliationScopeNodeType type) {
    assertEquals(id, node.id());
    assertEquals(title, node.title());
    assertEquals(type, node.type());
  }

  @Test
  void shouldExposePrimaryWithoutChildren_whenResolvingStaffScope() {
    BddLogger.given("a STAFF affiliated to a PRIMARY institution without SECONDARY");
    Institution primary =
        institution(UUID.randomUUID(), "Université de Nantes", EInstitutionType.PRIMARY, null);

    BddLogger.when("resolving the staff scope");
    AffiliationScopeNode root = resolveInstitutionScope(primary).institutions().get(0);

    BddLogger.then("it should expose a PRIMARY node with empty children");
    assertNode(root, primary.getId(), "Université de Nantes", EAffiliationScopeNodeType.PRIMARY);
    assertEquals(List.of(), root.children());
  }

  @Test
  void shouldExposePrimaryWithSeveralSecondaries_whenResolvingStaffScope() {
    BddLogger.given("a STAFF affiliated to a PRIMARY institution with two SECONDARY");
    Institution primary =
        institution(UUID.randomUUID(), "Université de Nantes", EInstitutionType.PRIMARY, null);
    Institution iut =
        institution(UUID.randomUUID(), "IUT de Nantes", EInstitutionType.SECONDARY, primary);
    Institution ufr =
        institution(UUID.randomUUID(), "UFR Sciences", EInstitutionType.SECONDARY, primary);

    BddLogger.when("resolving the staff scope");
    AffiliationScopeNode root = resolveInstitutionScope(primary, iut, ufr).institutions().get(0);

    BddLogger.then("it should expose the PRIMARY with both SECONDARY leaves");
    assertNode(root, primary.getId(), "Université de Nantes", EAffiliationScopeNodeType.PRIMARY);
    assertEquals(2, root.children().size());
    assertNode(
        root.children().get(0), iut.getId(), "IUT de Nantes", EAffiliationScopeNodeType.SECONDARY);
    assertNode(
        root.children().get(1), ufr.getId(), "UFR Sciences", EAffiliationScopeNodeType.SECONDARY);
    root.children().forEach(child -> assertEquals(List.of(), child.children()));
  }

  @Test
  void shouldExposeSecondaryAsRoot_whenOnlyASecondaryIsAffiliated() {
    BddLogger.given("a STAFF affiliated directly to a SECONDARY institution");
    Institution primary =
        institution(UUID.randomUUID(), "Université de Nantes", EInstitutionType.PRIMARY, null);
    Institution iut =
        institution(UUID.randomUUID(), "IUT de Nantes", EInstitutionType.SECONDARY, primary);

    BddLogger.when("resolving the staff scope");
    AffiliationScopeNode root = resolveInstitutionScope(iut).institutions().get(0);

    BddLogger.then("the root node should be typed SECONDARY");
    assertNode(root, iut.getId(), "IUT de Nantes", EAffiliationScopeNodeType.SECONDARY);
    assertEquals(List.of(), root.children());
  }

  @Test
  void shouldExposeProgramWithoutChildren_whenResolvingStaffScope() {
    BddLogger.given("a STAFF affiliated to a PROGRAM without children");
    Group program = group(UUID.randomUUID(), "BUT Informatique", EGroupType.PROGRAM, null);

    BddLogger.when("resolving the staff scope");
    AffiliationScopeNode root = resolveGroupScope(program).groups().get(0);

    BddLogger.then("it should expose a PROGRAM node with empty children");
    assertNode(root, program.getId(), "BUT Informatique", EAffiliationScopeNodeType.PROGRAM);
    assertEquals(List.of(), root.children());
  }

  @Test
  void shouldKeepProgramOptionType_whenOptionHasNoChildren() {
    BddLogger.given("a PROGRAM with a PROGRAM_OPTION that has no STUDENT_GROUP");
    Group program = group(UUID.randomUUID(), "BUT Informatique", EGroupType.PROGRAM, null);
    Group option = group(UUID.randomUUID(), "Cybersécurité", EGroupType.PROGRAM_OPTION, program);

    BddLogger.when("resolving the staff scope");
    AffiliationScopeNode root = resolveGroupScope(program, option).groups().get(0);

    BddLogger.then("the leaf should still be typed PROGRAM_OPTION, never STUDENT_GROUP");
    assertEquals(1, root.children().size());
    assertNode(
        root.children().get(0),
        option.getId(),
        "Cybersécurité",
        EAffiliationScopeNodeType.PROGRAM_OPTION);
    assertEquals(List.of(), root.children().get(0).children());
  }

  @Test
  void shouldExposeDirectStudentGroup_whenProgramHasNoOption() {
    BddLogger.given("a PROGRAM with a direct STUDENT_GROUP");
    Group program = group(UUID.randomUUID(), "BUT Informatique", EGroupType.PROGRAM, null);
    Group studentGroup =
        group(UUID.randomUUID(), "Groupe BUT-ALL", EGroupType.STUDENT_GROUP, program);

    BddLogger.when("resolving the staff scope");
    AffiliationScopeNode root = resolveGroupScope(program, studentGroup).groups().get(0);

    BddLogger.then("the child should be typed STUDENT_GROUP");
    assertEquals(1, root.children().size());
    assertNode(
        root.children().get(0),
        studentGroup.getId(),
        "Groupe BUT-ALL",
        EAffiliationScopeNodeType.STUDENT_GROUP);
  }

  @Test
  void shouldExposeFullGroupHierarchy_whenProgramHasOptionAndDirectStudentGroup() {
    BddLogger.given(
        "a PROGRAM with a PROGRAM_OPTION holding a STUDENT_GROUP, and a direct STUDENT_GROUP");
    Group program = group(UUID.randomUUID(), "BUT Informatique", EGroupType.PROGRAM, null);
    Group option = group(UUID.randomUUID(), "Cybersécurité", EGroupType.PROGRAM_OPTION, program);
    Group cyberA = group(UUID.randomUUID(), "Groupe CYBER-A", EGroupType.STUDENT_GROUP, option);
    Group all = group(UUID.randomUUID(), "Groupe BUT-ALL", EGroupType.STUDENT_GROUP, program);

    BddLogger.when("resolving the staff scope");
    AffiliationScopeNode root = resolveGroupScope(program, option, cyberA, all).groups().get(0);

    BddLogger.then("every node should carry its real type and children");
    assertNode(root, program.getId(), "BUT Informatique", EAffiliationScopeNodeType.PROGRAM);
    assertEquals(2, root.children().size());
    AffiliationScopeNode optionNode = root.children().get(0);
    assertNode(
        optionNode, option.getId(), "Cybersécurité", EAffiliationScopeNodeType.PROGRAM_OPTION);
    assertEquals(1, optionNode.children().size());
    assertNode(
        optionNode.children().get(0),
        cyberA.getId(),
        "Groupe CYBER-A",
        EAffiliationScopeNodeType.STUDENT_GROUP);
    assertNode(
        root.children().get(1),
        all.getId(),
        "Groupe BUT-ALL",
        EAffiliationScopeNodeType.STUDENT_GROUP);
  }

  @Test
  void shouldExposeProgramOptionAsRoot_whenOnlyAnOptionIsAffiliated() {
    BddLogger.given("a STAFF affiliated directly to a PROGRAM_OPTION");
    Group program = group(UUID.randomUUID(), "BUT Informatique", EGroupType.PROGRAM, null);
    Group option = group(UUID.randomUUID(), "Cybersécurité", EGroupType.PROGRAM_OPTION, program);
    Group cyberA = group(UUID.randomUUID(), "Groupe CYBER-A", EGroupType.STUDENT_GROUP, option);

    BddLogger.when("resolving the staff scope");
    AffiliationScopeNode root = resolveGroupScope(option, cyberA).groups().get(0);

    BddLogger.then("the root node should be typed PROGRAM_OPTION with its STUDENT_GROUP child");
    assertNode(root, option.getId(), "Cybersécurité", EAffiliationScopeNodeType.PROGRAM_OPTION);
    assertNode(
        root.children().get(0),
        cyberA.getId(),
        "Groupe CYBER-A",
        EAffiliationScopeNodeType.STUDENT_GROUP);
  }

  @Test
  void shouldQueryTheRepositoryWithEveryFilter_whenFindingAllAffiliatedUsers() {
    BddLogger.given("a keyword and filters the logged-in staff is affiliated to");
    UUID institutionId = UUID.randomUUID();
    UUID groupId = UUID.randomUUID();
    PageCriteria pageCriteria = new PageCriteria(1, 20);
    PageInfo expectedPageInfo = new PageInfo(1, 20, 0);
    grantLoggedInStaffAccessTo(List.of(institutionId), List.of(groupId), true);
    when(externalUserRepository.findAllAffiliated(
            "tessier",
            List.of(EUserCategory.STAFF),
            List.of(institutionId),
            List.of(groupId),
            pageCriteria))
        .thenReturn(new PagedResult<>(List.of(), expectedPageInfo));

    BddLogger.when("finding all the affiliated users");
    PagedResult<AffiliatedExternalUser> result =
        service.findAll(
            "tessier",
            List.of(EUserCategory.STAFF),
            List.of(institutionId),
            List.of(groupId),
            pageCriteria);

    BddLogger.then("it should forward every filter to the repository, without narrowing them");
    assertEquals(List.of(), result.content());
    assertEquals(expectedPageInfo, result.pageInfo());
  }

  @Test
  void shouldReportTheUserOnceWithEveryRattachement_whenFindingAllAffiliatedUsers() {
    BddLogger.given("a user affiliated to two institutions, to a student group and to a program");
    ExternalUser user = externalUser(EUserCategory.STUDENT);
    Institution rennes =
        institution(UUID.randomUUID(), "Université de Rennes", EInstitutionType.PRIMARY, null);
    Institution bordeaux =
        institution(UUID.randomUUID(), "Université de Bordeaux", EInstitutionType.PRIMARY, null);
    Group program = group(UUID.randomUUID(), "Licence Informatique", EGroupType.PROGRAM, null);
    Group option = group(UUID.randomUUID(), "Parcours IA", EGroupType.PROGRAM_OPTION, program);
    Group groupA = group(UUID.randomUUID(), "Groupe A", EGroupType.STUDENT_GROUP, option);
    PageCriteria pageCriteria = new PageCriteria(0, 8);
    UUID staffInstitutionId = UUID.randomUUID();
    loggedInStaffAffiliatedToInstitutions(List.of(staffInstitutionId));
    when(externalUserRepository.findAllAffiliated(
            null, null, List.of(staffInstitutionId), null, pageCriteria))
        .thenReturn(new PagedResult<>(List.of(user), new PageInfo(0, 8, 1)));
    when(externalUserAffiliationRepository.findAllByExternalUserIds(List.of(user.getId())))
        .thenReturn(
            List.of(
                ExternalUserAffiliation.create(user, rennes, groupA, EUserCategory.STUDENT),
                ExternalUserAffiliation.create(user, rennes, null, EUserCategory.STUDENT),
                ExternalUserAffiliation.create(user, bordeaux, program, EUserCategory.STUDENT)));

    BddLogger.when("finding all the affiliated users");
    List<AffiliatedExternalUser> content =
        service.findAll(null, null, null, null, pageCriteria).content();

    BddLogger.then(
        "it should report the user once, with the ancestors of its groups and no duplicate");
    assertEquals(1, content.size());
    AffiliatedExternalUser affiliatedUser = content.get(0);
    assertSame(user, affiliatedUser.externalUser());
    assertEquals(List.of(EUserCategory.STUDENT), affiliatedUser.categories());
    assertEquals(
        List.of("Université de Bordeaux", "Université de Rennes"),
        affiliatedUser.institutions().stream().map(Institution::getName).toList());
    assertEquals(
        List.of("Groupe A", "Licence Informatique", "Parcours IA"),
        affiliatedUser.groups().stream().map(Group::getName).toList());
  }

  @Test
  void shouldRejectTheSearch_whenFilteringOnAScopeTheStaffIsNotAffiliatedTo() {
    BddLogger.given("a logged-in staff not affiliated to the filtered group");
    UUID groupId = UUID.randomUUID();
    grantLoggedInStaffAccessTo(null, List.of(groupId), false);

    BddLogger.when("finding all the affiliations filtered by that group");

    BddLogger.then("it should reject the search without querying the affiliated users");
    assertThrows(
        UserNotAuthorizedException.class,
        () -> service.findAll(null, null, null, List.of(groupId), new PageCriteria(0, 8)));
    verify(externalUserRepository, never())
        .findAllAffiliated(any(), any(), any(), any(), any(PageCriteria.class));
  }

  @Test
  void shouldDefaultToTheStaffScope_whenNoInstitutionNorGroupIsFiltered() {
    BddLogger.given("a search narrowed by a keyword and a category only");
    UUID institutionId = UUID.randomUUID();
    PageCriteria pageCriteria = new PageCriteria(0, 8);
    PageInfo expectedPageInfo = new PageInfo(0, 8, 0);
    loggedInStaffAffiliatedToInstitutions(List.of(institutionId));
    when(externalUserRepository.findAllAffiliated(
            "tessier", List.of(EUserCategory.STAFF), List.of(institutionId), null, pageCriteria))
        .thenReturn(new PagedResult<>(List.of(), expectedPageInfo));

    BddLogger.when("finding all the affiliated users");
    PagedResult<AffiliatedExternalUser> result =
        service.findAll("tessier", List.of(EUserCategory.STAFF), null, null, pageCriteria);

    BddLogger.then("it should query the repository with the institutions of the staff only");
    assertEquals(List.of(), result.content());
    assertEquals(expectedPageInfo, result.pageInfo());
    verifyNoInteractions(institutionService, groupService);
  }

  @Test
  void shouldDefaultToTheStaffScope_whenInstitutionAndGroupFiltersAreEmpty() {
    BddLogger.given("a search with empty institution and group filters");
    UUID institutionId = UUID.randomUUID();
    PageCriteria pageCriteria = new PageCriteria(0, 8);
    loggedInStaffAffiliatedToInstitutions(List.of(institutionId));
    when(externalUserRepository.findAllAffiliated(
            null, null, List.of(institutionId), List.of(), pageCriteria))
        .thenReturn(new PagedResult<>(List.of(), new PageInfo(0, 8, 0)));

    BddLogger.when("finding all the affiliated users");
    service.findAll(null, null, List.of(), List.of(), pageCriteria);

    BddLogger.then("it should be scoped like a search without filters");
    verify(externalUserRepository)
        .findAllAffiliated(null, null, List.of(institutionId), List.of(), pageCriteria);
  }

  @Test
  void shouldNotNarrowTheSearch_whenNoFilterIsGivenAndTheStaffHasNoAffiliation() {
    BddLogger.given("a logged-in staff without any staff affiliation");
    PageCriteria pageCriteria = new PageCriteria(2, 10);
    PageInfo expectedPageInfo = new PageInfo(2, 10, 0);
    loggedInStaffAffiliatedToInstitutions(List.of());
    when(externalUserRepository.findAllAffiliated(null, null, List.of(), null, pageCriteria))
        .thenReturn(new PagedResult<>(List.of(), expectedPageInfo));

    BddLogger.when("finding all the affiliated users without filters");
    PagedResult<AffiliatedExternalUser> result =
        service.findAll(null, null, null, null, pageCriteria);

    BddLogger.then("it should query the repository with the empty staff scope, as is");
    assertEquals(List.of(), result.content());
    assertEquals(expectedPageInfo, result.pageInfo());
  }

  @Test
  void shouldNotDefaultTheGroups_whenOnlyInstitutionsAreFiltered() {
    BddLogger.given("a staff filtering on an institution it is affiliated to, without any group");
    UUID institutionId = UUID.randomUUID();
    PageCriteria pageCriteria = new PageCriteria(0, 8);
    ExternalUser staff = loggedInStaffAffiliatedToInstitutions(List.of(institutionId));
    when(externalUserRepository.findByEppn(staff.getEppn())).thenReturn(Optional.of(staff));
    when(institutionService.staffHasAccess(List.of(institutionId), List.of(institutionId)))
        .thenReturn(true);
    when(externalUserRepository.findAllAffiliated(
            null, null, List.of(institutionId), null, pageCriteria))
        .thenReturn(new PagedResult<>(List.of(), new PageInfo(0, 8, 0)));

    BddLogger.when("finding all the affiliated users");
    service.findAll(null, null, List.of(institutionId), null, pageCriteria);

    BddLogger.then("it should not narrow the search on any group");
    verify(externalUserRepository)
        .findAllAffiliated(null, null, List.of(institutionId), null, pageCriteria);
  }

  @Test
  void shouldNotDefaultTheInstitutions_whenOnlyGroupsAreFiltered() {
    BddLogger.given("a staff filtering on a group it is affiliated to, without any institution");
    UUID groupId = UUID.randomUUID();
    PageCriteria pageCriteria = new PageCriteria(0, 8);
    ExternalUser staff = loggedInStaffAffiliatedToGroups(List.of(groupId));
    when(externalUserRepository.findByEppn(staff.getEppn())).thenReturn(Optional.of(staff));
    when(groupService.staffHasAccess(List.of(groupId), List.of(groupId))).thenReturn(true);
    when(externalUserRepository.findAllAffiliated(
            null, null, List.of(), List.of(groupId), pageCriteria))
        .thenReturn(new PagedResult<>(List.of(), new PageInfo(0, 8, 0)));

    BddLogger.when("finding all the affiliated users");
    service.findAll(null, null, null, List.of(groupId), pageCriteria);

    BddLogger.then("it should not narrow the search on any institution");
    verify(externalUserRepository)
        .findAllAffiliated(null, null, List.of(), List.of(groupId), pageCriteria);
  }

  @Test
  void shouldReturnGroupsWithTheirParentsOnce_whenGettingExternalUserDetail() {
    BddLogger.given("a student in two groups of the same program, within the staff scope");
    ExternalUser staff = externalUser(EUserCategory.STAFF);
    ExternalUser student = externalUser(EUserCategory.STUDENT);
    Institution institution = institution(UUID.randomUUID());
    Group program = group(UUID.randomUUID(), "BUT Informatique", EGroupType.PROGRAM, null);
    Group option = group(UUID.randomUUID(), "Cybersécurité", EGroupType.PROGRAM_OPTION, program);
    Group studentGroup = group(UUID.randomUUID(), "TD1", EGroupType.STUDENT_GROUP, program);
    when(externalUserRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(staff);
    when(externalUserAffiliationRepository.findDistinctInstitutionIds(
            staff.getId(), EUserCategory.STAFF))
        .thenReturn(List.of(institution.getId()));
    when(externalUserAffiliationRepository.findAllByExternalUserId(student.getId()))
        .thenReturn(
            List.of(
                ExternalUserAffiliation.create(student, institution, option, EUserCategory.STUDENT),
                ExternalUserAffiliation.create(
                    student, institution, studentGroup, EUserCategory.STUDENT)));
    when(institutionService.staffHasAccess(
            List.of(institution.getId()), List.of(institution.getId())))
        .thenReturn(true);

    BddLogger.when("getting the external user detail");
    ExternalUserDetail detail = service.getExternalUserDetail(student.getId());

    BddLogger.then("it should list each group and the shared program once");
    assertSame(institution, detail.institution());
    assertEquals(
        List.of(option.getId(), program.getId(), studentGroup.getId()),
        detail.groups().stream().map(Group::getId).toList());
  }

  @Test
  void shouldRejectExternalUserDetail_whenNoInstitutionOfTheUserIsInTheStaffScope() {
    BddLogger.given("a student affiliated to an institution outside the staff scope");
    ExternalUser staff = externalUser(EUserCategory.STAFF);
    ExternalUser student = externalUser(EUserCategory.STUDENT);
    Institution institution = institution(UUID.randomUUID());
    when(externalUserRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(staff);
    when(externalUserAffiliationRepository.findDistinctInstitutionIds(
            staff.getId(), EUserCategory.STAFF))
        .thenReturn(List.of());
    when(externalUserAffiliationRepository.findAllByExternalUserId(student.getId()))
        .thenReturn(
            List.of(
                ExternalUserAffiliation.create(student, institution, null, EUserCategory.STUDENT)));
    when(institutionService.staffHasAccess(List.of(), List.of(institution.getId())))
        .thenReturn(false);

    BddLogger.when("getting the external user detail");

    BddLogger.then("it should be refused");
    assertThrows(
        UserNotAuthorizedException.class, () -> service.getExternalUserDetail(student.getId()));
  }

  @Test
  void shouldThrowNotFound_whenGettingDetailOfUnknownExternalUser() {
    BddLogger.given("an unknown external user id");
    UUID id = UUID.randomUUID();
    when(externalUserRepository.findById(id)).thenReturn(Optional.empty());

    BddLogger.when("getting the external user detail");

    BddLogger.then("it should be refused");
    assertThrows(ExternalUserNotFoundException.class, () -> service.getExternalUserDetail(id));
  }

  @Test
  void shouldReturnRolesAndUniverses_whenTheUserIsInTheStaffScope() {
    BddLogger.given("a student in the staff scope holding the student and staff roles");
    ExternalUser staff = externalUser(EUserCategory.STAFF);
    ExternalUser student = externalUser(EUserCategory.STUDENT);
    Institution institution = institution(UUID.randomUUID());
    when(externalUserRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(staff);
    when(externalUserAffiliationRepository.findDistinctInstitutionIds(
            staff.getId(), EUserCategory.STAFF))
        .thenReturn(List.of());
    when(externalUserAffiliationRepository.findAllByExternalUserId(student.getId()))
        .thenReturn(
            List.of(
                ExternalUserAffiliation.create(student, institution, null, EUserCategory.STUDENT)));
    when(institutionService.staffHasAccess(List.of(), List.of(institution.getId())))
        .thenReturn(true);
    when(securityClient.findRolesByLogin(student.getEppn()))
        .thenReturn(Set.of(ERole.ROLE_STUDENT, ERole.ROLE_STAFF));

    BddLogger.when("getting the global authorization of the external user");
    ExternalUserGlobalAuthorization authorization =
        service.getExternalUserGlobalAuthorization(student.getId());

    BddLogger.then("it should report the roles and both universes");
    assertEquals(Set.of(ERole.ROLE_STUDENT, ERole.ROLE_STAFF), authorization.roles());
    assertEquals(Set.of(EUniverse.STUDENT, EUniverse.STAFF), authorization.universes());
  }

  @Test
  void shouldNotAskTheSecurityService_whenTheUserIsOutsideTheStaffScope() {
    BddLogger.given("a student affiliated to an institution outside the staff scope");
    ExternalUser staff = externalUser(EUserCategory.STAFF);
    ExternalUser student = externalUser(EUserCategory.STUDENT);
    Institution institution = institution(UUID.randomUUID());
    when(externalUserRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(staff);
    when(externalUserAffiliationRepository.findDistinctInstitutionIds(
            staff.getId(), EUserCategory.STAFF))
        .thenReturn(List.of());
    when(externalUserAffiliationRepository.findAllByExternalUserId(student.getId()))
        .thenReturn(
            List.of(
                ExternalUserAffiliation.create(student, institution, null, EUserCategory.STUDENT)));
    when(institutionService.staffHasAccess(List.of(), List.of(institution.getId())))
        .thenReturn(false);

    BddLogger.when("getting the global authorization of the external user");

    BddLogger.then("it should be refused before any role lookup");
    assertThrows(
        UserNotAuthorizedException.class,
        () -> service.getExternalUserGlobalAuthorization(student.getId()));
    verifyNoInteractions(securityClient);
  }

  /** Stubs the affiliation scope of the logged-in staff, as {@code staffHasAccess} reads it. */
  private ExternalUser loggedInStaffAffiliatedTo(List<UUID> institutionIds, List<UUID> groupIds) {
    ExternalUser staff = externalUser(EUserCategory.STAFF);
    when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(staff);
    when(externalUserAffiliationRepository.findDistinctInstitutionIds(
            staff.getId(), EUserCategory.STAFF))
        .thenReturn(institutionIds);
    when(externalUserAffiliationRepository.findDistinctGroupIds(staff.getId(), EUserCategory.STAFF))
        .thenReturn(groupIds);
    return staff;
  }

  private ExternalUser loggedInStaffAffiliatedToInstitutions(List<UUID> institutionIds) {
    ExternalUser staff = externalUser(EUserCategory.STAFF);
    when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(staff);
    when(externalUserAffiliationRepository.findDistinctInstitutionIds(
            staff.getId(), EUserCategory.STAFF))
        .thenReturn(institutionIds);
    return staff;
  }

  private ExternalUser loggedInStaffAffiliatedToGroups(List<UUID> groupIds) {
    ExternalUser staff = externalUser(EUserCategory.STAFF);
    when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(staff);
    when(externalUserAffiliationRepository.findDistinctGroupIds(staff.getId(), EUserCategory.STAFF))
        .thenReturn(groupIds);
    return staff;
  }

  private void grantLoggedInStaffAccessTo(
      List<UUID> institutionIds, List<UUID> groupIds, boolean granted) {
    ExternalUser staff = externalUser(EUserCategory.STAFF);
    when(loggedInExternalUserService.getLoggedInExternalUser()).thenReturn(staff);
    when(externalUserRepository.findByEppn(staff.getEppn())).thenReturn(Optional.of(staff));

    if (institutionIds != null && !institutionIds.isEmpty()) {
      when(externalUserAffiliationRepository.findDistinctInstitutionIds(
              staff.getId(), EUserCategory.STAFF))
          .thenReturn(List.of());
      when(institutionService.staffHasAccess(List.of(), institutionIds)).thenReturn(granted);
    }
    if (groupIds != null && !groupIds.isEmpty()) {
      when(externalUserAffiliationRepository.findDistinctGroupIds(
              staff.getId(), EUserCategory.STAFF))
          .thenReturn(List.of());
      when(groupService.staffHasAccess(List.of(), groupIds)).thenReturn(granted);
    }
  }
}
