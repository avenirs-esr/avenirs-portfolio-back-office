package fr.avenirsesr.portfolio.backoffice.group.domain.service;

import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupNotFoundException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupProgramCannotHaveParentException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupProgramOptionParentMustBeProgramException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupProgramOptionRequiresParentException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupStudentGroupParentMustBeProgramOrOptionException;
import fr.avenirsesr.portfolio.backoffice.group.domain.exception.GroupStudentGroupRequiresParentException;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.Group;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupData;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupImportFailure;
import fr.avenirsesr.portfolio.backoffice.group.domain.model.GroupImportSummary;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.input.GroupService;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import fr.avenirsesr.portfolio.common.group.domain.model.enums.EGroupType;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class GroupServiceImpl implements GroupService {
  private final GroupRepository groupRepository;
  private final InstitutionRepository institutionRepository;

  @Override
  public Group create(
      String name,
      String idSISco,
      String institutionUAI,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      String parentIdSISco) {
    return upsert(name, idSISco, institutionUAI, codeSise, startDate, endDate, type, parentIdSISco)
        .group();
  }

  @Override
  public GroupImportSummary createAll(List<GroupData> groups) {
    List<Group> created = new ArrayList<>();
    List<Group> updated = new ArrayList<>();
    List<GroupImportFailure> failed = new ArrayList<>();

    for (GroupData data : groups) {
      try {
        UpsertResult result =
            upsert(
                data.name(),
                data.idSISco(),
                data.institutionUAI(),
                data.codeSise(),
                data.startDate(),
                data.endDate(),
                data.type(),
                data.parentIdSISco());
        if (result.created()) {
          created.add(result.group());
        } else {
          updated.add(result.group());
        }
      } catch (BusinessException e) {
        log.warn("Failed to import group with id_si_sco {}: {}", data.idSISco(), e.getMessage());
        failed.add(new GroupImportFailure(data.idSISco(), e.getMessage()));
      }
    }

    return new GroupImportSummary(created, updated, failed);
  }

  /** Creates the group, or updates the existing one matching the given id_si_sco. */
  private UpsertResult upsert(
      String name,
      String idSISco,
      String institutionUAI,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      String parentIdSISco) {
    Optional<Group> existing = groupRepository.findByIdSISco(idSISco);
    if (existing.isPresent()) {
      return new UpsertResult(
          update(idSISco, name, institutionUAI, codeSise, startDate, endDate, type, parentIdSISco),
          false);
    }

    Institution institution = findInstitutionOrThrow(institutionUAI);
    Group parent = resolveParent(type, parentIdSISco);
    Group group =
        Group.create(
            idFromIdSISco(idSISco),
            name,
            idSISco,
            institution,
            codeSise,
            startDate,
            endDate,
            type,
            parent);
    return new UpsertResult(groupRepository.save(group), true);
  }

  private static UUID idFromIdSISco(String idSISco) {
    return UUID.nameUUIDFromBytes(("group:" + idSISco).getBytes(StandardCharsets.UTF_8));
  }

  private record UpsertResult(Group group, boolean created) {}

  @Override
  public Group update(
      String idSISco,
      String name,
      String institutionUAI,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      String parentIdSISco) {
    Group group = groupRepository.findByIdSISco(idSISco).orElseThrow(GroupNotFoundException::new);

    Institution institution = findInstitutionOrThrow(institutionUAI);
    Group parent = resolveParent(type, parentIdSISco);

    group.setName(name);
    group.setInstitution(institution);
    group.setCodeSise(codeSise);
    group.setStartDate(startDate);
    group.setEndDate(endDate);
    group.setType(type);
    group.setParent(parent);

    return groupRepository.save(group);
  }

  @Override
  public List<Group> updateAll(List<GroupData> groups) {
    return groups.stream()
        .map(
            data ->
                update(
                    data.idSISco(),
                    data.name(),
                    data.institutionUAI(),
                    data.codeSise(),
                    data.startDate(),
                    data.endDate(),
                    data.type(),
                    data.parentIdSISco()))
        .toList();
  }

  @Override
  public List<Group> findAll(
      UUID institutionId, UUID parentId, EGroupType type, LocalDate startDate, LocalDate endDate) {
    return groupRepository.findAll(institutionId, parentId, type, startDate, endDate);
  }

  @Override
  public Group findById(UUID id) {
    return groupRepository.findById(id).orElseThrow(GroupNotFoundException::new);
  }

  @Override
  public Group findProgramOf(UUID groupId) {
    Group program = findById(groupId);
    Set<UUID> visited = new HashSet<>();
    visited.add(program.getId());

    Optional<Group> parent = program.getParent();
    while (parent.isPresent() && visited.add(parent.get().getId())) {
      program = parent.get();
      parent = program.getParent();
    }

    if (program.getType() != EGroupType.PROGRAM) {
      log.warn(
          "Group {} has no program ancestor, returning its topmost ancestor {} of type {}",
          groupId,
          program.getId(),
          program.getType());
    }

    return program;
  }

  @Override
  public boolean staffHasAccess(List<UUID> affiliatedIds, List<UUID> targetIds) {
    List<UUID> distinctTargetIds = targetIds.stream().distinct().toList();
    List<Group> targets = groupRepository.findAllById(distinctTargetIds);
    if (targets.size() != distinctTargetIds.size()) {
      throw new GroupNotFoundException();
    }

    Set<UUID> affiliatedIdSet = new HashSet<>(affiliatedIds);
    return targets.stream().allMatch(target -> isCoveredByAffiliation(target, affiliatedIdSet));
  }

  private boolean isCoveredByAffiliation(Group group, Set<UUID> affiliatedIds) {
    Optional<Group> current = Optional.of(group);
    while (current.isPresent()) {
      if (affiliatedIds.contains(current.get().getId())) {
        return true;
      }
      current = current.get().getParent();
    }

    return false;
  }

  @Override
  public List<UUID> studentAccessibleIds(List<UUID> affiliatedIds) {
    List<UUID> distinctAffiliatedIds = affiliatedIds.stream().distinct().toList();
    List<Group> affiliated = groupRepository.findAllById(distinctAffiliatedIds);
    if (affiliated.size() != distinctAffiliatedIds.size()) {
      throw new GroupNotFoundException();
    }

    Set<UUID> accessibleIds = new LinkedHashSet<>();
    for (Group group : affiliated) {
      Optional<Group> current = Optional.of(group);
      while (current.isPresent() && accessibleIds.add(current.get().getId())) {
        current = current.get().getParent();
      }
    }

    return new ArrayList<>(accessibleIds);
  }

  @Override
  public void delete(UUID id) {
    groupRepository.removeFromDatabase(findById(id));
  }

  private Institution findInstitutionOrThrow(String institutionUAI) {
    return institutionRepository
        .findByUai(institutionUAI)
        .orElseThrow(InstitutionNotFoundException::new);
  }

  private Group resolveParent(EGroupType type, String parentIdSISco) {
    return switch (type) {
      case PROGRAM -> {
        if (parentIdSISco != null) {
          throw new GroupProgramCannotHaveParentException();
        }
        yield null;
      }
      case PROGRAM_OPTION -> {
        if (parentIdSISco == null) {
          throw new GroupProgramOptionRequiresParentException();
        }
        Group parent = findParentOrThrow(parentIdSISco);
        if (parent.getType() != EGroupType.PROGRAM) {
          throw new GroupProgramOptionParentMustBeProgramException();
        }
        yield parent;
      }
      case STUDENT_GROUP -> {
        if (parentIdSISco == null) {
          throw new GroupStudentGroupRequiresParentException();
        }
        Group parent = findParentOrThrow(parentIdSISco);
        if (parent.getType() != EGroupType.PROGRAM
            && parent.getType() != EGroupType.PROGRAM_OPTION) {
          throw new GroupStudentGroupParentMustBeProgramOrOptionException();
        }
        yield parent;
      }
    };
  }

  private Group findParentOrThrow(String parentIdSISco) {
    return groupRepository.findByIdSISco(parentIdSISco).orElseThrow(GroupNotFoundException::new);
  }
}
