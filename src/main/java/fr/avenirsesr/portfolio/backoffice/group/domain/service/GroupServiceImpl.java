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
import fr.avenirsesr.portfolio.backoffice.group.domain.model.enums.EGroupType;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.input.GroupService;
import fr.avenirsesr.portfolio.backoffice.group.domain.port.output.repository.GroupRepository;
import fr.avenirsesr.portfolio.backoffice.institution.domain.exception.InstitutionNotFoundException;
import fr.avenirsesr.portfolio.backoffice.institution.domain.model.Institution;
import fr.avenirsesr.portfolio.backoffice.institution.domain.port.output.repository.InstitutionRepository;
import fr.avenirsesr.portfolio.common.error.domain.exception.BusinessException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
      String idSiSco,
      UUID institutionId,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      String parentIdSiSco) {
    return upsert(name, idSiSco, institutionId, codeSise, startDate, endDate, type, parentIdSiSco)
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
                data.idSiSco(),
                data.institutionId(),
                data.codeSise(),
                data.startDate(),
                data.endDate(),
                data.type(),
                data.parentIdSiSco());
        if (result.created()) {
          created.add(result.group());
        } else {
          updated.add(result.group());
        }
      } catch (BusinessException e) {
        log.warn("Failed to import group with id_si_sco {}: {}", data.idSiSco(), e.getMessage());
        failed.add(new GroupImportFailure(data.idSiSco(), e.getMessage()));
      }
    }

    return new GroupImportSummary(created, updated, failed);
  }

  /** Creates the group, or updates the existing one matching the given id_si_sco. */
  private UpsertResult upsert(
      String name,
      String idSiSco,
      UUID institutionId,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      String parentIdSiSco) {
    Optional<Group> existing = groupRepository.findByIdSiSco(idSiSco);
    if (existing.isPresent()) {
      return new UpsertResult(
          update(idSiSco, name, institutionId, codeSise, startDate, endDate, type, parentIdSiSco),
          false);
    }

    Institution institution = findInstitutionOrThrow(institutionId);
    Group parent = resolveParent(type, parentIdSiSco);
    Group group =
        Group.create(
            UUID.randomUUID(),
            name,
            idSiSco,
            institution,
            codeSise,
            startDate,
            endDate,
            type,
            parent);
    return new UpsertResult(groupRepository.save(group), true);
  }

  private record UpsertResult(Group group, boolean created) {}

  @Override
  public Group update(
      String idSiSco,
      String name,
      UUID institutionId,
      String codeSise,
      LocalDate startDate,
      LocalDate endDate,
      EGroupType type,
      String parentIdSiSco) {
    Group group = groupRepository.findByIdSiSco(idSiSco).orElseThrow(GroupNotFoundException::new);

    Institution institution = findInstitutionOrThrow(institutionId);
    Group parent = resolveParent(type, parentIdSiSco);

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
                    data.idSiSco(),
                    data.name(),
                    data.institutionId(),
                    data.codeSise(),
                    data.startDate(),
                    data.endDate(),
                    data.type(),
                    data.parentIdSiSco()))
        .toList();
  }

  @Override
  public List<Group> findAll() {
    return groupRepository.findAll();
  }

  @Override
  public Group findById(UUID id) {
    return groupRepository.findById(id).orElseThrow(GroupNotFoundException::new);
  }

  @Override
  public void delete(UUID id) {
    groupRepository.removeFromDatabase(findById(id));
  }

  private Institution findInstitutionOrThrow(UUID institutionId) {
    return institutionRepository
        .findById(institutionId)
        .orElseThrow(InstitutionNotFoundException::new);
  }

  private Group resolveParent(EGroupType type, String parentIdSiSco) {
    return switch (type) {
      case PROGRAM -> {
        if (parentIdSiSco != null) {
          throw new GroupProgramCannotHaveParentException();
        }
        yield null;
      }
      case PROGRAM_OPTION -> {
        if (parentIdSiSco == null) {
          throw new GroupProgramOptionRequiresParentException();
        }
        Group parent = findParentOrThrow(parentIdSiSco);
        if (parent.getType() != EGroupType.PROGRAM) {
          throw new GroupProgramOptionParentMustBeProgramException();
        }
        yield parent;
      }
      case STUDENT_GROUP -> {
        if (parentIdSiSco == null) {
          throw new GroupStudentGroupRequiresParentException();
        }
        Group parent = findParentOrThrow(parentIdSiSco);
        if (parent.getType() != EGroupType.PROGRAM
            && parent.getType() != EGroupType.PROGRAM_OPTION) {
          throw new GroupStudentGroupParentMustBeProgramOrOptionException();
        }
        yield parent;
      }
    };
  }

  private Group findParentOrThrow(String parentIdSiSco) {
    return groupRepository.findByIdSiSco(parentIdSiSco).orElseThrow(GroupNotFoundException::new);
  }
}
