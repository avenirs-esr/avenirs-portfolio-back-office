package fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.service;

import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.exception.ExternalUserAffiliationCategoryNotAllowedException;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.exception.ExternalUserAffiliationNotFoundException;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.AffiliationScopeNode;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliation;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationData;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationImportFailure;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationImportSummary;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.ExternalUserAffiliationScope;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.model.StaffAffiliationScope;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.input.ExternalUserAffiliationService;
import fr.avenirsesr.portfolio.backoffice.externaluser.affiliation.domain.port.output.repository.ExternalUserAffiliationRepository;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.model.ExternalUser;
import fr.avenirsesr.portfolio.backoffice.externaluser.domain.port.output.repository.ExternalUserRepository;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupNotFoundException;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.input.GroupService;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.input.InstitutionService;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.backoffice.shared.domain.port.input.service.LoggedInExternalUserService;
import fr.avenirsesr.portfolio.common.data.domain.model.enums.EUserCategory;
import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.user.domain.exceptions.ExternalUserNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ExternalUserAffiliationServiceImpl implements ExternalUserAffiliationService {

  private final ExternalUserAffiliationRepository externalUserAffiliationRepository;
  private final ExternalUserRepository externalUserRepository;
  private final InstitutionRepository institutionRepository;
  private final GroupRepository groupRepository;
  private final InstitutionService institutionService;
  private final GroupService groupService;
  private final LoggedInExternalUserService loggedInExternalUserService;

  @Override
  public ExternalUserAffiliation addAffiliation(
      UUID externalUserId, UUID institutionId, UUID groupId, EUserCategory category) {
    ExternalUser externalUser =
        externalUserRepository
            .findById(externalUserId)
            .orElseThrow(ExternalUserNotFoundException::new);
    Institution institution =
        institutionRepository
            .findById(institutionId)
            .orElseThrow(InstitutionNotFoundException::new);
    Group group =
        groupId != null
            ? groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new)
            : null;

    return findOrCreate(externalUser, institution, group, category).affiliation();
  }

  @Override
  public void removeAffiliation(UUID externalUserId, UUID affiliationId) {
    ExternalUserAffiliation affiliation =
        externalUserAffiliationRepository
            .findById(affiliationId)
            .filter(a -> a.getExternalUser().getId().equals(externalUserId))
            .orElseThrow(ExternalUserAffiliationNotFoundException::new);

    externalUserAffiliationRepository.removeFromDatabase(affiliation);
  }

  @Override
  public List<ExternalUserAffiliation> getAffiliations(UUID externalUserId) {
    externalUserRepository.findById(externalUserId).orElseThrow(ExternalUserNotFoundException::new);

    return externalUserAffiliationRepository.findAllByExternalUserId(externalUserId);
  }

  @Override
  public ExternalUserAffiliationImportSummary createAll(
      List<ExternalUserAffiliationData> affiliations) {
    List<ExternalUserAffiliation> created = new ArrayList<>();
    List<ExternalUserAffiliation> existing = new ArrayList<>();
    List<ExternalUserAffiliationImportFailure> failed = new ArrayList<>();

    for (ExternalUserAffiliationData data : affiliations) {
      try {
        UpsertResult result = upsertByNaturalKeys(data);
        if (result.created()) {
          created.add(result.affiliation());
        } else {
          existing.add(result.affiliation());
        }
      } catch (BusinessException e) {
        log.warn(
            "Failed to import affiliation for eppn {} / institution {} / group {} / category {}:"
                + " {}",
            data.eppn(),
            data.institutionUAI(),
            data.groupIdSISco(),
            data.category(),
            e.getMessage());
        failed.add(
            new ExternalUserAffiliationImportFailure(
                data.eppn(),
                data.institutionUAI(),
                data.groupIdSISco(),
                data.category(),
                e.getMessage()));
      }
    }

    return new ExternalUserAffiliationImportSummary(created, existing, failed);
  }

  @Override
  public boolean staffHasAccess(
      String eppn, List<UUID> targetInstitutionIds, List<UUID> targetGroupIds) {
    ExternalUser externalUser =
        externalUserRepository.findByEppn(eppn).orElseThrow(ExternalUserNotFoundException::new);

    boolean institutionsOk =
        isEmpty(targetInstitutionIds)
            || institutionService.staffHasAccess(
                institutionIdsOf(externalUser.getId(), EUserCategory.STAFF), targetInstitutionIds);
    boolean groupsOk =
        isEmpty(targetGroupIds)
            || groupService.staffHasAccess(
                groupIdsOf(externalUser.getId(), EUserCategory.STAFF), targetGroupIds);

    return institutionsOk && groupsOk;
  }

  @Override
  public ExternalUserAffiliationScope studentScope(String eppn) {
    ExternalUser externalUser =
        externalUserRepository.findByEppn(eppn).orElseThrow(ExternalUserNotFoundException::new);

    List<UUID> affiliatedInstitutionIds =
        institutionIdsOf(externalUser.getId(), EUserCategory.STUDENT);
    List<UUID> affiliatedGroupIds = groupIdsOf(externalUser.getId(), EUserCategory.STUDENT);

    return new ExternalUserAffiliationScope(
        institutionService.studentAccessibleIds(affiliatedInstitutionIds),
        groupService.studentAccessibleIds(affiliatedGroupIds));
  }

  @Override
  public StaffAffiliationScope staffScope() {
    ExternalUser externalUser = loggedInExternalUserService.getLoggedInExternalUser();

    List<UUID> affiliatedInstitutionIds =
        institutionIdsOf(externalUser.getId(), EUserCategory.STAFF);
    List<UUID> affiliatedGroupIds = groupIdsOf(externalUser.getId(), EUserCategory.STAFF);

    return new StaffAffiliationScope(
        institutionRepository.findAllById(affiliatedInstitutionIds).stream()
            .map(this::toInstitutionItem)
            .map(root -> toScopeTree(root, this::findInstitutionChildren))
            .toList(),
        groupRepository.findAllById(affiliatedGroupIds).stream()
            .map(this::toGroupItem)
            .map(root -> toScopeTree(root, this::findGroupChildren))
            .toList());
  }

  private ScopeItem toInstitutionItem(Institution institution) {
    return new ScopeItem(
        institution.getId(),
        institution.getName(),
        institution.getParent().map(Institution::getId).orElse(null));
  }

  private ScopeItem toGroupItem(Group group) {
    return new ScopeItem(
        group.getId(), group.getName(), group.getParent().map(Group::getId).orElse(null));
  }

  private List<ScopeItem> findInstitutionChildren(Collection<UUID> parentIds) {
    return institutionRepository.findAllByParentIds(parentIds).stream()
        .map(this::toInstitutionItem)
        .toList();
  }

  private List<ScopeItem> findGroupChildren(Collection<UUID> parentIds) {
    return groupRepository.findAllByParentIds(parentIds).stream().map(this::toGroupItem).toList();
  }

  private AffiliationScopeNode toScopeTree(
      ScopeItem root, Function<Collection<UUID>, List<ScopeItem>> childrenLoader) {
    Map<UUID, List<ScopeItem>> childrenByParentId = new HashMap<>();
    Set<UUID> visited = new HashSet<>(Set.of(root.id()));
    List<UUID> currentLevelIds = List.of(root.id());

    while (!currentLevelIds.isEmpty()) {
      List<ScopeItem> children =
          childrenLoader.apply(currentLevelIds).stream()
              .filter(child -> visited.add(child.id()))
              .toList();
      children.forEach(
          child ->
              childrenByParentId
                  .computeIfAbsent(child.parentId(), k -> new ArrayList<>())
                  .add(child));
      currentLevelIds = children.stream().map(ScopeItem::id).toList();
    }

    return toScopeNode(root, childrenByParentId);
  }

  private AffiliationScopeNode toScopeNode(
      ScopeItem item, Map<UUID, List<ScopeItem>> childrenByParentId) {
    List<AffiliationScopeNode> children =
        childrenByParentId.getOrDefault(item.id(), List.of()).stream()
            .map(child -> toScopeNode(child, childrenByParentId))
            .toList();

    return new AffiliationScopeNode(item.id(), item.name(), children);
  }

  private record ScopeItem(UUID id, String name, UUID parentId) {}

  private List<UUID> institutionIdsOf(UUID externalUserId, EUserCategory category) {
    return externalUserAffiliationRepository.findDistinctInstitutionIds(externalUserId, category);
  }

  private List<UUID> groupIdsOf(UUID externalUserId, EUserCategory category) {
    return externalUserAffiliationRepository.findDistinctGroupIds(externalUserId, category);
  }

  private static boolean isEmpty(List<UUID> ids) {
    return ids == null || ids.isEmpty();
  }

  /**
   * Resolves the external user, institution and group from their natural keys, then finds or
   * creates the affiliation.
   */
  private UpsertResult upsertByNaturalKeys(ExternalUserAffiliationData data) {
    ExternalUser externalUser =
        externalUserRepository
            .findByEppn(data.eppn())
            .orElseThrow(ExternalUserNotFoundException::new);
    Institution institution =
        institutionRepository
            .findByUai(data.institutionUAI())
            .orElseThrow(InstitutionNotFoundException::new);
    Group group =
        data.groupIdSISco() != null
            ? groupRepository
                .findByIdSISco(data.groupIdSISco())
                .orElseThrow(GroupNotFoundException::new)
            : null;

    return findOrCreate(externalUser, institution, group, data.category());
  }

  private UpsertResult findOrCreate(
      ExternalUser externalUser, Institution institution, Group group, EUserCategory category) {
    requireCategoryAllowed(externalUser, category);

    UUID groupId = group != null ? group.getId() : null;

    return externalUserAffiliationRepository
        .findByExternalUserIdAndInstitutionIdAndGroupIdAndCategory(
            externalUser.getId(), institution.getId(), groupId, category)
        .map(affiliation -> new UpsertResult(affiliation, false))
        .orElseGet(
            () ->
                new UpsertResult(
                    externalUserAffiliationRepository.save(
                        ExternalUserAffiliation.create(externalUser, institution, group, category)),
                    true));
  }

  private void requireCategoryAllowed(ExternalUser externalUser, EUserCategory category) {
    if (category == null || !externalUser.getCategories().contains(category)) {
      throw new ExternalUserAffiliationCategoryNotAllowedException();
    }
  }

  private record UpsertResult(ExternalUserAffiliation affiliation, boolean created) {}
}
